package com.ecommerceproject.OrderService.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Subset of the Product Service response we care about when pricing an order.
 * Unknown fields are ignored so the two services can evolve independently.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {
    private UUID id;
    private String title;
    private double price;
}
