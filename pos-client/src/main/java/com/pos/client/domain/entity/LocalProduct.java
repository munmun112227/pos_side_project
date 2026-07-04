package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 本地商品快取表
 */
@Entity
@Table(name = "local_products")
@Getter
@Setter
public class LocalProduct {

    @Id
    @Column(name = "item_id", length = 50, nullable = false)
    private String itemId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "original_price", nullable = false)
    private Double originalPrice;

    @Column(name = "promotional_price")
    private Double promotionalPrice;

    @Column(name = "promo_start")
    private LocalDateTime promoStart;

    @Column(name = "promo_end")
    private LocalDateTime promoEnd;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "category_name", length = 50)
    private String categoryName;

    public LocalProduct() {
    }
}
