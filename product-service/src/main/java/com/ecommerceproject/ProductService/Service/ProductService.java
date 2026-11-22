package com.ecommerceproject.ProductService.Service;

import com.ecommerceproject.ProductService.Dto.ProductListResponseDto;
import com.ecommerceproject.ProductService.Dto.ProductRequestDto;
import com.ecommerceproject.ProductService.Dto.ProductResponseDto;
import com.ecommerceproject.ProductService.Exception.ProductNotFoundException;
import com.ecommerceproject.ProductService.Model.Product;

import java.util.UUID;

public interface ProductService {
    ProductListResponseDto getAllProducts() throws ProductNotFoundException;
    ProductResponseDto getProductById(UUID productId) throws ProductNotFoundException;
    ProductResponseDto createProduct(ProductRequestDto newProduct);
    boolean deleteProduct(UUID productId);
    Product updateProduct(UUID productId, Product updatedProduct) throws ProductNotFoundException;

    ProductResponseDto findProductByTitle(String title) throws ProductNotFoundException;
}
