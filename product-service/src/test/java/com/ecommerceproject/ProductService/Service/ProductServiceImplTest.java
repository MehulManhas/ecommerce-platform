package com.ecommerceproject.ProductService.Service;

import com.ecommerceproject.ProductService.Dto.ProductRequestDto;
import com.ecommerceproject.ProductService.Dto.ProductResponseDto;
import com.ecommerceproject.ProductService.Exception.InvalidTitleException;
import com.ecommerceproject.ProductService.Exception.ProductNotFoundException;
import com.ecommerceproject.ProductService.Model.Category;
import com.ecommerceproject.ProductService.Model.Price;
import com.ecommerceproject.ProductService.Model.Product;
import com.ecommerceproject.ProductService.Repository.CategoryRepository;
import com.ecommerceproject.ProductService.Repository.PriceRepository;
import com.ecommerceproject.ProductService.Repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PriceRepository priceRepository;
    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProductByIdReturnsProduct() throws ProductNotFoundException {
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, "iPhone", 100.0, "Electronics");
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ProductResponseDto dto = productService.getProductById(id);

        Assertions.assertEquals(id, dto.getId());
        Assertions.assertEquals("iPhone", dto.getTitle());
        Assertions.assertEquals(100.0, dto.getPrice());
        Assertions.assertEquals("Electronics", dto.getCategory());
    }

    @Test
    void getProductByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(ProductNotFoundException.class, () -> productService.getProductById(id));
    }

    @Test
    void createProductPersistsAndReturnsDto() {
        ProductRequestDto request = new ProductRequestDto();
        request.setTitle("Macbook");
        request.setPrice(200.0);
        request.setCategory("Electronics");
        request.setDescription("laptop");
        request.setImage("img");

        when(categoryRepository.findByCategoryName("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        when(priceRepository.save(any(Price.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDto dto = productService.createProduct(request);

        Assertions.assertEquals("Macbook", dto.getTitle());
        Assertions.assertEquals(200.0, dto.getPrice());
        Assertions.assertEquals("Electronics", dto.getCategory());
    }

    @Test
    void createProductThrowsOnBlankTitle() {
        ProductRequestDto request = new ProductRequestDto();
        request.setTitle("");
        Assertions.assertThrows(InvalidTitleException.class, () -> productService.createProduct(request));
    }

    @Test
    void deleteProductReturnsTrueWhenPresent() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        Assertions.assertTrue(productService.deleteProduct(id));
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProductReturnsFalseWhenMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);
        Assertions.assertFalse(productService.deleteProduct(id));
    }

    @Test
    void updateProductAppliesNonNullFields() throws ProductNotFoundException {
        UUID id = UUID.randomUUID();
        Product existing = buildProduct(id, "old title", 10.0, "Electronics");
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product patch = new Product();
        patch.setTitle("new title");

        Product result = productService.updateProduct(id, patch);

        Assertions.assertEquals("new title", result.getTitle());
        Assertions.assertNotEquals("old title", result.getTitle());
    }

    @Test
    void testFindProductByTitleSuccess() throws ProductNotFoundException {
        Price mockPrice = new Price();
        mockPrice.setAmount(100);

        Category mockCategory = new Category();
        mockCategory.setCategoryName("mockCategory");

        String testTitle = "testTitle";
        Product mockProduct = new Product();
        mockProduct.setId(UUID.randomUUID());
        mockProduct.setTitle(testTitle);
        mockProduct.setDescription("testDescription");
        mockProduct.setPrice(mockPrice);
        mockProduct.setCategory(mockCategory);

        when(productRepository.findByTitle(testTitle)).thenReturn(mockProduct);

        ProductResponseDto actualProductResponseDto = productService.findProductByTitle(testTitle);

        Assertions.assertEquals(actualProductResponseDto.getTitle(), mockProduct.getTitle());
        Assertions.assertEquals(actualProductResponseDto.getId(), mockProduct.getId());
        Assertions.assertEquals(actualProductResponseDto.getPrice(), mockProduct.getPrice().getAmount());
        Assertions.assertEquals(actualProductResponseDto.getDescription(), mockProduct.getDescription());
    }

    @Test
    public void testFindByProductByTitleRepoRespondsWithNullObject() {
        String testTitle = "testProductTitle";
        when(productRepository.findByTitle(testTitle)).thenReturn(null);
        Assertions.assertThrows(ProductNotFoundException.class, () -> productService.findProductByTitle(testTitle));
    }

    @Test
    public void testFindProductByTitle_NullTitle() {
        String testTitle = null;
        Assertions.assertThrows(InvalidTitleException.class, () -> productService.findProductByTitle(testTitle));
    }

    private Product buildProduct(UUID id, String title, double amount, String categoryName) {
        Price price = new Price();
        price.setAmount(amount);

        Category category = new Category();
        category.setCategoryName(categoryName);

        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setDescription("desc");
        product.setImage("img");
        product.setPrice(price);
        product.setCategory(category);
        return product;
    }
}
