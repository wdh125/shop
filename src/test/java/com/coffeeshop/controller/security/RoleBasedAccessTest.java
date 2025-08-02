package com.coffeeshop.controller.security;

import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for role-based access control (403 Forbidden scenarios)
 * Tests various permission scenarios for different user roles
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Role-Based Access Control Tests (403 Forbidden)")
class RoleBasedAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminProductRequestDTO adminProductRequest;
    private AdminTableRequestDTO adminTableRequest;

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

        adminTableRequest = new AdminTableRequestDTO();
        adminTableRequest.setTableNumber("T01");
        adminTableRequest.setCapacity(4);
        adminTableRequest.setLocation("Main Floor");
        adminTableRequest.setIsActive(true);
    }

    @Test
    @DisplayName("Customer role accessing admin product endpoints should return 403 Forbidden")
    @WithMockUser(authorities = {"ROLE_CUSTOMER"})
    void customerRole_AccessingAdminProductEndpoints_ShouldReturn403() throws Exception {
        // GET all products (admin only)
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        // POST create product (admin only)
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isForbidden());

        // PUT update product (admin only)
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isForbidden());

        // DELETE product (admin only)
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());

        // GET product by ID (admin only)
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isForbidden());

        // PATCH toggle product availability (admin only)
        mockMvc.perform(patch("/api/products/1/toggle-available"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer role accessing admin table endpoints should return 403 Forbidden")
    @WithMockUser(authorities = {"ROLE_CUSTOMER"})
    void customerRole_AccessingAdminTableEndpoints_ShouldReturn403() throws Exception {
        // GET all tables (admin only)
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());

        // POST create table (admin only)
        mockMvc.perform(post("/api/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isForbidden());

        // PUT update table (admin only)
        mockMvc.perform(put("/api/tables/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isForbidden());

        // DELETE table (admin only)
        mockMvc.perform(delete("/api/tables/1"))
                .andExpect(status().isForbidden());

        // GET table by ID (admin only)
        mockMvc.perform(get("/api/tables/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User role (non-admin, non-customer) accessing admin endpoints should return 403 Forbidden")
    @WithMockUser(authorities = {"ROLE_USER"})
    void userRole_AccessingAdminEndpoints_ShouldReturn403() throws Exception {
        // Product admin endpoints
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest)))
                .andExpect(status().isForbidden());

        // Table admin endpoints
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("No role/authorities accessing admin endpoints should return 403 Forbidden")
    @WithMockUser
    void noRole_AccessingAdminEndpoints_ShouldReturn403() throws Exception {
        // Product admin endpoints
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());

        // Table admin endpoints
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/tables/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin role should have access to admin endpoints (positive test)")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void adminRole_AccessingAdminEndpoints_ShouldSucceed() throws Exception {
        // These should return 200 or other success codes (not 403)
        // Note: They might still fail due to business logic or missing data, but not due to authorization

        mockMvc.perform(get("/api/products"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        mockMvc.perform(get("/api/tables"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        mockMvc.perform(get("/api/products/1"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        mockMvc.perform(get("/api/tables/1"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });
    }

    @Test
    @DisplayName("Customer role should have access to customer endpoints (positive test)")
    @WithMockUser(authorities = {"ROLE_CUSTOMER"})
    void customerRole_AccessingCustomerEndpoints_ShouldSucceed() throws Exception {
        // Customer should be able to access their own orders and create orders
        // Note: These might fail due to missing data or business logic, but not due to authorization

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        // Note: POST /api/orders might still fail due to validation or missing data, 
        // but it should not return 403 for authorized customers
    }

    @Test
    @DisplayName("Mixed role scenarios - correct access patterns")
    @WithMockUser(authorities = {"ROLE_CUSTOMER"})
    void mixedRoleScenarios_ShouldFollowCorrectAccessPatterns() throws Exception {
        // Customer should access public endpoints successfully
        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk());

        // But should be forbidden from admin endpoints
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Invalid authority format should return 403 Forbidden")
    @WithMockUser(authorities = {"INVALID_ROLE"})
    void invalidAuthority_AccessingProtectedEndpoints_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());

        // Don't test complex endpoints that might cause business logic errors with invalid auth
    }

    @Test
    @DisplayName("Multiple roles - higher privilege should grant access")
    @WithMockUser(authorities = {"ROLE_CUSTOMER", "ROLE_ADMIN"})
    void multipleRoles_WithAdminRole_ShouldGrantAdminAccess() throws Exception {
        // Should have admin access when ROLE_ADMIN is present
        mockMvc.perform(get("/api/products"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        mockMvc.perform(get("/api/tables"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });
    }

    @Test
    @DisplayName("Case sensitivity in roles should matter")
    @WithMockUser(authorities = {"role_admin"}) // lowercase
    void lowercaseRole_AccessingAdminEndpoints_ShouldReturn403() throws Exception {
        // Should return 403 because Spring Security is case-sensitive
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());
    }
}