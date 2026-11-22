package com.ecommerceproject.OrderService.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequestDto {
    private List<OrderItemRequestDto> items = new ArrayList<>();
}
