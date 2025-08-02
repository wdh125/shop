package com.coffeeshop.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.coffeeshop.entity.Category;
import com.coffeeshop.entity.Product;

import org.springframework.test.context.ActiveProfiles;

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
        // Create test category first
        testCategory = new Category();
        testCategory.setName("Drinks");
        testCategory.setDescription("Hot and cold beverages");
        testCategory.setIsActive(true);
        testCategory.setDisplayOrder(1);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = entityManager.persistAndFlush(testCategory);

        // Create test product
        testProduct = new Product();
        testProduct.setCategory(testCategory);
        testProduct.setName("Espresso");
        testProduct.setDescription("Strong coffee");
        testProduct.setPrice(BigDecimal.valueOf(25000));
        testProduct.setImageUrl("espresso.jpg");
        testProduct.setIsAvailable(true);
        testProduct.setPreparationTime(5);
        testProduct.setDisplayOrder(1);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByName_ExistingProduct() {
        // Arrange
        entityManager.persistAndFlush(testProduct);

        // Act
        Optional<Product> found = productRepository.findByName("Espresso");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Espresso");
        assertThat(found.get().getPrice()).isEqualTo(BigDecimal.valueOf(25000));
    }

    @Test
    void testFindByName_NonExistingProduct() {
        // Act
        Optional<Product> found = productRepository.findByName("NonExistentProduct");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void testSave_NewProduct() {
        // Act
        Product saved = productRepository.save(testProduct);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Espresso");
        assertThat(saved.getCategory()).isEqualTo(testCategory);
        assertThat(saved.getIsAvailable()).isTrue();
    }

    @Test
    void testFindById_ExistingProduct() {
        // Arrange
        Product saved = entityManager.persistAndFlush(testProduct);

        // Act
        Optional<Product> found = productRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Espresso");
        assertThat(found.get().getCategory().getName()).isEqualTo("Drinks");
    }

    @Test
    void testFindAll_MultipleProducts() {
        // Arrange
        Product product2 = new Product();
        product2.setCategory(testCategory);
        product2.setName("Cappuccino");
        product2.setDescription("Coffee with milk foam");
        product2.setPrice(BigDecimal.valueOf(35000));
        product2.setIsAvailable(true);
        product2.setPreparationTime(7);
        product2.setCreatedAt(LocalDateTime.now());
        product2.setUpdatedAt(LocalDateTime.now());

        entityManager.persist(testProduct);
        entityManager.persist(product2);
        entityManager.flush();

        // Act
        var products = productRepository.findAll();

        // Assert
        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Espresso", "Cappuccino");
    }

    @Test
    void testDelete_ExistingProduct() {
        // Arrange
        Product saved = entityManager.persistAndFlush(testProduct);
        Integer productId = saved.getId();

        // Act
        productRepository.deleteById(productId);

        // Assert
        Optional<Product> found = productRepository.findById(productId);
        assertThat(found).isEmpty();
    }

    @Test
    void testUpdate_ExistingProduct() {
        // Arrange
        Product saved = entityManager.persistAndFlush(testProduct);
        entityManager.clear(); // Clear the persistence context

        // Act
        saved.setPrice(BigDecimal.valueOf(30000));
        saved.setDescription("Premium espresso");
        Product updated = productRepository.save(saved);

        // Assert
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(updated.getDescription()).isEqualTo("Premium espresso");
        assertThat(updated.getName()).isEqualTo("Espresso"); // Unchanged field
    }
}