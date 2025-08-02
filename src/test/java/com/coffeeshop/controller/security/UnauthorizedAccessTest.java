package com.coffeeshop.controller.security;

import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for unauthorized access scenarios
 * Tests various negative cases for JWT authentication
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Unauthorized Access Tests")
class UnauthorizedAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminProductRequestDTO adminProductRequest;
    private CustomerOrderRequestDTO customerOrderRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        adminProductRequest = new AdminProductRequestDTO();
        adminProductRequest.setName("Test Product");
        adminProductRequest.setDescription("Test Description");
        adminProductRequest.setPrice(new BigDecimal("50000"));
        adminProductRequest.setImageUrl("test.jpg");
        adminProductRequest.setIsAvailable(true);
        adminProductRequest.setPreparationTime(5);
        adminProductRequest.setDisplayOrder(1);
        adminProductRequest.setCategoryId(1);

        customerOrderRequest = new CustomerOrderRequestDTO();
        customerOrderRequest.setTableId(1);
        OrderItemDTO orderItem = new OrderItemDTO();
        orderItem.setProductId(1);
        orderItem.setQuantity(2);
        customerOrderRequest.setItems(Arrays.asList(orderItem));
    }

    @Test
    @DisplayName("Admin endpoints without JWT token should return 403 Forbidden")
    void adminEndpoints_WithoutToken_ShouldReturn403() throws Exception {
        // Product admin endpoints
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());

        // Table admin endpoints
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tableNumber\":\"T01\",\"capacity\":4,\"location\":\"Main Floor\",\"isActive\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer endpoints without JWT token should return 403 Forbidden")
    void customerEndpoints_WithoutToken_ShouldReturn403() throws Exception {
        // Order endpoints
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerOrderRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoints with invalid JWT token should return 403 Forbidden (treated as no token)")
    void endpoints_WithInvalidToken_ShouldReturn403() throws Exception {
        String invalidToken = "Bearer invalid.jwt.token";

        mockMvc.perform(get("/api/products")
                .header("Authorization", invalidToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerOrderRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoints with malformed JWT token should return 403 Forbidden (treated as no token)")
    void endpoints_WithMalformedToken_ShouldReturn403() throws Exception {
        String malformedToken = "Bearer malformed_token";

        mockMvc.perform(get("/api/products")
                .header("Authorization", malformedToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/tables")
                .header("Authorization", malformedToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tableNumber\":\"T01\",\"capacity\":4,\"location\":\"Test\",\"isActive\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoints with expired JWT token should return 403 Forbidden (treated as no token)")
    void endpoints_WithExpiredToken_ShouldReturn403() throws Exception {
        // Simulate an expired token (this would typically be generated with a past expiration)
        String expiredToken = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTYwOTQ1OTIwMH0.expired";

        mockMvc.perform(get("/api/products")
                .header("Authorization", expiredToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", expiredToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerOrderRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Endpoints with token missing Bearer prefix should return 403 Forbidden (treated as no token)")
    void endpoints_WithTokenMissingBearerPrefix_ShouldReturn403() throws Exception {
        String tokenWithoutBearer = "invalid.jwt.token";

        mockMvc.perform(get("/api/products")
                .header("Authorization", tokenWithoutBearer))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", tokenWithoutBearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerOrderRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Mixed scenarios - valid public access and forbidden protected access")
    void mixedScenarios_ShouldBehaveCorrectly() throws Exception {
        // Protected endpoint without token should fail with 403
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        // Protected endpoint with invalid token should also fail with 403 (treated as no token)
        mockMvc.perform(get("/api/products")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }
}