package com.ecommerceproject.ProductService.Dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductListResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ProductResponseDto> productList;

    public ProductListResponseDto(){
        this.productList = new ArrayList<>();
    }

}
