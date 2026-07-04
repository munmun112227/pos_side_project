package com.pos.client.repository;

import com.pos.client.domain.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for OutboxEvent entity.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 按建立時間遞增排序取得所有事件，用以確保事件依序處理。
     */
    List<OutboxEvent> findByOrderByCreatedAtAsc();
}
