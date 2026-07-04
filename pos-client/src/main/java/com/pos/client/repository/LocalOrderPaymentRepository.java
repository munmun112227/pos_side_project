package com.pos.client.repository;

import com.pos.client.domain.entity.LocalOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for LocalOrderPayment entity.
 */
@Repository
public interface LocalOrderPaymentRepository extends JpaRepository<LocalOrderPayment, Long> {

    /**
     * 依據訂單 ID 查詢所有的付款明細。
     */
    List<LocalOrderPayment> findByOrderOrderId(String orderId);
}
