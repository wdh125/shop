package com.coffeeshop.controller.integration;

import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Controller endpoints
 * Tests basic functionality for ProductController, OrderController, and TableController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/products/available should return available products")
    void getAvailableProducts_ShouldReturnProducts() throws Exception {
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/tables/available should return available tables") 
    void getAvailableTables_ShouldReturnTables() throws Exception {
        mockMvc.perform(get("/api/tables/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/products with admin role should handle product creation")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createProduct_WithAdminRole_ShouldHandleRequest() throws Exception {
        AdminProductRequestDTO request = new AdminProductRequestDTO();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("50000"));
        request.setImageUrl("test.jpg");
        request.setIsAvailable(true);
        request.setPreparationTime(5);
        request.setDisplayOrder(1);
        request.setCategoryId(1);

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/tables with admin role should handle table creation")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createTable_WithAdminRole_ShouldHandleRequest() throws Exception {
        AdminTableRequestDTO request = new AdminTableRequestDTO();
        request.setTableNumber("T99");
        request.setCapacity(4);
        request.setLocation("Test Floor");
        request.setIsActive(true);

        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/orders with authenticated user should handle order creation")
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void createOrder_WithAuthenticatedUser_ShouldHandleRequest() throws Exception {
        OrderItemDTO item = new OrderItemDTO();
        item.setProductId(1);
        item.setQuantity(2);

        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setTableId(1);
        request.setNote("Test order");
        request.setItems(Arrays.asList(item));

        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/orders/my-orders with authenticated user should return user orders")
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void getMyOrders_WithAuthenticatedUser_ShouldReturnOrders() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Admin endpoints without authentication should return 401")
    void adminEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST requests with invalid data should return 400")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void invalidDataRequests_ShouldReturn400() throws Exception {
        // Invalid product request (missing required fields)
        AdminProductRequestDTO invalidProduct = new AdminProductRequestDTO();
        
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        // Invalid table request (missing required fields)
        AdminTableRequestDTO invalidTable = new AdminTableRequestDTO();
        
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)  
                .content(objectMapper.writeValueAsString(invalidTable)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Non-admin users should get 403 for admin endpoints")
    @WithMockUser(authorities = {"ROLE_USER"})
    void nonAdminUsers_ShouldGet403ForAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());
    }
}