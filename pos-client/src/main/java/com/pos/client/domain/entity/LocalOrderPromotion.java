package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 本地訂單優惠明細表
 */
@Entity
@Table(name = "local_order_promotions")
@Getter
@Setter
public class LocalOrderPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalOrder order;


    @Column(name = "promo_id", length = 50, nullable = false)
    private String promoId;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "applied_at", insertable = false, updatable = false)
    private LocalDateTime appliedAt;

    public LocalOrderPromotion() {
    }
}
