package com.ecommerceproject.NotificationService.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Turns platform events into notifications. A real implementation would send
 * email/SMS; here each handler logs the message it would dispatch.
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @KafkaListener(topics = "user.signup", groupId = "notification-service")
    public void onUserSignup(String message) {
        log.info("[notification] welcome email -> {}", message);
    }

    @KafkaListener(topics = "order.placed", groupId = "notification-service")
    public void onOrderPlaced(String message) {
        log.info("[notification] order confirmation -> {}", message);
    }
}
