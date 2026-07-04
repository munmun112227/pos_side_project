package com.pos.system.repository;

import com.pos.system.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for Member entity.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByCode(String code);
}
