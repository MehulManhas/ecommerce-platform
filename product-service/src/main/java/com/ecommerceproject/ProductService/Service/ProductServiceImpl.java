package com.ecommerceproject.ProductService.Service;

import com.ecommerceproject.ProductService.Dto.ProductListResponseDto;
import com.ecommerceproject.ProductService.Dto.ProductRequestDto;
import com.ecommerceproject.ProductService.Dto.ProductResponseDto;
import com.ecommerceproject.ProductService.Exception.InvalidTitleException;
import com.ecommerceproject.ProductService.Exception.ProductNotFoundException;
import com.ecommerceproject.ProductService.Mapper.ProductMapper;
import com.ecommerceproject.ProductService.Model.Category;
import com.ecommerceproject.ProductService.Model.Price;
import com.ecommerceproject.ProductService.Model.Product;
import com.ecommerceproject.ProductService.Repository.CategoryRepository;
import com.ecommerceproject.ProductService.Repository.PriceRepository;
import com.ecommerceproject.ProductService.Repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("productService")
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PriceRepository priceRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              PriceRepository priceRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.priceRepository = priceRepository;
    }

    @Override
    public ProductListResponseDto getAllProducts() {
        List<Product> products = productRepository.findAll();
        return ProductMapper.productListToProductResponseDto(products);
    }

    @Override
    public ProductResponseDto getProductById(UUID productId) throws ProductNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + productId));
        return ProductMapper.productToProductResponseDto(product);
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto newProduct) {
        if (newProduct.getTitle() == null || newProduct.getTitle().isBlank()) {
            throw new InvalidTitleException("title is invalid");
        }

        Category category = resolveCategory(newProduct.getCategory());

        Price price = new Price();
        price.setCurrency("INR");
        price.setAmount(newProduct.getPrice());
        price.setDiscount(0);
        price = priceRepository.save(price);

        Product product = new Product();
        product.setTitle(newProduct.getTitle());
        product.setDescription(newProduct.getDescription());
        product.setImage(newProduct.getImage());
        product.setCategory(category);
        product.setPrice(price);

        Product saved = productRepository.save(product);
        return ProductMapper.productToProductResponseDto(saved);
    }

    @Override
    public boolean deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            return false;
        }
        productRepository.deleteById(productId);
        return true;
    }

    @Override
    public Product updateProduct(UUID productId, Product updatedProduct) throws ProductNotFoundException {
        Product existing = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id : " + productId));

        if (updatedProduct.getTitle() != null) {
            existing.setTitle(updatedProduct.getTitle());
        }
        if (updatedProduct.getDescription() != null) {
            existing.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getImage() != null) {
            existing.setImage(updatedProduct.getImage());
        }
        if (updatedProduct.getCategory() != null) {
            existing.setCategory(updatedProduct.getCategory());
        }
        if (updatedProduct.getPrice() != null) {
            existing.setPrice(updatedProduct.getPrice());
        }

        return productRepository.save(existing);
    }

    @Override
    public ProductResponseDto findProductByTitle(String title) throws ProductNotFoundException {
        if (title == null || title.isEmpty()) {
            throw new InvalidTitleException("title is invalid");
        }
        Product product = productRepository.findByTitle(title);

        if (product == null) {
            throw new ProductNotFoundException("Product with given title is not available");
        }

        return ProductMapper.productToProductResponseDto(product);
    }

    private Category resolveCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        Optional<Category> existing = categoryRepository.findByCategoryName(categoryName);
        if (existing.isPresent()) {
            return existing.get();
        }
        Category category = new Category();
        category.setCategoryName(categoryName);
        return categoryRepository.save(category);
    }
}
