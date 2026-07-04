package com.pos.system.repository;

import com.pos.system.domain.entity.ProductStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for ProductStatusLog entity.
 */
@Repository
public interface ProductStatusLogRepository extends JpaRepository<ProductStatusLog, Integer> {
    List<ProductStatusLog> findByProductId(Integer productId);
}
