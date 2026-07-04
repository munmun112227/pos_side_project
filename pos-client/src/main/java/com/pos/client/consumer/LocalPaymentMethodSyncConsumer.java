package com.pos.client.consumer;

import com.pos.client.domain.entity.LocalPaymentMethod;
import com.pos.client.dto.PaymentMethodSyncMessage;
import com.pos.client.repository.LocalPaymentMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalPaymentMethodSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(LocalPaymentMethodSyncConsumer.class);

    @Autowired
    private LocalPaymentMethodRepository localPaymentMethodRepository;

    /**
     * Listens to the client-specific queue bound to the payment method sync Fanout Exchange.
     * Receives batch payment method updates and synchronizes them into the local H2 database.
     */
    @RabbitListener(queues = "#{clientPaymentMethodSyncQueue.name}")
    public void handlePaymentMethodSync(List<PaymentMethodSyncMessage> syncMessages) {
        if (syncMessages == null || syncMessages.isEmpty()) {
            return;
        }

        log.info("Received {} payment method updates from RabbitMQ.", syncMessages.size());

        for (PaymentMethodSyncMessage msg : syncMessages) {
            LocalPaymentMethod localPaymentMethod = localPaymentMethodRepository.findById(msg.getCode())
                    .orElse(new LocalPaymentMethod());

            localPaymentMethod.setCode(msg.getCode());
            localPaymentMethod.setDescription(msg.getDescription());

            localPaymentMethodRepository.save(localPaymentMethod);
        }
        log.info("Local H2 database successfully updated with payment method sync data.");
    }
}
