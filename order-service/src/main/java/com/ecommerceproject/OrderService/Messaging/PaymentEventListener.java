package com.ecommerceproject.OrderService.Messaging;

import com.ecommerceproject.OrderService.Model.OrderStatus;
import com.ecommerceproject.OrderService.Repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Closes the order loop: when the Payment Service reports a completed payment,
 * the corresponding order is marked PAID.
 */
@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public PaymentEventListener(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void onPaymentCompleted(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            UUID orderId = UUID.fromString(node.get("orderId").asText());
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                log.info("[order] marked order {} as PAID", orderId);
            });
        } catch (Exception ex) {
            log.warn("Failed to process payment.completed: {}", ex.getMessage());
        }
    }
}
