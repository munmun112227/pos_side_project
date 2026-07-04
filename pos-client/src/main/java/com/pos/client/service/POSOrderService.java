package com.pos.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.client.domain.entity.LocalOrder;
import com.pos.client.domain.entity.LocalOrderItem;
import com.pos.client.domain.entity.LocalOrderPromotion;
import com.pos.client.domain.entity.OutboxEvent;
import com.pos.client.dto.TransactionUploadMessage;
import com.pos.client.dto.cart.CalculatedItemDto;
import com.pos.client.dto.cart.AppliedPromotionDto;
import com.pos.client.dto.cart.CalculationRequest;
import com.pos.client.dto.cart.CalculationResponse;
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
 * 處理本地訂單生命週期 (如草稿建立、中途取消、結帳歸檔)
 */
@Service
public class POSOrderService {

    private final CartCalculationService cartCalculationService;
    private final LocalOrderRepository orderRepository;
    private final AdvancedOrderNumberService orderNumberService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final String clientId;

    public POSOrderService(CartCalculationService cartCalculationService,
                           LocalOrderRepository orderRepository,
                           AdvancedOrderNumberService orderNumberService,
                           OutboxEventRepository outboxEventRepository,
                           ObjectMapper objectMapper,
                           @Value("${pos.client.id}") String clientId) {
        this.cartCalculationService = cartCalculationService;
        this.orderRepository = orderRepository;
        this.orderNumberService = orderNumberService;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
    }

    /**
     * 啟動新交易：產生最新的訂單編號並在 H2 建立一筆 DRAFT 狀態的空訂單。
     */
    @Transactional
    public String startNewTransaction(String orderType) {
        String orderId = orderNumberService.generateOrderNumber();
        
        LocalOrder order = new LocalOrder();
        order.setOrderId(orderId);
        order.setOrderType(orderType != null ? orderType : "RETAIL");
        order.setStatus("DRAFT");
        order.setOriginalTotal(0.0);
        order.setCartDiscountAmount(0.0);
        order.setFinalAmount(0.0);
        order.setRemark("新交易初始化");
        
        orderRepository.save(order);
        return orderId;
    }

    /**
     * 將當前購物車明細寫入已有的草稿訂單，準備進入付款階段。
     */
    @Transactional
    public void prepareCheckout(String orderId, CalculationRequest request) {
        LocalOrder order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到指定的訂單草稿: " + orderId));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("只有草稿狀態 (DRAFT) 的訂單才能更新商品明細準備結帳");
        }

        // 1. 重新執行金額與促銷優惠計算
        CalculationResponse calculatedCart = cartCalculationService.calculateCart(request);

        // 2. 清除舊明細以重新填入最新掃描結果
        order.getItems().clear();
        order.getPromotions().clear();

        // 3. 設定新金額與會員資訊
        order.setMemberId(request.getMemberId());
        order.setOriginalTotal(calculatedCart.getOriginalTotal());
        order.setCartDiscountAmount(calculatedCart.getCartDiscountAmount());
        order.setFinalAmount(calculatedCart.getFinalAmount());
        order.setRemark("購物車商品確認，進入結帳付款程序");

        // 4. 寫入訂單商品明細
        if (calculatedCart.getItems() != null) {
            for (CalculatedItemDto itemDto : calculatedCart.getItems()) {
                LocalOrderItem orderItem = new LocalOrderItem();
                orderItem.setItemId(itemDto.getItemId());
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setOriginalUnitPrice(itemDto.getOriginalUnitPrice());
                orderItem.setFinalUnitPrice(itemDto.getFinalUnitPrice());
                orderItem.setDiscountAmount(itemDto.getDiscountAmount());
                orderItem.setAppliedPromoId(itemDto.getAppliedPromoId());
                orderItem.setSubtotal(itemDto.getSubtotal());

                order.addItem(orderItem);
            }
        }

        // 5. 寫入促銷套用紀錄
        if (calculatedCart.getAppliedPromotions() != null) {
            for (AppliedPromotionDto promoDto : calculatedCart.getAppliedPromotions()) {
                LocalOrderPromotion orderPromo = new LocalOrderPromotion();
                orderPromo.setPromoId(promoDto.getPromoId());
                orderPromo.setDiscountAmount(promoDto.getDiscountAmount());

                order.addPromotion(orderPromo);
            }
        }

