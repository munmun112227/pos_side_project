package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * 本地款別 (付款方式) 快取表
 */
@Entity
@Table(name = "local_payment_methods")
@Getter
@Setter
public class LocalPaymentMethod {

    @Id
    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "description", length = 100)
    private String description;

    public LocalPaymentMethod() {
    }
}
