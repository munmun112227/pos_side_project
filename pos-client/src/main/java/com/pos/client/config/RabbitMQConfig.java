package com.pos.client.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PRODUCT_SYNC_EXCHANGE = "pos.product.sync.exchange";
    public static final String PAYMENT_METHOD_SYNC_EXCHANGE = "pos.payment-method.sync.exchange";
    public static final String TRANSACTION_UPLOAD_QUEUE = "pos.transaction.upload.queue";

    @Value("${pos.client.id}")
    private String clientId;

    @Bean
    public FanoutExchange productSyncExchange() {
        return new FanoutExchange(PRODUCT_SYNC_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange paymentMethodSyncExchange() {
        return new FanoutExchange(PAYMENT_METHOD_SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue transactionUploadQueue() {
        return new Queue(TRANSACTION_UPLOAD_QUEUE, true, false, false);
    }

    @Bean
    public Queue clientProductSyncQueue() {
        // Create a unique, durable queue for this specific client
        String queueName = "pos.client." + clientId + ".product-sync.queue";
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public Queue clientPaymentMethodSyncQueue() {
        // Create a unique, durable queue for payment method sync
        String queueName = "pos.client." + clientId + ".payment-method-sync.queue";
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public Binding productBinding(Queue clientProductSyncQueue, FanoutExchange productSyncExchange) {
        return BindingBuilder.bind(clientProductSyncQueue).to(productSyncExchange);
    }

    @Bean
    public Binding paymentMethodBinding(Queue clientPaymentMethodSyncQueue, FanoutExchange paymentMethodSyncExchange) {
        return BindingBuilder.bind(clientPaymentMethodSyncQueue).to(paymentMethodSyncExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
