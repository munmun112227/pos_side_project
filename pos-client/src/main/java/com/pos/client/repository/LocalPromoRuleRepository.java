package com.pos.client.repository;

import com.pos.client.domain.entity.LocalPromoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for LocalPromoRule entity.
 */
@Repository
public interface LocalPromoRuleRepository extends JpaRepository<LocalPromoRule, String> {
}
