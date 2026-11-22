package com.ecommerceproject.OrderService.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderResponseDto {
    private UUID id;
    private Long userId;
    private String status;
    private double totalAmount;
    private List<OrderItemResponseDto> items = new ArrayList<>();
}
