package com.coffeeshop.repository;

import com.coffeeshop.entity.Category;
import com.coffeeshop.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for ProductRepository
 * Tests data access methods for Product entity
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Beverages");
        testCategory.setDescription("Hot and cold beverages");
        testCategory.setDisplayOrder(1);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

        // Create test product
        testProduct = new Product();
        testProduct.setName("Espresso");
        testProduct.setDescription("Strong black coffee");
        testProduct.setPrice(new BigDecimal("50000"));
        testProduct.setImageUrl("espresso.jpg");
        testProduct.setIsAvailable(true);
        testProduct.setPreparationTime(5);
        testProduct.setDisplayOrder(1);
        testProduct.setCategory(testCategory);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Save and find product should work correctly")
    void saveAndFindProduct_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);

        // Act
        Product savedProduct = entityManager.persistAndFlush(testProduct);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Espresso");
        assertThat(foundProduct.get().getDescription()).isEqualTo("Strong black coffee");
        assertThat(foundProduct.get().getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(foundProduct.get().getImageUrl()).isEqualTo("espresso.jpg");
        assertThat(foundProduct.get().getIsAvailable()).isTrue();
        assertThat(foundProduct.get().getPreparationTime()).isEqualTo(5);
        assertThat(foundProduct.get().getDisplayOrder()).isEqualTo(1);
        assertThat(foundProduct.get().getCategory().getName()).isEqualTo("Beverages");
    }

    @Test
    @DisplayName("Find by name should return product when exists")
    void findByName_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        entityManager.persistAndFlush(testProduct);

        // Act
        Optional<Product> result = productRepository.findByName("Espresso");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Espresso");
        assertThat(result.get().getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
    }

    @Test
    @DisplayName("Find by name should return empty when product not exists")
    void findByName_WhenProductNotExists_ShouldReturnEmpty() {
        // Act
        Optional<Product> result = productRepository.findByName("Nonexistent Product");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find by name should be case sensitive")
    void findByName_ShouldBeCaseSensitive() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        entityManager.persistAndFlush(testProduct);

        // Act
        Optional<Product> result1 = productRepository.findByName("Espresso");
        Optional<Product> result2 = productRepository.findByName("espresso");
        Optional<Product> result3 = productRepository.findByName("ESPRESSO");

        // Assert
        assertThat(result1).isPresent();
        assertThat(result2).isEmpty();
        assertThat(result3).isEmpty();
    }

    @Test
    @DisplayName("Save product with different prices should work correctly")
    void saveProduct_WithDifferentPrices_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        
        Product expensiveProduct = new Product();
        expensiveProduct.setName("Premium Coffee");
        expensiveProduct.setDescription("Premium blend");
        expensiveProduct.setPrice(new BigDecimal("150000.50"));
        expensiveProduct.setIsAvailable(true);
        expensiveProduct.setCategory(savedCategory);
        expensiveProduct.setCreatedAt(LocalDateTime.now());
        expensiveProduct.setUpdatedAt(LocalDateTime.now());

        // Act
        Product savedProduct = productRepository.save(expensiveProduct);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("150000.50"));
    }

    @Test
    @DisplayName("Save product with unavailable status should work correctly")
    void saveProduct_WithUnavailableStatus_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        testProduct.setIsAvailable(false);

        // Act
        Product savedProduct = productRepository.save(testProduct);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getIsAvailable()).isFalse();
    }

    @Test
    @DisplayName("Update product should work correctly")
    void updateProduct_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        Product savedProduct = entityManager.persistAndFlush(testProduct);

        // Act
        savedProduct.setName("Updated Espresso");
        savedProduct.setPrice(new BigDecimal("60000"));
        savedProduct.setIsAvailable(false);
        savedProduct.setUpdatedAt(LocalDateTime.now());
        Product updatedProduct = productRepository.save(savedProduct);

        // Assert
        assertThat(updatedProduct.getName()).isEqualTo("Updated Espresso");
        assertThat(updatedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("60000"));
        assertThat(updatedProduct.getIsAvailable()).isFalse();
        assertThat(updatedProduct.getDescription()).isEqualTo("Strong black coffee"); // Should remain unchanged
    }

    @Test
    @DisplayName("Delete product should work correctly")
    void deleteProduct_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        Product savedProduct = entityManager.persistAndFlush(testProduct);
        Integer productId = savedProduct.getId();

        // Act
        productRepository.deleteById(productId);
        entityManager.flush();

        // Assert
        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertThat(deletedProduct).isEmpty();
    }

    @Test
    @DisplayName("Find all products should return all products")
    void findAllProducts_ShouldReturnAllProducts() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        
        testProduct.setCategory(savedCategory);
        
        Product product2 = new Product();
        product2.setName("Cappuccino");
        product2.setDescription("Coffee with steamed milk");
        product2.setPrice(new BigDecimal("60000"));
        product2.setIsAvailable(true);
        product2.setCategory(savedCategory);
        product2.setCreatedAt(LocalDateTime.now());
        product2.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(testProduct);
        entityManager.persistAndFlush(product2);

        // Act
        var allProducts = productRepository.findAll();

        // Assert
        assertThat(allProducts).hasSize(2);
        assertThat(allProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("Espresso", "Cappuccino");
        assertThat(allProducts).extracting(Product::getPrice)
                .containsExactlyInAnyOrder(new BigDecimal("50000"), new BigDecimal("60000"));
    }

    @Test
    @DisplayName("Product with null optional fields should be saved correctly")
    void saveProduct_WithNullOptionalFields_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        
        Product minimalProduct = new Product();
        minimalProduct.setName("Basic Coffee");
        minimalProduct.setPrice(new BigDecimal("30000"));
        minimalProduct.setIsAvailable(true);
        minimalProduct.setCategory(savedCategory);
        minimalProduct.setCreatedAt(LocalDateTime.now());
        minimalProduct.setUpdatedAt(LocalDateTime.now());
        // description, imageUrl, phone, preparationTime, displayOrder are null/default

        // Act
        Product savedProduct = productRepository.save(minimalProduct);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getName()).isEqualTo("Basic Coffee");
        assertThat(savedProduct.getDescription()).isNull();
        assertThat(savedProduct.getImageUrl()).isNull();
        assertThat(savedProduct.getPreparationTime()).isEqualTo(10); // Default value
        assertThat(savedProduct.getDisplayOrder()).isEqualTo(0); // Default value
    }

    @Test
    @DisplayName("Product with long preparation time should be saved correctly")
    void saveProduct_WithLongPreparationTime_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        testProduct.setPreparationTime(60); // 1 hour

        // Act
        Product savedProduct = productRepository.save(testProduct);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getPreparationTime()).isEqualTo(60);
    }

    @Test
    @DisplayName("Product with high display order should be saved correctly")
    void saveProduct_WithHighDisplayOrder_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        testProduct.setDisplayOrder(999);

        // Act
        Product savedProduct = productRepository.save(testProduct);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getDisplayOrder()).isEqualTo(999);
    }

    @Test
    @DisplayName("Product with very long description should be saved correctly")
    void saveProduct_WithLongDescription_ShouldWorkCorrectly() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory);
        testProduct.setCategory(savedCategory);
        
        String longDescription = "This is a very long description that contains many words and characters to test if the database can handle large text fields properly. ".repeat(10);
        testProduct.setDescription(longDescription);

        // Act
        Product savedProduct = productRepository.save(testProduct);

        // Assert
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getDescription()).isEqualTo(longDescription);
        assertThat(savedProduct.getDescription().length()).isGreaterThan(1000);
    }
}