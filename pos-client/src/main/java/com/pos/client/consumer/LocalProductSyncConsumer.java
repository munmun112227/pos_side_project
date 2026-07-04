package com.pos.client.consumer;

import com.pos.client.domain.entity.LocalProduct;
import com.pos.client.dto.ProductSyncMessage;
import com.pos.client.repository.LocalProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LocalProductSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(LocalProductSyncConsumer.class);

    @Autowired
    private LocalProductRepository localProductRepository;

    /**
     * Listens to the client-specific queue bound to the product sync Fanout Exchange.
     * Receives batch product updates and synchronizes them into the local H2 database.
     */
    @RabbitListener(queues = "#{clientProductSyncQueue.name}")
    public void handleProductSync(List<ProductSyncMessage> syncMessages) {
        if (syncMessages == null || syncMessages.isEmpty()) {
            return;
        }

        log.info("Received {} product updates from RabbitMQ.", syncMessages.size());

        for (ProductSyncMessage msg : syncMessages) {
            // Find existing product by SKU (itemId on the client side)
            LocalProduct localProduct = localProductRepository.findById(msg.getSku())
                    .orElse(new LocalProduct());

            localProduct.setItemId(msg.getSku());
            localProduct.setName(msg.getName());
            
            if (msg.getOriginalPrice() != null) {
                localProduct.setOriginalPrice(msg.getOriginalPrice().doubleValue());
            }
            
            localProduct.setCategoryName(msg.getCategoryName());
            localProduct.setIsActive(msg.getIsActive() != null ? msg.getIsActive() : true);

            localProductRepository.save(localProduct);
        }
        log.info("Local H2 database successfully updated with product sync data.");
    }
}
