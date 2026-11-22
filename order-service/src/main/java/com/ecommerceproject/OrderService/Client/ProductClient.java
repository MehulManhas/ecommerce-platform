package com.ecommerceproject.OrderService.Client;

import com.ecommerceproject.OrderService.Dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * Synchronous lookup of a product (for pricing) from the Product Service.
 * Returns null when the product cannot be resolved.
 */
@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${productservice.base-url:http://localhost:9090}") String productServiceBaseUrl) {
        this.restClient = RestClient.create(productServiceBaseUrl);
    }

    public ProductDto getProduct(UUID productId) {
        try {
            return restClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductDto.class);
        } catch (RestClientException ex) {
            return null;
        }
    }
}
