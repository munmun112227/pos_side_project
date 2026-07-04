package com.pos.system.repository;

import com.pos.system.domain.entity.Pos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for Pos entity.
 */
@Repository
public interface PosRepository extends JpaRepository<Pos, Integer> {
    Optional<Pos> findByCode(String code);
}
