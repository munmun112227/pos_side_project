package com.pos.system.schedule;

import com.pos.system.service.PosItemListDailyUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Unified scheduler on the server side to trigger sync jobs.
 * Cron expressions are retrieved from application.properties.
 */
@Component
public class ServerSchedule {

    private static final Logger log = LoggerFactory.getLogger(ServerSchedule.class);

    @Autowired
    private PosItemListDailyUpdateService dailyUpdateService;

    /**
     * Triggers the daily product updates synchronization.
     */
    @Scheduled(cron = "${pos.schedule.product-sync:0 0 2 * * ?}")
    public void runProductSync() {
        log.info("Scheduler: Starting scheduled product sync job.");
        dailyUpdateService.broadcastDailyProductUpdates();
    }

    /**
     * Triggers the daily payment method updates synchronization.
     */
    @Scheduled(cron = "${pos.schedule.payment-method-sync:0 30 2 * * ?}")
    public void runPaymentMethodSync() {
        log.info("Scheduler: Starting scheduled payment method sync job.");
        dailyUpdateService.broadcastDailyPaymentMethodUpdates();
    }
}
