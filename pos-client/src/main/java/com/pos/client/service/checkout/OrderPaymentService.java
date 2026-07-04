package com.pos.client.service.checkout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.client.domain.entity.LocalOrder;
import com.pos.client.domain.entity.LocalOrderItem;
import com.pos.client.domain.entity.LocalOrderPayment;
import com.pos.client.domain.entity.LocalOrderPromotion;
import com.pos.client.domain.entity.OutboxEvent;
import com.pos.client.dto.TransactionUploadMessage;
import com.pos.client.repository.LocalOrderRepository;
import com.pos.client.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 協調複式付款流程的服務
 */
@Service
public class OrderPaymentService {

    private final LocalOrderRepository orderRepository;
    private final CheckoutEngine checkoutEngine;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final String clientId;

    public OrderPaymentService(LocalOrderRepository orderRepository, 
                               CheckoutEngine checkoutEngine,
                               OutboxEventRepository outboxEventRepository,
                               ObjectMapper objectMapper,
                               @Value("${pos.client.id}") String clientId) {
        this.orderRepository = orderRepository;
        this.checkoutEngine = checkoutEngine;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
    }

    /**
     * 對指定訂單執行「一筆」付款
     *
     * @param orderId 訂單 ID
     * @param request 支付請求 (包含這一步要扣多少錢、付款管道、以及該管道細節)
     * @return 訂單當前的最新付款狀態
     */
    @Transactional
    public OrderPaymentStatus addPayment(String orderId, CheckoutRequest request) {
        // 1. 查詢訂單
        LocalOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到訂單: " + orderId));

        // 檢查訂單狀態
        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("只有草稿狀態 (DRAFT) 的訂單才能執行付款，目前狀態為: " + order.getStatus());
        }

        // 2. 計算目前已付金額與剩餘應收金額
        double totalPaid = order.getPayments().stream()
                .mapToDouble(LocalOrderPayment::getAmount)
                .sum();
        double remainingAmount = order.getFinalAmount() - totalPaid;

        if (remainingAmount <= 0) {
            throw new IllegalStateException("該訂單已付清，不需再付款");
        }

        // 3. 確保本次支付金額沒有超過剩餘應收金額 (防呆)
        if (request.getAmount() > remainingAmount) {
            throw new IllegalArgumentException("本次付款抵扣金額 (" + request.getAmount() + ") 不能大於剩餘應付金額 (" + remainingAmount + ")");
        }

        // 4. 執行支付策略 (呼叫 CheckoutEngine)
        CheckoutResult result = checkoutEngine.process(request);

        if (!result.isSuccess()) {
            throw new RuntimeException("付款執行失敗: " + result.getErrorMessage());
        }

        // 5. 支付成功，將付款明細記錄到訂單中
        LocalOrderPayment payment = new LocalOrderPayment();
        payment.setPaymentMethodId(request.getPaymentMethodId());
        payment.setAmount(request.getAmount()); 
        payment.setPayAmount(result.getPayAmount());
        payment.setChangeAmount(result.getChangeAmount());
        
        order.addPayment(payment);

        // 6. 計算最新狀態
        double newTotalPaid = totalPaid + request.getAmount();
        double newRemaining = Math.max(0.0, order.getFinalAmount() - newTotalPaid);

        // 如果全部付清，更新訂單狀態為已支付 (PAID)，並寫入 Outbox 準備送往 MQ
        if (newRemaining <= 0.0) {
            order.setStatus("PAID");
            saveOutboxEvent(order);
        }

        orderRepository.save(order);

        OrderPaymentStatus status = new OrderPaymentStatus();
        status.setOrderId(orderId);
        status.setReceivableAmount(order.getFinalAmount());
        status.setTotalPaidAmount(newTotalPaid);
        status.setBalanceAmount(newRemaining);
        status.setFullyPaid(newRemaining <= 0.0);
        status.setCurrentPayments(order.getPayments());

