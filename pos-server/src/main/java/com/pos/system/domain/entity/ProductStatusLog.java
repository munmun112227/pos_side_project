package com.pos.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

import com.pos.system.domain.enums.ProductStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 5. 商品狀態/庫存變動紀錄表 Product_Status_Log
 */
@Entity
@Table(name = "product_status_log")
@Getter
@Setter
public class ProductStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_code", nullable = false, length = 50)
    private ProductStatus statusCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ProductStatusLog() {
    }
}
