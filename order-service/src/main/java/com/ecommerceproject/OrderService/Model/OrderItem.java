package com.ecommerceproject.OrderService.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem extends BaseModel {

    private UUID productId;

    private String productTitle;

    private int quantity;

    private double unitPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
