package com.ecommerceproject.ProductService.Service;

import com.ecommerceproject.ProductService.Client.FakeStoreAPIClient;
import com.ecommerceproject.ProductService.Dto.*;
import com.ecommerceproject.ProductService.Exception.ProductNotFoundException;
import com.ecommerceproject.ProductService.Model.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.ecommerceproject.ProductService.Mapper.ProductMapper.fakeStoreProductResponseToProductResponse;
import static com.ecommerceproject.ProductService.Mapper.ProductMapper.productRequestToFakeStoreProductRequest;
import static com.ecommerceproject.ProductService.Utils.ProductUtils.isNull;

@Service("fakeStoreProductService")
public class FakeStoreProductServiceImpl implements ProductService{

    private RestTemplateBuilder restTemplateBuilder;
    private FakeStoreAPIClient fakeStoreAPIClient;

    public FakeStoreProductServiceImpl(RestTemplateBuilder restTemplateBuilder, FakeStoreAPIClient fakeStoreAPIClient) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.fakeStoreAPIClient = fakeStoreAPIClient;
    }

    @Override
    public ProductListResponseDto getAllProducts() throws ProductNotFoundException {
        List<FakeStoreProductResponseDto> fakeStoreProductResponseDtoList = fakeStoreAPIClient.getAllProducts();

        if(isNull(fakeStoreProductResponseDtoList)){
            throw new ProductNotFoundException("Product not found");
        }

        ProductListResponseDto productResponseDtos = new ProductListResponseDto();

        for(FakeStoreProductResponseDto fakeStoreProductResponseDto : fakeStoreProductResponseDtoList){
            productResponseDtos.getProductList().add(fakeStoreProductResponseToProductResponse(fakeStoreProductResponseDto));
        }

        return productResponseDtos;
    }

    @Override
    public ProductResponseDto getProductById(UUID productId) throws ProductNotFoundException {
        // FakeStore API addresses products by integer id, which is incompatible
        // with the UUID-based API. Use the DB-backed "productService" instead.
        throw new UnsupportedOperationException(
                "FakeStore API uses integer ids; UUID lookup is not supported by this implementation");
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto newProduct) {
        String postProductURL = "https://fakestoreapi.com/products/";
        FakeStoreProductRequestDto fakeStoreProductRequestDto = productRequestToFakeStoreProductRequest(newProduct);
        FakeStoreProductResponseDto fakeStoreProductResponseDto = fakeStoreAPIClient.createProduct(fakeStoreProductRequestDto);

//        RestTemplate restTemplate = restTemplateBuilder.build();
//
//        ResponseEntity<ProductResponseDto> newProductResponse = restTemplate.postForEntity(postProductURL, newProduct, ProductResponseDto.class);

        return fakeStoreProductResponseToProductResponse(fakeStoreProductResponseDto);
    }

    @Override
    public boolean deleteProduct(UUID productId) {
        throw new UnsupportedOperationException(
                "FakeStore API uses integer ids; UUID delete is not supported by this implementation");
    }

    @Override
    public Product updateProduct(UUID productId, Product updatedProduct) throws ProductNotFoundException {
        throw new UnsupportedOperationException(
                "FakeStore API uses integer ids; UUID update is not supported by this implementation");
    }

    @Override
    public ProductResponseDto findProductByTitle(String title) {
        return null;
    }
}
