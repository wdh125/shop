package com.coffeeshop.integration;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.entity.Category;
import com.coffeeshop.entity.Product;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.TableStatus;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.repository.TableRepository;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for the coffee shop application
 * Tests end-to-end business flows including user registration, authentication, and ordering
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CoffeeShopIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testCustomer;
    private User testAdmin;
    private Category testCategory;
    private Product testProduct;
    private TableEntity testTable;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Beverages");
        testCategory.setDescription("Hot and cold beverages");
        testCategory.setDisplayOrder(1);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

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
        testProduct = productRepository.save(testProduct);

        // Create test table
        testTable = new TableEntity();
        testTable.setTableNumber("T01");
        testTable.setLocation("Main Floor");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setIsActive(true);
        testTable.setCreatedAt(LocalDateTime.now());
        testTable.setUpdatedAt(LocalDateTime.now());
        testTable = tableRepository.save(testTable);

        // Create test customer
        testCustomer = new User();
        testCustomer.setUsername("testcustomer");
        testCustomer.setEmail("customer@example.com");
        testCustomer.setPassword(passwordEncoder.encode("password123"));
        testCustomer.setFullName("Test Customer");
        testCustomer.setPhone("1234567890");
        testCustomer.setRole(UserRole.ROLE_CUSTOMER);
        testCustomer.setIsActive(true);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());
        testCustomer = userRepository.save(testCustomer);

        // Create test admin
        testAdmin = new User();
        testAdmin.setUsername("testadmin");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPassword(passwordEncoder.encode("adminpass"));
        testAdmin.setFullName("Test Admin");
        testAdmin.setRole(UserRole.ROLE_ADMIN);
        testAdmin.setIsActive(true);
        testAdmin.setCreatedAt(LocalDateTime.now());
        testAdmin.setUpdatedAt(LocalDateTime.now());
        testAdmin = userRepository.save(testAdmin);
    }

    @Test
    @DisplayName("User registration flow should work end-to-end")
    void userRegistrationFlow_ShouldWorkEndToEnd() throws Exception {
        // Arrange
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newcustomer");
        registerRequest.setPassword("newpassword123");
        registerRequest.setEmail("newcustomer@example.com");
        registerRequest.setFullName("New Customer");
        registerRequest.setPhone("9876543210");

        // Act & Assert - Register
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Đăng ký thành công!"));

        // Verify user was created in database
        User createdUser = userRepository.findByUsername("newcustomer").orElse(null);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("newcustomer@example.com");
        assertThat(createdUser.getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(createdUser.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Authentication flow should work end-to-end")
    void authenticationFlow_ShouldWorkEndToEnd() throws Exception {
        // Arrange
        AuthRequestDTO loginRequest = new AuthRequestDTO("testcustomer", "password123");

        // Act & Assert - Login
        String response = mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testcustomer"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify response structure
        AuthResponseDTO authResponse = objectMapper.readValue(response, AuthResponseDTO.class);
        assertThat(authResponse.getAccessToken()).isNotEmpty();
        assertThat(authResponse.getRefreshToken()).isNotEmpty();
        assertThat(authResponse.getUsername()).isEqualTo("testcustomer");
        assertThat(authResponse.getRole()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Product availability check should work correctly")
    void productAvailabilityCheck_ShouldWorkCorrectly() throws Exception {
        // Act & Assert - Get available products
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Espresso"))
                .andExpect(jsonPath("$[0].price").value(50000.0))
                .andExpect(jsonPath("$[0].isAvailable").value(true));
    }

    @Test
    @DisplayName("Database entities should be properly configured with relationships")
    void databaseEntities_ShouldBeProperlyConfigured() {
        // Assert - Verify entities and relationships
        assertThat(testProduct.getCategory()).isNotNull();
        assertThat(testProduct.getCategory().getName()).isEqualTo("Beverages");
        assertThat(testProduct.getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(testProduct.getIsAvailable()).isTrue();

        assertThat(testTable.getTableNumber()).isEqualTo("T01");
        assertThat(testTable.getStatus()).isEqualTo(TableStatus.AVAILABLE);

        assertThat(testCustomer.getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(testAdmin.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("User roles should be properly distinguished")
    void userRoles_ShouldBeProperlyDistinguished() {
        // Assert
        assertThat(testCustomer.getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(testAdmin.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
        
        // Verify customer cannot have admin privileges
        assertThat(testCustomer.getRole()).isNotEqualTo(UserRole.ROLE_ADMIN);
        
        // Verify admin can have admin privileges
        assertThat(testAdmin.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Data validation should work for user creation")
    void dataValidation_ShouldWorkForUserCreation() {
        // Test duplicate username
        User duplicateUser = new User();
        duplicateUser.setUsername("testcustomer"); // Same as existing
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPassword("password");
        duplicateUser.setFullName("Different User");
        duplicateUser.setRole(UserRole.ROLE_CUSTOMER);
        duplicateUser.setIsActive(true);
        duplicateUser.setCreatedAt(LocalDateTime.now());
        duplicateUser.setUpdatedAt(LocalDateTime.now());

        // This should fail due to unique constraint
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Product pricing should be properly handled")
    void productPricing_ShouldBeProperlyHandled() {
        // Create product with different price formats
        Product expensiveProduct = new Product();
        expensiveProduct.setName("Premium Coffee");
        expensiveProduct.setDescription("Premium blend");
        expensiveProduct.setPrice(new BigDecimal("150000.50"));
        expensiveProduct.setIsAvailable(true);
        expensiveProduct.setCategory(testCategory);
        expensiveProduct.setCreatedAt(LocalDateTime.now());
        expensiveProduct.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(expensiveProduct);

        // Assert
        assertThat(savedProduct.getPrice()).isEqualByComparingTo(new BigDecimal("150000.50"));
        assertThat(savedProduct.getPrice().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Table management should work correctly")
    void tableManagement_ShouldWorkCorrectly() {
        // Create additional table
        TableEntity table2 = new TableEntity();
        table2.setTableNumber("T02");
        table2.setLocation("Outdoor");
        table2.setCapacity(2);
        table2.setStatus(TableStatus.OCCUPIED);
        table2.setIsActive(true);
        table2.setCreatedAt(LocalDateTime.now());
        table2.setUpdatedAt(LocalDateTime.now());

        TableEntity savedTable = tableRepository.save(table2);

        // Assert
        assertThat(savedTable.getTableNumber()).isEqualTo("T02");
        assertThat(savedTable.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        assertThat(savedTable.getCapacity()).isEqualTo(2);

        // Verify both tables exist
        var allTables = tableRepository.findAll();
        assertThat(allTables).hasSize(2);
        assertThat(allTables).extracting(TableEntity::getTableNumber)
                .containsExactlyInAnyOrder("T01", "T02");
    }

    @Test
    @DisplayName("Category and product relationship should work correctly")
    void categoryProductRelationship_ShouldWorkCorrectly() {
        // Create another category
        Category dessertCategory = new Category();
        dessertCategory.setName("Desserts");
        dessertCategory.setDescription("Sweet treats");
        dessertCategory.setDisplayOrder(2);
        dessertCategory.setCreatedAt(LocalDateTime.now());
        dessertCategory.setUpdatedAt(LocalDateTime.now());
        dessertCategory = categoryRepository.save(dessertCategory);

        // Create product in new category
        Product dessertProduct = new Product();
        dessertProduct.setName("Chocolate Cake");
        dessertProduct.setDescription("Rich chocolate cake");
        dessertProduct.setPrice(new BigDecimal("80000"));
        dessertProduct.setIsAvailable(true);
        dessertProduct.setCategory(dessertCategory);
        dessertProduct.setCreatedAt(LocalDateTime.now());
        dessertProduct.setUpdatedAt(LocalDateTime.now());
        dessertProduct = productRepository.save(dessertProduct);

        // Assert
        assertThat(dessertProduct.getCategory().getName()).isEqualTo("Desserts");
        assertThat(dessertProduct.getCategory().getId()).isEqualTo(dessertCategory.getId());

        // Verify we have products in different categories
        var allProducts = productRepository.findAll();
        assertThat(allProducts).hasSize(2);
        assertThat(allProducts).extracting(product -> product.getCategory().getName())
                .containsExactlyInAnyOrder("Beverages", "Desserts");
    }
}