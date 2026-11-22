package com.ecommerceproject.UserService.Messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Publishes user lifecycle events to Kafka. Publishing never breaks signup:
 * a broker that is down is logged and swallowed.
 */
@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
    public static final String USER_SIGNUP_TOPIC = "user.signup";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishUserSignup(Long userId, String email) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("userId", userId);
            event.put("email", email);
            event.put("timestamp", System.currentTimeMillis());

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_SIGNUP_TOPIC, String.valueOf(userId), payload);
            log.info("Published {} event for user {}", USER_SIGNUP_TOPIC, userId);
        } catch (Exception ex) {
            log.warn("Failed to publish {} event: {}", USER_SIGNUP_TOPIC, ex.getMessage());
        }
    }
}
