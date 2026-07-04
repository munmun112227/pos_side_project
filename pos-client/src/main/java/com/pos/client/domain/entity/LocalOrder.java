package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地訂單主表
 */
@Entity
@Table(name = "local_orders")
@Getter
@Setter
public class LocalOrder {

    @Id
    @Column(name = "order_id", length = 50, nullable = false)
    private String orderId;

    @Column(name = "order_type", length = 50, nullable = false)
    private String orderType;

    @Column(name = "status", length = 50, nullable = false)
    private String status = "DRAFT"; // DRAFT (草稿/付款中), PAID (已付款/完成), CANCELLED (已取消)

    @Column(name = "member_id", length = 50)
    private String memberId;



    @Column(name = "original_total", nullable = false)
    private Double originalTotal;

    @Column(name = "cart_discount_amount")
    private Double cartDiscountAmount = 0.0;

    @Column(name = "final_amount", nullable = false)
    private Double finalAmount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LocalOrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LocalOrderPayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LocalOrderPromotion> promotions = new ArrayList<>();

    @Column(name = "remark", length = 255)
    private String remark;

    public LocalOrder() {
    }

    /**
     * Helper method to add an item and maintain bidirectional integrity.
     */
    public void addItem(LocalOrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to add a payment and maintain bidirectional integrity.
     */
    public void addPayment(LocalOrderPayment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }

    /**
     * Helper method to add a promotion and maintain bidirectional integrity.
     */
    public void addPromotion(LocalOrderPromotion promotion) {
        promotions.add(promotion);
        promotion.setOrder(this);
    }
}
