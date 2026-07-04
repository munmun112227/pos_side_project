package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

/**
 * 本地訂單付款明細表 (支援複合支付)
 */
@Entity
@Table(name = "local_order_payments")
@Getter
@Setter
public class LocalOrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private LocalOrder order;


    @Column(name = "payment_method_id", length = 50)
    private String paymentMethodId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "pay_amount", nullable = false)
    private Double payAmount;

    @Column(name = "change_amount")
    private Double changeAmount = 0.0;

    public LocalOrderPayment() {
    }
}
