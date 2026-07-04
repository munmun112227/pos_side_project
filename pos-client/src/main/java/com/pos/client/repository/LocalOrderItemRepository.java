package com.pos.client.repository;

import com.pos.client.domain.entity.LocalOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for LocalOrderItem entity.
 */
@Repository
public interface LocalOrderItemRepository extends JpaRepository<LocalOrderItem, Long> {
}
