package com.pos.system.repository;

import com.pos.system.domain.entity.TransactionDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for TransactionDiscount entity.
 */
@Repository
public interface TransactionDiscountRepository extends JpaRepository<TransactionDiscount, Integer> {
    List<TransactionDiscount> findByTransactionId(Integer transactionId);
}
