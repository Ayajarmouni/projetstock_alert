package com.stockalert.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ côté consommateur — identique à celle de service-approvisionnement (déclarations idempotentes).
 * <pre>
 *   stockalert.events (topic) --purchase-order.*--> notification.purchase-order --(rejet/échecs)--> stockalert.events.dlx --> notification.purchase-order.dlq
 * </pre>
 */
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "stockalert.events";
    public static final String ROUTING_PATTERN = "purchase-order.*";
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
        return BindingBuilder.bind(notificationQueue).to(evenementsExchange).with(ROUTING_PATTERN);
    }

    @Bean
    Binding deadLetterBinding(Queue notificationDeadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(notificationDeadLetterQueue).to(deadLetterExchange).with(DLQ_KEY);
    }

    /**
     * Désérialisation JSON. Le type cible est déduit du paramètre de la méthode {@code @RabbitListener} (INFERRED) et
     * non de l'en-tête {@code __TypeId__} écrit par le producteur (qui contient le nom d'une classe de l'AUTRE service).
     */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
