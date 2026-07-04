package com.pos.system.service;

import com.pos.system.config.RabbitMQConfig;
import com.pos.system.dto.ProductSyncMessage;
import com.pos.system.domain.entity.PaymentMethod;
import com.pos.system.dto.PaymentMethodSyncMessage;
import com.pos.system.repository.PaymentMethodRepository;
import com.pos.system.repository.ProductRepository;
import com.pos.system.repository.projection.ProductSyncProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PosItemListDailyUpdateService {

    private static final Logger log = LoggerFactory.getLogger(PosItemListDailyUpdateService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void broadcastDailyProductUpdates() {
        // Query all products and active prices in 1 single database roundtrip
        List<ProductSyncProjection> activeProducts = productRepository.findAllActiveProductsWithPrice();
        List<ProductSyncMessage> syncMessages = new ArrayList<>();

        for (ProductSyncProjection projection : activeProducts) {
            BigDecimal price = projection.getPrice() != null ? projection.getPrice() : BigDecimal.ZERO;
            String categoryName = projection.getCategoryName() != null ? projection.getCategoryName() : "General";

            ProductSyncMessage message = new ProductSyncMessage(
                    projection.getSku(),
                    projection.getName(),
                    price,
                    categoryName,
                    true
            );
            syncMessages.add(message);
        }

        if (!syncMessages.isEmpty()) {
            // Broadcast the updates via RabbitMQ Fanout Exchange
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PRODUCT_SYNC_EXCHANGE,
                    "", // routing key is ignored by fanout exchanges
                    syncMessages
            );
            log.info("Broadcasted {} product updates to all clients via RabbitMQ (Optimized single-query).", syncMessages.size());
        }
    }

    public void broadcastDailyPaymentMethodUpdates() {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        List<PaymentMethodSyncMessage> syncMessages = new ArrayList<>();

        for (PaymentMethod pm : paymentMethods) {
            syncMessages.add(new PaymentMethodSyncMessage(pm.getCode(), pm.getDescription()));
        }

        if (!syncMessages.isEmpty()) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_METHOD_SYNC_EXCHANGE,
                    "", // routing key is ignored by fanout exchanges
                    syncMessages
            );
            log.info("Broadcasted {} payment method updates to all clients via RabbitMQ.", syncMessages.size());
        }
    }
}
