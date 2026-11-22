package com.ecommerceproject.OrderService.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItemResponseDto {
    private UUID productId;
    private String productTitle;
    private int quantity;
    private double unitPrice;
}
