package com.ecommerceproject.OrderService.Event;

import java.util.UUID;

/**
 * Payload published to the "order.placed" topic. Plain POJO with getters so it
 * serialises cleanly to JSON.
 */
public class OrderPlacedEvent {
    private UUID orderId;
    private Long userId;
    private double totalAmount;
    private int itemCount;
    private long timestamp;

    public OrderPlacedEvent() {
    }

    public OrderPlacedEvent(UUID orderId, Long userId, double totalAmount, int itemCount, long timestamp) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.timestamp = timestamp;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
