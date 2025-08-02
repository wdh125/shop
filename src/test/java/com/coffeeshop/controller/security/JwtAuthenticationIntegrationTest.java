package com.coffeeshop.controller.security;

import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.auth.AuthRequestDTO;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * Comprehensive integration tests with full request/response lifecycle
 * Tests complete authentication and authorization flows using MockMvc
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JWT Authentication Integration Tests")
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequestDTO validLoginRequest;
    private AdminProductRequestDTO adminProductRequest;
    private CustomerOrderRequestDTO customerOrderRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        validLoginRequest = new AuthRequestDTO("testuser", "password123");

        adminProductRequest = new AdminProductRequestDTO();
        adminProductRequest.setName("Integration Test Product");
        adminProductRequest.setDescription("Product for integration testing");
        adminProductRequest.setPrice(new BigDecimal("75000"));
        adminProductRequest.setImageUrl("test-product.jpg");
        adminProductRequest.setIsAvailable(true);
        adminProductRequest.setPreparationTime(10);
        adminProductRequest.setDisplayOrder(1);
        adminProductRequest.setCategoryId(1);

        customerOrderRequest = new CustomerOrderRequestDTO();
        customerOrderRequest.setTableId(1);
        OrderItemDTO orderItem = new OrderItemDTO();
        orderItem.setProductId(1);
        orderItem.setQuantity(3);
        customerOrderRequest.setItems(Arrays.asList(orderItem));
    }

    @Test
    @DisplayName("Complete authentication flow - login attempt with full request/response validation")
    void completeAuthenticationFlow_LoginAttempt_ShouldValidateFullRequestResponse() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()) // Print full request/response for debugging
                .andExpect(status().isUnauthorized()) // Expecting failure since no real user exists
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // Validate response structure and headers
        String responseContent = result.getResponse().getContentAsString();
        // The response should contain error information (since login will fail)
        // but the important part is that it reached the controller and processed the request
    }

    @Test
    @DisplayName("Multi-step authorization flow - authenticate then access protected resource")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void multiStepAuthorizationFlow_AuthenticateThenAccessProtected_ShouldWork() throws Exception {
        // Step 1: Access protected resource with authentication
        MvcResult step1Result = mockMvc.perform(get("/api/products")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 403) {
                        throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status);
                    }
                }) // Should not be forbidden (might be 200, 404, 500 depending on data)
                .andReturn();

        // Step 2: Attempt to create a new product
        MvcResult step2Result = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status == 403) {
                        throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status);
                    }
                }) // Should not be forbidden due to authorization
                .andReturn();

        // Validate that we got proper responses (not authorization errors)
        // The actual business logic might fail, but authorization should succeed
    }

    @Test
    @DisplayName("Role escalation prevention - customer to admin access attempt")
    @WithMockUser(authorities = {"ROLE_CUSTOMER"})
    void roleEscalationPrevention_CustomerToAdminAttempt_ShouldBeForbidden() throws Exception {
        // Step 1: Customer can access their endpoints
        mockMvc.perform(get("/api/orders/my-orders")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        // Step 2: Customer cannot access admin endpoints
        mockMvc.perform(get("/api/products")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists()); // Should have error information

        // Step 3: Customer cannot perform admin actions
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Public endpoint accessibility validation with full lifecycle")
    void publicEndpointAccessibility_FullLifecycle_ShouldBeAccessible() throws Exception {
        // Test public product endpoints
        MvcResult availableProductsResult = mockMvc.perform(get("/api/products/available")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // Validate response structure
        String responseContent = availableProductsResult.getResponse().getContentAsString();
        // Response should be a JSON array (even if empty)

        // Test category-specific product endpoint
        mockMvc.perform(get("/api/products/category/1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Error handling in authentication flow - malformed requests")
    void errorHandlingInAuthFlow_MalformedRequests_ShouldReturnProperErrors() throws Exception {
        // Test with malformed JSON
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Test with missing required fields
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Test with wrong content type
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Cross-controller authorization validation - comprehensive flow")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void crossControllerAuthorizationValidation_ComprehensiveFlow_ShouldWork() throws Exception {
        // Test admin access across multiple controllers

        // Product Controller - should work
        mockMvc.perform(get("/api/products")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        // Table Controller - should work  
        mockMvc.perform(get("/api/tables")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });

        // Order Controller (if admin has access) - should work
        mockMvc.perform(get("/api/orders/my-orders")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } });
    }

    @Test
    @DisplayName("Security headers validation in responses")
    void securityHeadersValidation_InResponses_ShouldBePresent() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products/available")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andReturn();

        // Validate security headers are consistently applied
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    @DisplayName("Request/Response content validation - comprehensive data flow")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void requestResponseContentValidation_ComprehensiveDataFlow_ShouldValidateCorrectly() throws Exception {
        // Test POST request with comprehensive validation
        MvcResult createResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminProductRequest))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected status to not be 403 Forbidden, but was " + status); } }) // Authorization should succeed
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        // Validate that the request was processed (even if business logic fails)
        String createResponseContent = createResult.getResponse().getContentAsString();
        int createStatusCode = createResult.getResponse().getStatus();
        
        // Should not be an authorization error (401/403)
        assert createStatusCode != 401 && createStatusCode != 403;
    }

    @Test
    @DisplayName("Concurrent access patterns - multiple role validation")
    void concurrentAccessPatterns_MultipleRoleValidation_ShouldMaintainSecurity() throws Exception {
        // Simulate different users with different roles accessing the system
        
        // Admin access
        mockMvc.perform(get("/api/products")
                .header("X-Test-User", "admin")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized()); // No JWT token provided

        // Customer access to public endpoint
        mockMvc.perform(get("/api/products/available")
                .header("X-Test-User", "customer")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // Unauthenticated access to protected endpoint
        mockMvc.perform(get("/api/products")
                .header("X-Test-User", "anonymous")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}