package com.ecommerceproject.PaymentService.Messaging;

import com.ecommerceproject.PaymentService.Model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    public static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentCompleted(Payment payment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("paymentId", payment.getId());
            event.put("orderId", payment.getOrderId());
            event.put("amount", payment.getAmount());
            event.put("status", payment.getStatus().name());

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, String.valueOf(payment.getOrderId()), payload);
            log.info("Published {} for order {}", PAYMENT_COMPLETED_TOPIC, payment.getOrderId());
        } catch (Exception ex) {
            log.warn("Failed to publish {}: {}", PAYMENT_COMPLETED_TOPIC, ex.getMessage());
        }
    }
}
