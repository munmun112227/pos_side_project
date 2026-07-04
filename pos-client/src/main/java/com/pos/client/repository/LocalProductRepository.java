package com.pos.client.repository;

import com.pos.client.domain.entity.LocalProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LocalProduct entity.
 */
@Repository
public interface LocalProductRepository extends JpaRepository<LocalProduct, String> {

    /**
     * 依據商品 ID 查詢商品。
     */
    Optional<LocalProduct> findByItemId(String itemId);

    /**
     * 查詢所有上架販售中的商品。
     */
    List<LocalProduct> findByIsActiveTrue();

    /**
     * 模糊搜尋商品名稱或商品 ID (限制回傳筆數以防資料庫負荷)
     */
    @Query("SELECT p FROM LocalProduct p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.itemId) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<LocalProduct> searchProducts(@Param("query") String query, Pageable pageable);
}
