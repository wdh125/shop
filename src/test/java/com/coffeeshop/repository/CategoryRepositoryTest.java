package com.coffeeshop.repository;

import com.coffeeshop.entity.Category;
import com.coffeeshop.test.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for CategoryRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = TestDataFactory.createTestCategory("Beverages", 1);
        entityManager.persistAndFlush(testCategory);
    }

    @Test
    @DisplayName("Find by name - Success")
    void findByName_Success() {
        // When
        Optional<Category> result = categoryRepository.findByName("Beverages");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Beverages");
        assertThat(result.get().getDescription()).isEqualTo("Test Beverages");
        assertThat(result.get().getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Find by name - Not found")
    void findByName_NotFound() {
        // When
        Optional<Category> result = categoryRepository.findByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find by name - Case sensitive")
    void findByName_CaseSensitive() {
        // When
        Optional<Category> result = categoryRepository.findByName("beverages");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Save category - Success")
    void saveCategory_Success() {
        // Given
        Category newCategory = TestDataFactory.createTestCategory("Desserts", 2);

        // When
        Category savedCategory = categoryRepository.save(newCategory);

        // Then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Desserts");
        assertThat(savedCategory.getDescription()).isEqualTo("Test Desserts");
        assertThat(savedCategory.getDisplayOrder()).isEqualTo(2);
        
        // Verify it was persisted
        Optional<Category> retrievedCategory = categoryRepository.findById(savedCategory.getId());
        assertThat(retrievedCategory).isPresent();
        assertThat(retrievedCategory.get().getName()).isEqualTo("Desserts");
    }

    @Test
    @DisplayName("Find by ID - Success")
    void findById_Success() {
        // When
        Optional<Category> result = categoryRepository.findById(testCategory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Beverages");
    }

    @Test
    @DisplayName("Find by ID - Not found")
    void findById_NotFound() {
        // When
        Optional<Category> result = categoryRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Delete category - Success")
    void deleteCategory_Success() {
        // Given
        Integer categoryId = testCategory.getId();

        // When
        categoryRepository.delete(testCategory);
        entityManager.flush();

        // Then
        Optional<Category> result = categoryRepository.findById(categoryId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find all categories - Success")
    void findAllCategories_Success() {
        // Given
        Category category2 = TestDataFactory.createTestCategory("Snacks", 3);
        entityManager.persistAndFlush(category2);

        // When
        var allCategories = categoryRepository.findAll();

        // Then
        assertThat(allCategories).hasSize(2);
        assertThat(allCategories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Beverages", "Snacks");
    }

    @Test
    @DisplayName("Update category - Success")
    void updateCategory_Success() {
        // Given
        testCategory.setName("Hot Beverages");
        testCategory.setDescription("Updated description");

        // When
        Category updatedCategory = categoryRepository.save(testCategory);
        entityManager.flush();

        // Then
        assertThat(updatedCategory.getName()).isEqualTo("Hot Beverages");
        assertThat(updatedCategory.getDescription()).isEqualTo("Updated description");
        
        // Verify persistence
        Optional<Category> retrievedCategory = categoryRepository.findById(testCategory.getId());
        assertThat(retrievedCategory).isPresent();
        assertThat(retrievedCategory.get().getName()).isEqualTo("Hot Beverages");
        assertThat(retrievedCategory.get().getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("Find by name with null - Should not throw exception")
    void findByName_WithNull() {
        // When & Then
        Optional<Category> result = categoryRepository.findByName(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find by name with empty string - Should not throw exception")
    void findByName_WithEmptyString() {
        // When & Then
        Optional<Category> result = categoryRepository.findByName("");
        assertThat(result).isEmpty();
    }
}