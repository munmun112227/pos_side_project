package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

/**
 * 本地促銷規則
 */
@Entity
@Table(name = "local_promo_rules")
@Getter
@Setter
public class LocalPromoRule {

    @Id
    @Column(name = "rule_id", length = 50, nullable = false)
    private String ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promo_id")
    private LocalPromotion promotion;

    @Column(name = "rule_type", length = 50)
    private String ruleType;

    @Column(name = "condition_payload", columnDefinition = "TEXT")
    private String conditionPayload;

    @Column(name = "action_payload", columnDefinition = "TEXT")
    private String actionPayload;

    public LocalPromoRule() {
    }
}
