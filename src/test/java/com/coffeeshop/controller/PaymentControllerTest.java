package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.request.AdminPaymentStatusUpdateDTO;
import com.coffeeshop.dto.admin.response.AdminPaymentResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerPaymentResponseDTO;
import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for PaymentController
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerPaymentRequestDTO customerPaymentRequest;
    private CustomerPaymentResponseDTO customerPaymentResponse;
    private AdminPaymentResponseDTO adminPaymentResponse;
    private AdminPaymentStatusUpdateDTO statusUpdateRequest;

    @BeforeEach
    void setUp() {
        // Setup customer payment request
        customerPaymentRequest = new CustomerPaymentRequestDTO();
        customerPaymentRequest.setOrderId(1);
        customerPaymentRequest.setPaymentMethod(PaymentMethod.CASH);

        // Setup customer payment response
        customerPaymentResponse = new CustomerPaymentResponseDTO();
        customerPaymentResponse.setId(1);
        customerPaymentResponse.setOrderId(1);
        customerPaymentResponse.setAmount(BigDecimal.valueOf(50000));
        customerPaymentResponse.setPaymentMethod(PaymentMethod.CASH);
        customerPaymentResponse.setStatus(PaymentStatus.COMPLETED);
        customerPaymentResponse.setTransactionId("TXN_123456");
        customerPaymentResponse.setCreatedAt(LocalDateTime.now());

        // Setup admin payment response
        adminPaymentResponse = new AdminPaymentResponseDTO();
        adminPaymentResponse.setId(1);
        adminPaymentResponse.setOrderId(1);
        adminPaymentResponse.setCustomerId(1);
        adminPaymentResponse.setAmount(BigDecimal.valueOf(50000));
        adminPaymentResponse.setPaymentMethod(PaymentMethod.CASH);
        adminPaymentResponse.setStatus(PaymentStatus.COMPLETED);
        adminPaymentResponse.setTransactionId("TXN_123456");
        adminPaymentResponse.setCreatedAt(LocalDateTime.now());
        adminPaymentResponse.setUpdatedAt(LocalDateTime.now());

        // Setup status update request
        statusUpdateRequest = new AdminPaymentStatusUpdateDTO();
        statusUpdateRequest.setStatus(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("Create payment - Success")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void createPayment_Success() throws Exception {
        // Given
        when(paymentService.createPaymentForCustomer(any(CustomerPaymentRequestDTO.class), eq("customer")))
                .thenReturn(customerPaymentResponse);

        // When & Then
        mockMvc.perform(post("/api/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transactionId").value("TXN_123456"));
    }

    @Test
    @DisplayName("Create payment - Unauthorized access")
    void createPayment_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerPaymentRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create payment - Invalid request body")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void createPayment_InvalidRequest() throws Exception {
        // Given - invalid request (missing required fields)
        CustomerPaymentRequestDTO invalidRequest = new CustomerPaymentRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/payments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get all payments - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllPayments_AdminAccess() throws Exception {
        // Given
        List<AdminPaymentResponseDTO> payments = Arrays.asList(adminPaymentResponse);
        when(paymentService.getAllAdminPaymentDTOs()).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].amount").value(50000));
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
    @DisplayName("Get payment by ID - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentById_AdminAccess() throws Exception {
        // Given
        when(paymentService.getAdminPaymentDTOById(1)).thenReturn(adminPaymentResponse);

        // When & Then
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.transactionId").value("TXN_123456"));
    }

    @Test
    @DisplayName("Get payment by ID - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getPaymentById_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get my payments - Customer access")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getMyPayments_CustomerAccess() throws Exception {
        // Given
        List<CustomerPaymentResponseDTO> payments = Arrays.asList(customerPaymentResponse);
        when(paymentService.getCustomerPaymentDTOsByUsername("customer")).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments/my-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].amount").value(50000));
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
        List<CustomerPaymentResponseDTO> payments = Arrays.asList(customerPaymentResponse);
        when(paymentService.getCustomerPaymentDTOsByCustomerId(1)).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments/by-customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1));
    }

    @Test
    @DisplayName("Get payments by customer - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getPaymentsByCustomer_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/by-customer/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get payments by order - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentsByOrder_AdminAccess() throws Exception {
        // Given
        List<CustomerPaymentResponseDTO> payments = Arrays.asList(customerPaymentResponse);
        when(paymentService.getCustomerPaymentDTOsByOrderId(1)).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments/by-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1));
    }

    @Test
    @DisplayName("Update payment status - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updatePaymentStatus_AdminAccess() throws Exception {
        // Given
        AdminPaymentResponseDTO updatedPayment = new AdminPaymentResponseDTO();
        updatedPayment.setId(1);
        updatedPayment.setStatus(PaymentStatus.REFUNDED);
        when(paymentService.updatePaymentStatusByAdminAndReturnDTO(eq(1), any(AdminPaymentStatusUpdateDTO.class)))
                .thenReturn(updatedPayment);

        // When & Then
        mockMvc.perform(patch("/api/payments/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    @DisplayName("Update payment status - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void updatePaymentStatus_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/payments/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update payment status - Invalid request body")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updatePaymentStatus_InvalidRequest() throws Exception {
        // Given - invalid request (missing required fields)
        AdminPaymentStatusUpdateDTO invalidRequest = new AdminPaymentStatusUpdateDTO();

        // When & Then
        mockMvc.perform(patch("/api/payments/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get payment by invalid ID - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPaymentById_InvalidId() throws Exception {
        // When & Then - Test with non-numeric ID
        mockMvc.perform(get("/api/payments/invalid"))
                .andExpect(status().isBadRequest());
    }
}