        orderRepository.save(order);
    }

    /**
     * 購物車輸入商品階段中途取消交易：
     * 將當前購物車內容計算後，建立或更新狀態為 CANCELLED 的訂單，記錄商品明細與會員資訊，並寫入 Outbox 備份上傳至主機。
     */
    @Transactional
    public String abandonCart(String orderId, CalculationRequest request) {
        LocalOrder order;
        if (orderId != null && !orderId.trim().isEmpty()) {
            order = orderRepository.findByOrderId(orderId)
                    .orElseGet(() -> {
                        LocalOrder newOrder = new LocalOrder();
                        newOrder.setOrderId(orderId);
                        return newOrder;
                    });
        } else {
            order = new LocalOrder();
            order.setOrderId(orderNumberService.generateOrderNumber());
        }

        // 1. 執行計算
        CalculationResponse calculatedCart = cartCalculationService.calculateCart(request);

        // 2. 清除原有明細
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        } else {
            order.getItems().clear();
        }
        if (order.getPromotions() == null) {
            order.setPromotions(new ArrayList<>());
        } else {
            order.getPromotions().clear();
        }

        // 3. 設定訂單狀態為已取消 (CANCELLED)
        order.setOrderType("RETAIL");
        order.setStatus("CANCELLED"); 
        order.setMemberId(request.getMemberId());
        order.setOriginalTotal(calculatedCart.getOriginalTotal());
        order.setCartDiscountAmount(calculatedCart.getCartDiscountAmount());
        order.setFinalAmount(calculatedCart.getFinalAmount());
        order.setRemark("購物車階段中途取消交易 (稽核紀錄)");

        // 4. 寫入訂單商品明細
        if (calculatedCart.getItems() != null) {
            for (CalculatedItemDto itemDto : calculatedCart.getItems()) {
                LocalOrderItem orderItem = new LocalOrderItem();
                orderItem.setItemId(itemDto.getItemId());
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setOriginalUnitPrice(itemDto.getOriginalUnitPrice());
                orderItem.setFinalUnitPrice(itemDto.getFinalUnitPrice());
                orderItem.setDiscountAmount(itemDto.getDiscountAmount());
                orderItem.setAppliedPromoId(itemDto.getAppliedPromoId());
                orderItem.setSubtotal(itemDto.getSubtotal());

                order.addItem(orderItem); 
            }
        }

        // 5. 寫入訂單套用的促銷紀錄
        if (calculatedCart.getAppliedPromotions() != null) {
            for (AppliedPromotionDto promoDto : calculatedCart.getAppliedPromotions()) {
                LocalOrderPromotion orderPromo = new LocalOrderPromotion();
                orderPromo.setPromoId(promoDto.getPromoId());
                orderPromo.setDiscountAmount(promoDto.getDiscountAmount());

                order.addPromotion(orderPromo); 
            }
        }

        // 6. 存入 H2 資料庫
        orderRepository.save(order);

        // 7. 寫入 Outbox 異步上傳主機備查
        saveOutboxEvent(order);

        return order.getOrderId();
    }

    /**
     * 將交易資訊寫入 Outbox 準備送往主機
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


            // 1. 商品明細
            List<TransactionUploadMessage.ItemDto> items = new ArrayList<>();
            if (order.getItems() != null) {
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
            }
            msg.setItems(items);

            // 2. 付款明細
            List<TransactionUploadMessage.PaymentDto> payments = new ArrayList<>();
            if (order.getPayments() != null) {
                for (com.pos.client.domain.entity.LocalOrderPayment payment : order.getPayments()) {
                    TransactionUploadMessage.PaymentDto paymentDto = new TransactionUploadMessage.PaymentDto();
                    paymentDto.setPaymentMethodCode(payment.getPaymentMethodId());
                    paymentDto.setAmount(BigDecimal.valueOf(payment.getAmount()));
                    paymentDto.setPayAmount(BigDecimal.valueOf(payment.getPayAmount()));
                    paymentDto.setChangeAmount(BigDecimal.valueOf(payment.getChangeAmount()));
                    payments.add(paymentDto);
                }
            }
            msg.setPayments(payments);

            // 3. 優惠折抵明細
            List<TransactionUploadMessage.DiscountDto> discounts = new ArrayList<>();
            if (order.getPromotions() != null) {
                for (LocalOrderPromotion promo : order.getPromotions()) {
                    TransactionUploadMessage.DiscountDto discountDto = new TransactionUploadMessage.DiscountDto();
                    discountDto.setDiscountCode(promo.getPromoId());
                    discountDto.setAmount(BigDecimal.valueOf(promo.getDiscountAmount()));
                    discounts.add(discountDto);
                }
            }
            msg.setDiscounts(discounts);

            String jsonPayload = objectMapper.writeValueAsString(msg);

            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(order.getOrderId());
            event.setEventType("TRANSACTION_CANCELLED");
            event.setPayload(jsonPayload);

            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("寫入交易發件箱 (Outbox) 失敗: " + e.getMessage(), e);
        }
    }
}
