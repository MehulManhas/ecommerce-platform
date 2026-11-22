package com.ecommerceproject.PaymentService.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentResponseDto {
    private UUID paymentId;
    private UUID orderId;
    private double amount;
    private String status;
}
