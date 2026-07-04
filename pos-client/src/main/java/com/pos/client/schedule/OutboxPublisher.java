package com.pos.client.schedule;

import com.pos.client.config.RabbitMQConfig;
import com.pos.client.domain.entity.OutboxEvent;
import com.pos.client.dto.TransactionUploadMessage;
import com.pos.client.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 交易發件箱 (Outbox) 發布器：定期輪詢本地 H2 資料庫中的未發送事件，並將其送往 RabbitMQ 隊列。
 * 保證即使本機斷網，交易資料依然安全暫存在 H2，待網路恢復後會自動重傳。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 每 5 秒執行一次，掃描並送出未處理的交易發件箱資料。
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByOrderByCreatedAtAsc();
        if (events.isEmpty()) {
            return;
        }

        log.info("掃描到發件箱中有 {} 筆待上傳交易，開始推送至 RabbitMQ...", events.size());

        for (OutboxEvent event : events) {
            try {
                // 1. 將 payload 反序列化為 TransactionUploadMessage 物件
                TransactionUploadMessage message = objectMapper.readValue(
                        event.getPayload(), TransactionUploadMessage.class);

                // 2. 推送至 RabbitMQ 佇列
                rabbitTemplate.convertAndSend(RabbitMQConfig.TRANSACTION_UPLOAD_QUEUE, message);
                log.info("交易單號 {} 成功推送至 RabbitMQ。", event.getAggregateId());

                // 3. 推送成功，將該事件自發件箱物理刪除 (保證 At-Least-Once)
                outboxEventRepository.delete(event);
            } catch (Exception e) {
                // 若推送失敗 (如 RabbitMQ 斷線)，記錄 Error 並中斷迴圈，下個週期再重試 (保留交易發生順序)
                log.error("交易單號 {} 上傳失敗，將於下個週期重傳。錯誤訊息: {}", event.getAggregateId(), e.getMessage());
                break;
            }
        }
    }
}
