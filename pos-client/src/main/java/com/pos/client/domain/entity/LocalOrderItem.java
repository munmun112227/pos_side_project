package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * 本地訂單明細表
 */
@Entity
@Table(name = "local_order_items")
@Getter
@Setter
public class LocalOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalOrder order;


    @Column(name = "item_id", length = 50)
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "original_unit_price", nullable = false)
    private Double originalUnitPrice;

    @Column(name = "final_unit_price", nullable = false)
    private Double finalUnitPrice;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "applied_promo_id", length = 50)
    private String appliedPromoId;

    @Column(name = "subtotal", nullable = false)
    private Double subtotal;

    public LocalOrderItem() {
    }
}
