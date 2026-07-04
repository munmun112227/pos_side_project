package com.pos.client.service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.H2SequenceMaxValueIncrementer;
import org.springframework.stereotype.Service;

@Service
public class AdvancedOrderNumberService {
  private static final Logger log = LoggerFactory.getLogger(AdvancedOrderNumberService.class);
  @Autowired
  private JdbcTemplate jdbcTemplate;
  private DataFieldMaxValueIncrementer sequenceIncrementer;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  @Value("${pos.number.prefix}")
  private String posNumberPrefix;

  @PostConstruct
  public void init() {
    // 確保本機 H2 資料庫中存在 order_seq 序列 (因為 Hibernate ddl-auto=update 無法自動建立非 Entity 的 Sequence)
    try {
      jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS order_seq START WITH 1 INCREMENT BY 1");
      log.info("Sequence 'order_seq' initialized successfully.");
    } catch (Exception e) {
      log.error("Failed to initialize sequence 'order_seq'", e);
    }
    sequenceIncrementer = new H2SequenceMaxValueIncrementer(jdbcTemplate.getDataSource(), "order_seq");
  }


  public String generateOrderNumber() {
    String orderDate = LocalDate.now().format(DATE_FORMATTER);
    // 透過介面直接取得下一個 Long 數值
    Long sequenceVal = sequenceIncrementer.nextLongValue();

    // 後續組合訂單編號的邏輯跟之前一樣
    String paddedSequence = String.format("%05d", sequenceVal);
    return posNumberPrefix + orderDate + paddedSequence;
  }

  public void resetSequence() {
    log.info("Resetting sequence");
    try {
      String sql = "ALTER SEQUENCE order_seq RESTART WITH 1";
      jdbcTemplate.execute(sql);
      log.info("Sequence reset successfully");
    } catch (Exception e) {
      log.error("Error resetting sequence", e);
    }
  }

}
