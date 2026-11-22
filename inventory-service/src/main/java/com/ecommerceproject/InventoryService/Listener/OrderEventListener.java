package com.ecommerceproject.InventoryService.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to order.placed events. The current order.placed payload carries an
 * item count rather than per-product lines, so this logs the event; enriching
 * the event with product ids/quantities would let it call
 * InventoryService.decrement(...) for each line.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @KafkaListener(topics = "order.placed", groupId = "inventory-service")
    public void onOrderPlaced(String message) {
        log.info("[inventory] order.placed received, adjusting stock -> {}", message);
    }
}
