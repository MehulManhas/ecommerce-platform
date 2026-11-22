package com.ecommerceproject.ProductService.Messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes order.placed events. For now it just logs them; a real implementation
 * might decrement stock or update product metrics. Demonstrates the asynchronous,
 * event-driven link between the Order Service and the Product Service.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @KafkaListener(topics = "order.placed", groupId = "product-service")
    public void onOrderPlaced(String message) {
        log.info("[product-service] received order.placed event: {}", message);
    }
}
