package com.pos.client.domain.entity;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地促銷主檔
 */
@Entity
@Table(name = "local_promotions")
@Getter
@Setter
public class LocalPromotion {

    @Id
    @Column(name = "promo_id", length = 50, nullable = false)
    private String promoId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "target_member_tier", length = 20)
    private String targetMemberTier;

    @Column(name = "is_stackable")
    private Boolean isStackable = false;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LocalPromoRule> rules = new ArrayList<>();

    public LocalPromotion() {
    }

    /**
     * Helper method to add a rule and maintain bidirectional integrity.
     */
    public void addRule(LocalPromoRule rule) {
        rules.add(rule);
        rule.setPromotion(this);
    }
}
