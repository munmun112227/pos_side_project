package com.pos.system.repository;

import com.pos.system.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pos.system.repository.projection.ProductSyncProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findBySku(String sku);

    @Query("SELECT p.sku as sku, p.name as name, pp.price as price, c.name as categoryName " +
           "FROM Product p " +
           "LEFT JOIN ProductPrice pp ON pp.product = p " +
           "AND (pp.startDate IS NULL OR pp.startDate <= CURRENT_DATE) " +
           "AND (pp.endDate IS NULL OR pp.endDate >= CURRENT_DATE) " +
           "LEFT JOIN p.category c")
    List<ProductSyncProjection> findAllActiveProductsWithPrice();
}
