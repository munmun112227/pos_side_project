package com.pos.client.repository;

import com.pos.client.domain.entity.LocalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for LocalOrder entity.
 */
@Repository
public interface LocalOrderRepository extends JpaRepository<LocalOrder, String> {

    /**
     * 依據訂單 ID 查詢訂單。
     */
    Optional<LocalOrder> findByOrderId(String orderId);
}
