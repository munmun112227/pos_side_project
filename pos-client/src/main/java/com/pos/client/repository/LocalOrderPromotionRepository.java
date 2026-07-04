package com.pos.client.repository;

import com.pos.client.domain.entity.LocalOrderPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for LocalOrderPromotion entity.
 */
@Repository
public interface LocalOrderPromotionRepository extends JpaRepository<LocalOrderPromotion, Long> {

    /**
     * 依據訂單 ID 查詢所有的優惠明細。
     */
    List<LocalOrderPromotion> findByOrderOrderId(String orderId);
}
