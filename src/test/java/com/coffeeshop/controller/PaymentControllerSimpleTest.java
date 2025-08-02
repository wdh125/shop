package com.coffeeshop.controller;

import com.coffeeshop.service.PaymentService;  
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple unit tests for PaymentController to verify security and basic functionality
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Get all payments - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllPayments_AdminAccess() throws Exception {
        // Given
        when(paymentService.getAllAdminPaymentDTOs()).thenReturn(Collections.emptyList());

        // When & Then  
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get all payments - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getAllPayments_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get all payments - Unauthorized access")
    void getAllPayments_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get my payments - Customer access")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getMyPayments_CustomerAccess() throws Exception {
        // Given
        when(paymentService.getCustomerPaymentDTOsByUsername("customer")).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/payments/my-payments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get my payments - Unauthorized access")
    void getMyPayments_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/my-payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get payments by customer - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentsByCustomer_AdminAccess() throws Exception {
        // Given
        when(paymentService.getCustomerPaymentDTOsByCustomerId(1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/payments/by-customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get payments by customer - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getPaymentsByCustomer_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/by-customer/1"))
                .andExpect(status().isForbidden());
    }
}