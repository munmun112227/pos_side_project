package com.pos.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

import com.pos.system.domain.enums.DiscountType;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 10. 優惠活動表 Discount
 */
@Entity
@Table(name = "discount")
@Getter
@Setter
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private DiscountType type;

    @Column(name = "threshold", precision = 10, scale = 2)
    private BigDecimal threshold;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Discount() {
    }
}
