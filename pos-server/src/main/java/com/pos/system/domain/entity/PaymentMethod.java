package com.pos.system.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * 9. 付款方式表 Payment_Method
 */
@Entity
@Table(name = "payment_method")
@Getter
@Setter
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "description", length = 100)
    private String description;

    public PaymentMethod() {
    }
}
