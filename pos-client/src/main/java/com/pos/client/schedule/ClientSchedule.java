package com.pos.client.schedule;

import com.pos.client.service.AdvancedOrderNumberService;
import com.pos.client.service.CleanOutDatedTxDataService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ClientSchedule {
  private final CleanOutDatedTxDataService cleanOutDatedTxDataService;
  private final AdvancedOrderNumberService advancedOrderNumberService;

  @Scheduled(cron = "${pos.client.reset-sequence}")
  public void resetOrderNumber () {
    advancedOrderNumberService.resetSequence();
  }
}
