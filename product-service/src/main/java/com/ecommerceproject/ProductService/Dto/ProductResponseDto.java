package com.ecommerceproject.ProductService.Dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class ProductResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String title;
    private double price;
    private String category;
    private  String description;
    private String image;
}
