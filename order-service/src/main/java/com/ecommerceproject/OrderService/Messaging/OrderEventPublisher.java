package com.ecommerceproject.OrderService.Messaging;

import com.ecommerceproject.OrderService.Event.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes order lifecycle events to Kafka. Publishing never breaks the order
 * flow: a broker that is down is logged and swallowed.
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    public static final String ORDER_PLACED_TOPIC = "order.placed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ORDER_PLACED_TOPIC, String.valueOf(event.getOrderId()), payload);
            log.info("Published {} event for order {}", ORDER_PLACED_TOPIC, event.getOrderId());
        } catch (Exception ex) {
            log.warn("Failed to publish {} event: {}", ORDER_PLACED_TOPIC, ex.getMessage());
        }
    }
}