        return status;
    }

    /**
     * 取消/作廢單筆已付款項目 (中途取消某個付款方式)
     */
    @Transactional
    public OrderPaymentStatus voidPayment(String orderId, Long paymentId) {
        LocalOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到訂單: " + orderId));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("訂單已完成付款或已取消，無法作廢單筆付款。目前狀態為: " + order.getStatus());
        }

        LocalOrderPayment paymentToVoid = order.getPayments().stream()
                .filter(p -> p.getId().equals(paymentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("在此訂單下找不到指定的付款紀錄 ID: " + paymentId));

        // 如果是信用卡等線上支付，在此處應呼叫刷卡管道 API 進行退刷或取消授權
        if ("CREDIT_CARD".equalsIgnoreCase(paymentToVoid.getPaymentMethodId())) {
            // cardReader.voidTransaction(paymentToVoid.getTransactionId());
        }

        // 自關聯列表中移除，因 orphanRemoval = true，JPA 會自動從資料庫刪除此筆記錄
        order.getPayments().remove(paymentToVoid);
        orderRepository.save(order);

        // 重新計算最新金額狀態
        double totalPaid = order.getPayments().stream()
                .mapToDouble(LocalOrderPayment::getAmount)
                .sum();
        double remainingAmount = Math.max(0.0, order.getFinalAmount() - totalPaid);

        OrderPaymentStatus status = new OrderPaymentStatus();
        status.setOrderId(orderId);
        status.setReceivableAmount(order.getFinalAmount());
        status.setTotalPaidAmount(totalPaid);
        status.setBalanceAmount(remainingAmount);
        status.setFullyPaid(false);
        status.setCurrentPayments(order.getPayments());

        return status;
    }

    /**
     * 中途取消整筆交易
     */
    @Transactional
    public void cancelTransaction(String orderId) {
        LocalOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到訂單: " + orderId));

        if ("PAID".equals(order.getStatus())) {
            throw new IllegalStateException("交易已完成付款，無法取消，必須走退貨退款程序");
        }

        if ("CANCELLED".equals(order.getStatus())) {
            return;
        }

        // 1. 逐筆處理線上金流的退刷/取消授權 (如信用卡)
        for (LocalOrderPayment payment : order.getPayments()) {
            if ("CREDIT_CARD".equalsIgnoreCase(payment.getPaymentMethodId())) {
                // cardReader.voidTransaction(payment.getTransactionId());
            }
        }

        // 2. 清除所有已支付的明細 (因 orphanRemoval = true 會自 H2 物理刪除)
        order.getPayments().clear();

        // 3. 將訂單狀態更新為已取消 (CANCELLED)
        order.setStatus("CANCELLED");
        saveOutboxEvent(order);
        orderRepository.save(order);

    }

    /**
     * 將已完成交易包裝為 TransactionUploadMessage 並存入 Outbox 發件箱中。
     */
    private void saveOutboxEvent(LocalOrder order) {
        try {
            TransactionUploadMessage msg = new TransactionUploadMessage();
            msg.setTransactionNo(order.getOrderId());
            msg.setCashierCode("CASHIER-01");
            msg.setPosCode(clientId);
            msg.setMemberCode(order.getMemberId());
            msg.setTotalAmount(BigDecimal.valueOf(order.getOriginalTotal()));
            msg.setDiscountAmount(BigDecimal.valueOf(order.getCartDiscountAmount()));
            msg.setNetAmount(BigDecimal.valueOf(order.getFinalAmount()));
            msg.setCreatedAt(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now());
            msg.setStatus(order.getStatus());
            msg.setTransactionType("CANCELLED".equals(order.getStatus()) ? "CANCELLED" : "RETAIL");



            // 1. 轉換商品明細列表
            List<TransactionUploadMessage.ItemDto> items = new ArrayList<>();
            for (LocalOrderItem item : order.getItems()) {
                TransactionUploadMessage.ItemDto itemDto = new TransactionUploadMessage.ItemDto();
                itemDto.setSku(item.getItemId());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setOriginalUnitPrice(BigDecimal.valueOf(item.getOriginalUnitPrice()));
                itemDto.setFinalUnitPrice(BigDecimal.valueOf(item.getFinalUnitPrice()));
                itemDto.setDiscountAmount(BigDecimal.valueOf(item.getDiscountAmount()));
                itemDto.setAppliedPromoId(item.getAppliedPromoId());
                itemDto.setSubtotal(BigDecimal.valueOf(item.getSubtotal()));
                items.add(itemDto);
            }
            msg.setItems(items);

            // 2. 轉換付款明細列表
            List<TransactionUploadMessage.PaymentDto> payments = new ArrayList<>();
            for (LocalOrderPayment payment : order.getPayments()) {
                TransactionUploadMessage.PaymentDto paymentDto = new TransactionUploadMessage.PaymentDto();
                paymentDto.setPaymentMethodCode(payment.getPaymentMethodId());
                paymentDto.setAmount(BigDecimal.valueOf(payment.getAmount()));
                paymentDto.setPayAmount(BigDecimal.valueOf(payment.getPayAmount()));
                paymentDto.setChangeAmount(BigDecimal.valueOf(payment.getChangeAmount()));
                payments.add(paymentDto);
            }
            msg.setPayments(payments);

            // 3. 轉換訂單優惠明細列表
            List<TransactionUploadMessage.DiscountDto> discounts = new ArrayList<>();
            for (LocalOrderPromotion promo : order.getPromotions()) {
                TransactionUploadMessage.DiscountDto discountDto = new TransactionUploadMessage.DiscountDto();
                discountDto.setDiscountCode(promo.getPromoId());
                discountDto.setAmount(BigDecimal.valueOf(promo.getDiscountAmount()));
                discounts.add(discountDto);
            }
            msg.setDiscounts(discounts);

            // 4. 物件序列化為 JSON 字串並寫入發件箱
            String jsonPayload = objectMapper.writeValueAsString(msg);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(order.getOrderId());
            event.setEventType("TRANSACTION_COMPLETED");
            event.setPayload(jsonPayload);

            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("寫入交易發件箱 (Outbox) 失敗: " + e.getMessage(), e);
        }
    }
}
