package com.pos.system.repository;

import com.pos.system.domain.entity.TransactionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for TransactionPayment entity.
 */
@Repository
public interface TransactionPaymentRepository extends JpaRepository<TransactionPayment, Integer> {
    List<TransactionPayment> findByTransactionId(Integer transactionId);
}
