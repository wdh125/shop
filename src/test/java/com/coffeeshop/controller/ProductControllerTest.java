package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import com.coffeeshop.security.JwtAuthenticationFilter;
import com.coffeeshop.security.JwtUtils;
import com.coffeeshop.service.CustomUserDetailsService;
import com.coffeeshop.service.ProductService;
import com.coffeeshop.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController
 * Tests REST API endpoints for product management
 */
@WebMvcTest(ProductController.class)
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminProductRequestDTO adminProductRequest;
    private CustomerProductResponseDTO customerProductResponse;

    @BeforeEach
    void setUp() {
        // Setup admin product request DTO
        adminProductRequest = new AdminProductRequestDTO();
        adminProductRequest.setName("Cappuccino");
        adminProductRequest.setDescription("Coffee with steamed milk");
        adminProductRequest.setPrice(new BigDecimal("60000"));
        adminProductRequest.setImageUrl("cappuccino.jpg");
        adminProductRequest.setIsAvailable(true);
        adminProductRequest.setPreparationTime(7);
        adminProductRequest.setDisplayOrder(2);
        adminProductRequest.setCategoryId(1);

        // Setup customer product response DTO
        customerProductResponse = new CustomerProductResponseDTO();
        customerProductResponse.setId(1);
        customerProductResponse.setName("Cappuccino");
        customerProductResponse.setPrice(60000.0);
        customerProductResponse.setImageUrl("cappuccino.jpg");
        customerProductResponse.setIsAvailable(true);
    }

    @Test
    @DisplayName("GET /api/products/available should return available products for everyone")
    void getAvailableProducts_ShouldReturnAvailableProducts() throws Exception {
        // Arrange
        List<CustomerProductResponseDTO> products = Arrays.asList(customerProductResponse);
        when(productService.getFilteredCustomerProducts(null, null, "")).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cappuccino"))
                .andExpect(jsonPath("$[0].price").value(60000.0))
                .andExpect(jsonPath("$[0].isAvailable").value(true));

        verify(productService).getFilteredCustomerProducts(null, null, "");
    }

    @Test
    @DisplayName("GET /api/products/available with filters should apply filters")
    void getAvailableProducts_WithFilters_ShouldApplyFilters() throws Exception {
        // Arrange
        List<CustomerProductResponseDTO> products = Arrays.asList(customerProductResponse);
        when(productService.getFilteredCustomerProducts(1, "Cappuccino", "name")).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/available")
                .param("categoryId", "1")
                .param("search", "Cappuccino")
                .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Cappuccino"));

        verify(productService).getFilteredCustomerProducts(1, "Cappuccino", "name");
    }

    @Test
    @DisplayName("GET /api/products/category/{categoryId} should return products by category")
    void getProductsByCategory_ShouldReturnProductsByCategory() throws Exception {
        // Arrange
        List<CustomerProductResponseDTO> products = Arrays.asList(customerProductResponse);
        when(productService.getCustomerProductsByCategory(1)).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(productService).getCustomerProductsByCategory(1);
    }

    @Test
    @DisplayName("GET /api/products with admin role should return admin products")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getAllProducts_WithAdminRole_ShouldReturnAdminProducts() throws Exception {
        // Arrange - Use mock since AdminProductResponseDTO is immutable
        AdminProductResponseDTO mockResponse = mock(AdminProductResponseDTO.class);
        when(mockResponse.getId()).thenReturn(1);
        when(mockResponse.getName()).thenReturn("Cappuccino");
        
        List<AdminProductResponseDTO> products = Arrays.asList(mockResponse);
        when(productService.getFilteredAdminProducts(null, null, null, "")).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cappuccino"));

        verify(productService).getFilteredAdminProducts(null, null, null, "");
    }

    @Test
    @DisplayName("GET /api/products without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void getAllProducts_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("POST /api/products with admin role should create product")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createProduct_WithAdminRole_ShouldCreateProduct() throws Exception {
        // Arrange - Use mock since AdminProductResponseDTO is immutable
        AdminProductResponseDTO mockResponse = mock(AdminProductResponseDTO.class);
        when(mockResponse.getId()).thenReturn(1);
        when(mockResponse.getName()).thenReturn("Cappuccino");
        when(mockResponse.getPrice()).thenReturn(new BigDecimal("60000"));
        
        when(productService.createProduct(any(AdminProductRequestDTO.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cappuccino"));

        verify(productService).createProduct(any(AdminProductRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/products without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void createProduct_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("POST /api/products with invalid data should return 400")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createProduct_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange - Create invalid request (missing required fields)
        AdminProductRequestDTO invalidRequest = new AdminProductRequestDTO();
        // name is required but missing

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Admin endpoints without authentication should return 401")
    void adminEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/products/{id} with admin role should update product")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void updateProduct_WithAdminRole_ShouldUpdateProduct() throws Exception {
        // Arrange - Use mock since AdminProductResponseDTO is immutable
        AdminProductResponseDTO mockResponse = mock(AdminProductResponseDTO.class);
        when(mockResponse.getId()).thenReturn(1);
        when(mockResponse.getName()).thenReturn("Cappuccino");
        
        when(productService.updateProduct(eq(1), any(AdminProductRequestDTO.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cappuccino"));

        verify(productService).updateProduct(eq(1), any(AdminProductRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} with admin role should delete product")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteProduct_WithAdminRole_ShouldDeleteProduct() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1);

        // Act & Assert
        mockMvc.perform(delete("/api/products/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(productService).deleteProduct(1);
    }
}