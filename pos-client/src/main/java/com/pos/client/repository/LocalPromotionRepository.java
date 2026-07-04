package com.pos.client.repository;

import com.pos.client.domain.entity.LocalPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LocalPromotion entity.
 */
@Repository
public interface LocalPromotionRepository extends JpaRepository<LocalPromotion, String> {

    /**
     * 依據促銷 ID 查詢促銷。
     */
    Optional<LocalPromotion> findByPromoId(String promoId);

    /**
     * 按優先級順序（數值越小越優先）取得所有促銷。
     */
    List<LocalPromotion> findByOrderByPriorityAsc();
}
