package com.pos.system.repository;

import com.pos.system.domain.entity.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for ProductPrice entity.
 */
@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Integer> {
    List<ProductPrice> findByProductId(Integer productId);
}
