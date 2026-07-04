package com.pos.client.repository;

import com.pos.client.domain.entity.LocalPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for LocalPaymentMethod entity.
 */
@Repository
public interface LocalPaymentMethodRepository extends JpaRepository<LocalPaymentMethod, String> {
}
