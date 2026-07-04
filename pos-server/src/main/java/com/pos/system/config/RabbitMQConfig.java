package com.pos.system.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PRODUCT_SYNC_EXCHANGE = "pos.product.sync.exchange";
    public static final String PAYMENT_METHOD_SYNC_EXCHANGE = "pos.payment-method.sync.exchange";
    public static final String TRANSACTION_UPLOAD_QUEUE = "pos.transaction.upload.queue";

    @Bean
    public FanoutExchange productSyncExchange() {
        // durable=true, autoDelete=false
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
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
