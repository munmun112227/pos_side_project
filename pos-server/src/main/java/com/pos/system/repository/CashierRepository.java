package com.pos.system.repository;

import com.pos.system.domain.entity.Cashier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for Cashier entity.
 */
@Repository
public interface CashierRepository extends JpaRepository<Cashier, Integer> {
    Optional<Cashier> findByCode(String code);
}
