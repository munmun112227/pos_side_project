package com.pos.system.consumer;

import com.pos.system.config.RabbitMQConfig;
import com.pos.system.domain.entity.*;
import com.pos.system.dto.TransactionUploadMessage;
import com.pos.system.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Consumer on the server side that listens to the transaction upload queue.
 * Resolves entities (Cashier, POS, Member, Product, PaymentMethod, Discount)
 * and stores the transaction data in the central database.
 */
@Component
public class TransactionUploadConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionUploadConsumer.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CashierRepository cashierRepository;

    @Autowired
    private PosRepository posRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private DiscountRepository discountRepository;

    /**
     * Consumes the transaction upload message, performs an idempotency check,
     * resolves foreign keys, and persists the transaction.
     */
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_UPLOAD_QUEUE)
    @Transactional
    public void consumeTransactionUpload(TransactionUploadMessage msg) {
        if (msg == null) {
            return;
        }

        log.info("Received transaction upload message for transaction No: {}", msg.getTransactionNo());

        // Idempotency check: skip if transaction is already stored
        Optional<Transaction> existingTx = transactionRepository.findByTransactionNo(msg.getTransactionNo());
        if (existingTx.isPresent()) {
            log.warn("Transaction No: {} already exists. Skipping duplicate insert.", msg.getTransactionNo());
            return;
        }

        // 1. Look up Cashier (Create if missing to avoid breaking offline registration flows)
        String cashierCode = msg.getCashierCode() != null ? msg.getCashierCode() : "SYSTEM";
        Cashier cashier = cashierRepository.findByCode(cashierCode)
                .orElseGet(() -> {
                    Cashier newCashier = new Cashier();
                    newCashier.setCode(cashierCode);
                    newCashier.setName("Auto-Created Cashier (" + cashierCode + ")");
                    return cashierRepository.save(newCashier);
                });

        // 2. Look up Pos (Create if missing)
        String posCode = msg.getPosCode() != null ? msg.getPosCode() : "POS-01";
        Pos pos = posRepository.findByCode(posCode)
                .orElseGet(() -> {
                    Pos newPos = new Pos();
                    newPos.setCode(posCode);
                    newPos.setLocation("Auto-Created Location");
                    newPos.setDescription("Auto-created during sync");
                    return posRepository.save(newPos);
                });

        // 3. Look up Member (Optional)
        Member member = null;
        if (msg.getMemberCode() != null && !msg.getMemberCode().trim().isEmpty()) {
            member = memberRepository.findByCode(msg.getMemberCode()).orElse(null);
        }

        // 4. Instantiate Transaction
        Transaction tx = new Transaction();
        tx.setTransactionNo(msg.getTransactionNo());
        tx.setCashier(cashier);
        tx.setPos(pos);
        tx.setMember(member);
        tx.setTotalAmount(msg.getTotalAmount());
        tx.setDiscountAmount(msg.getDiscountAmount());
        tx.setNetAmount(msg.getNetAmount());
        tx.setCreatedAt(msg.getCreatedAt() != null ? msg.getCreatedAt() : java.time.LocalDateTime.now());
        
        // 儲存從 Client 傳過來的交易狀態與類別
        String status = msg.getStatus() != null ? msg.getStatus() : "PAID";
        tx.setTransactionType(msg.getTransactionType() != null ? msg.getTransactionType() : ("CANCELLED".equals(status) ? "CANCELLED" : "RETAIL"));
        tx.setRemark("交易狀態: " + status);



        // 5. Map Transaction Items
        if (msg.getItems() != null) {
            for (TransactionUploadMessage.ItemDto itemDto : msg.getItems()) {
                Product product = productRepository.findBySku(itemDto.getSku())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found in central catalog for SKU: " + itemDto.getSku()));

                TransactionItem item = new TransactionItem();
                item.setTransaction(tx);
                item.setProduct(product);
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getFinalUnitPrice());
                item.setSubtotal(itemDto.getSubtotal());

                tx.getItems().add(item);
            }
        }

        // 6. Map Payments
        if (msg.getPayments() != null) {
            for (TransactionUploadMessage.PaymentDto paymentDto : msg.getPayments()) {
                PaymentMethod paymentMethod = paymentMethodRepository.findByCode(paymentDto.getPaymentMethodCode())
                        .orElseThrow(() -> new IllegalArgumentException("Payment method not found in central catalog for Code: " + paymentDto.getPaymentMethodCode()));

                TransactionPayment payment = new TransactionPayment();
                payment.setTransaction(tx);
                payment.setPaymentMethod(paymentMethod);
                payment.setAmount(paymentDto.getAmount());
                payment.setPayAmount(paymentDto.getPayAmount());
                payment.setChangeAmount(paymentDto.getChangeAmount());

                tx.getPayments().add(payment);
            }
        }

        // 7. Map Discounts
        if (msg.getDiscounts() != null) {
            for (TransactionUploadMessage.DiscountDto discountDto : msg.getDiscounts()) {
                Discount discount = discountRepository.findByCode(discountDto.getDiscountCode())
                        .orElseThrow(() -> new IllegalArgumentException("Discount promotion not found in central catalog for Code: " + discountDto.getDiscountCode()));

                TransactionDiscount transactionDiscount = new TransactionDiscount();
                transactionDiscount.setTransaction(tx);
                transactionDiscount.setDiscount(discount);
                transactionDiscount.setAmount(discountDto.getAmount());

                tx.getDiscounts().add(transactionDiscount);
            }
        }

        // 8. Save parent transaction (Cascades all children)
        transactionRepository.save(tx);
        log.info("Successfully stored transaction No: {} with {} items and {} payments in central DB.", 
                tx.getTransactionNo(), 
                tx.getItems().size(), 
                tx.getPayments().size());
    }
}
