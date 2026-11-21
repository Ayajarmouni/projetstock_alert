package com.stockalert.approvisionnement.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ côté producteur. Elle est déclarée À L'IDENTIQUE dans service-notification : les déclarations
 * sont idempotentes, et la file existe donc dès que l'un des deux services a démarré — les messages publiés pendant
 * que le consommateur est arrêté sont conservés (file durable) au lieu d'être perdus.
 * <pre>
 *   stockalert.events (topic) --purchase-order.*--> notification.purchase-order --(rejet/échecs)--> stockalert.events.dlx --> notification.purchase-order.dlq
 * </pre>
 */
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "stockalert.events";
    public static final String ROUTING_PREFIX = "purchase-order.";
    public static final String QUEUE = "notification.purchase-order";
    public static final String DLX = "stockalert.events.dlx";
    public static final String DLQ = "notification.purchase-order.dlq";
    public static final String DLQ_KEY = "notification.purchase-order.dead";

    @Bean
    TopicExchange evenementsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE).deadLetterExchange(DLX).deadLetterRoutingKey(DLQ_KEY).build();
    }

    @Bean
    Queue notificationDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange evenementsExchange) {
        return BindingBuilder.bind(notificationQueue).to(evenementsExchange).with(ROUTING_PREFIX + "*");
    }

    @Bean
    Binding deadLetterBinding(Queue notificationDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationDeadLetterQueue).to(deadLetterExchange).with(DLQ_KEY);
    }

    /** Les messages sont sérialisés en JSON (content-type application/json). */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
