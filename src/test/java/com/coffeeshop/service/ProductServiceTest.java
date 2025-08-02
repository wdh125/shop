package com.coffeeshop.service;

import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import com.coffeeshop.entity.Category;
import com.coffeeshop.entity.Product;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 * Tests product management including CRUD operations, validation, and filtering
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Product testProduct;
    private Category testCategory;
    private AdminProductRequestDTO testProductRequest;
    
    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Beverages");
        testCategory.setDescription("Hot and cold beverages");
        
        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("Espresso");
        testProduct.setDescription("Strong black coffee");
        testProduct.setPrice(new BigDecimal("50000"));
        testProduct.setImageUrl("espresso.jpg");
        testProduct.setIsAvailable(true);
        testProduct.setPreparationTime(5);
        testProduct.setDisplayOrder(1);
        testProduct.setCategory(testCategory);
        testProduct.setCreatedAt(LocalDateTime.now().minusDays(1));
        testProduct.setUpdatedAt(LocalDateTime.now());
        
        testProductRequest = new AdminProductRequestDTO();
        testProductRequest.setName("Cappuccino");
        testProductRequest.setDescription("Coffee with steamed milk");
        testProductRequest.setPrice(new BigDecimal("60000"));
        testProductRequest.setImageUrl("cappuccino.jpg");
        testProductRequest.setIsAvailable(true);
        testProductRequest.setPreparationTime(7);
        testProductRequest.setDisplayOrder(2);
        testProductRequest.setCategoryId(1);
    }
    
    @Test
    @DisplayName("Get all products should return all products")
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);
        
        // Act
        List<Product> result = productService.getAllProducts();
        
        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("Espresso");
        
        verify(productRepository).findAll();
    }
    
    @Test
    @DisplayName("Get product by ID should return product when exists")
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        
        // Act
        Optional<Product> result = productService.getProductById(1);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Espresso");
        
        verify(productRepository).findById(1);
    }
    
    @Test
    @DisplayName("Get product by ID should return empty when not exists")
    void getProductById_WhenProductNotExists_ShouldReturnEmpty() {
        // Arrange
        when(productRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act
        Optional<Product> result = productService.getProductById(999);
        
        // Assert
        assertThat(result).isEmpty();
        
        verify(productRepository).findById(999);
    }
    
    @Test
    @DisplayName("Save new product should set created and updated timestamps")
    void saveProduct_WithNewProduct_ShouldSetTimestamps() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Latte");
        newProduct.setPrice(new BigDecimal("55000"));
        newProduct.setCategory(testCategory);
        
        when(productRepository.findByName("Latte")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);
        
        // Act
        Product result = productService.saveProduct(newProduct);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(productRepository).findByName("Latte");
        verify(productRepository).save(argThat(product -> 
            product.getCreatedAt() != null && 
            product.getUpdatedAt() != null
        ));
    }
    
    @Test
    @DisplayName("Save existing product should preserve created timestamp and update updated timestamp")
    void saveProduct_WithExistingProduct_ShouldPreserveCreatedTimestamp() {
        // Arrange
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(1);
        testProduct.setCreatedAt(originalCreatedAt);
        
        when(productRepository.findByName("Espresso")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act
        Product result = productService.saveProduct(testProduct);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(productRepository).findByName("Espresso");
        verify(productRepository).save(argThat(product -> 
            product.getCreatedAt().equals(originalCreatedAt) &&
            product.getUpdatedAt() != null
        ));
    }
    
    @Test
    @DisplayName("Save product with duplicate name should throw exception")
    void saveProduct_WithDuplicateName_ShouldThrowException() {
        // Arrange
        Product existingProduct = new Product();
        existingProduct.setId(2);
        existingProduct.setName("Espresso");
        
        Product newProduct = new Product();
        newProduct.setName("Espresso");
        newProduct.setPrice(new BigDecimal("55000"));
        
        when(productRepository.findByName("Espresso")).thenReturn(Optional.of(existingProduct));
        
        // Act & Assert
        assertThatThrownBy(() -> productService.saveProduct(newProduct))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Tên sản phẩm đã tồn tại!");
        
        verify(productRepository).findByName("Espresso");
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Save product with zero price should throw exception")
    void saveProduct_WithZeroPrice_ShouldThrowException() {
        // Arrange
        Product invalidProduct = new Product();
        invalidProduct.setName("Free Coffee");
        invalidProduct.setPrice(BigDecimal.ZERO);
        
        when(productRepository.findByName("Free Coffee")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> productService.saveProduct(invalidProduct))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Giá sản phẩm phải lớn hơn 0!");
        
        verify(productRepository).findByName("Free Coffee");
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Save product with negative price should throw exception")
    void saveProduct_WithNegativePrice_ShouldThrowException() {
        // Arrange
        Product invalidProduct = new Product();
        invalidProduct.setName("Invalid Coffee");
        invalidProduct.setPrice(new BigDecimal("-10000"));
        
        when(productRepository.findByName("Invalid Coffee")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> productService.saveProduct(invalidProduct))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Giá sản phẩm phải lớn hơn 0!");
        
        verify(productRepository).findByName("Invalid Coffee");
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Save product from DTO should create new product with category")
    void saveProductFromDTO_WithNewProduct_ShouldCreateProduct() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(productRepository.findByName("Cappuccino")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act
        Product result = productService.saveProductFromDTO(testProductRequest, null);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(categoryRepository).findById(1);
        verify(productRepository).findByName("Cappuccino");
        verify(productRepository).save(argThat(product -> 
            product.getName().equals("Cappuccino") &&
            product.getPrice().equals(new BigDecimal("60000")) &&
            product.getCategory().equals(testCategory)
        ));
    }
    
    @Test
    @DisplayName("Save product from DTO with existing ID should update existing product")
    void saveProductFromDTO_WithExistingId_ShouldUpdateProduct() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(productRepository.findByName("Cappuccino")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act
        Product result = productService.saveProductFromDTO(testProductRequest, 1);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(productRepository).findById(1);
        verify(categoryRepository).findById(1);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Save product from DTO with invalid category should throw exception")
    void saveProductFromDTO_WithInvalidCategory_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());
        
        testProductRequest.setCategoryId(999);
        
        // Act & Assert
        assertThatThrownBy(() -> productService.saveProductFromDTO(testProductRequest, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category not found with id: 999");
        
        verify(categoryRepository).findById(999);
        verifyNoInteractions(productRepository);
    }
    
    @Test
    @DisplayName("Delete product should call repository delete")
    void deleteProduct_WithValidId_ShouldCallRepositoryDelete() {
        // Arrange
        doNothing().when(productRepository).deleteById(1);
        
        // Act
        productService.deleteProduct(1);
        
        // Assert
        verify(productRepository).deleteById(1);
    }
    
    @Test
    @DisplayName("Reorder products should update display order")
    void reorderProducts_WithValidMap_ShouldUpdateDisplayOrder() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1);
        product1.setDisplayOrder(1);
        
        Product product2 = new Product();
        product2.setId(2);
        product2.setDisplayOrder(2);
        
        List<Product> products = Arrays.asList(product1, product2);
        Map<Integer, Integer> reorderMap = Map.of(1, 3, 2, 1);
        
        when(productRepository.findAll()).thenReturn(products);
        when(productRepository.save(any(Product.class))).thenReturn(product1, product2);
        
        // Act
        List<Product> result = productService.reorderProducts(reorderMap);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(productRepository, times(2)).findAll();
        verify(productRepository).save(argThat(product -> 
            product.getId().equals(1) && product.getDisplayOrder().equals(3)
        ));
        verify(productRepository).save(argThat(product -> 
            product.getId().equals(2) && product.getDisplayOrder().equals(1)
        ));
    }
    
    @Test
    @DisplayName("Reorder products with partial map should only update specified products")
    void reorderProducts_WithPartialMap_ShouldUpdateOnlySpecifiedProducts() {
        // Arrange
        Product product1 = new Product();
        product1.setId(1);
        product1.setDisplayOrder(1);
        
        Product product2 = new Product();
        product2.setId(2);
        product2.setDisplayOrder(2);
        
        List<Product> products = Arrays.asList(product1, product2);
        Map<Integer, Integer> reorderMap = Map.of(1, 5); // Only reorder product 1
        
        when(productRepository.findAll()).thenReturn(products);
        when(productRepository.save(any(Product.class))).thenReturn(product1);
        
        // Act
        List<Product> result = productService.reorderProducts(reorderMap);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(productRepository, times(2)).findAll();
        verify(productRepository, times(1)).save(argThat(product -> 
            product.getId().equals(1) && product.getDisplayOrder().equals(5)
        ));
        // Verify product2 was not saved (not in reorder map)
        verify(productRepository, never()).save(argThat(product -> 
            product.getId().equals(2)
        ));
    }
    
    @Test
    @DisplayName("Update existing product with same name should not throw exception")
    void saveProduct_WithSameNameForExistingProduct_ShouldNotThrowException() {
        // Arrange
        when(productRepository.findByName("Espresso")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        // Act & Assert
        assertThatCode(() -> productService.saveProduct(testProduct)).doesNotThrowAnyException();
        
        verify(productRepository).findByName("Espresso");
        verify(productRepository).save(testProduct);
    }
}