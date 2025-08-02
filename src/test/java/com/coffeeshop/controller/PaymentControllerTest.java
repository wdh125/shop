package com.coffeeshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.coffeeshop.dto.admin.request.AdminPaymentStatusUpdateDTO;
import com.coffeeshop.dto.admin.response.AdminPaymentResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerPaymentResponseDTO;
import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerPaymentRequestDTO paymentRequest;
    private CustomerPaymentResponseDTO customerPaymentResponse;
    private AdminPaymentResponseDTO adminPaymentResponse;
    private AdminPaymentStatusUpdateDTO statusUpdateRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new CustomerPaymentRequestDTO();
        paymentRequest.setOrderId(1);
        paymentRequest.setPaymentMethod(PaymentMethod.CASH);
        paymentRequest.setAmount(new BigDecimal("150000"));

        customerPaymentResponse = new CustomerPaymentResponseDTO();
        customerPaymentResponse.setId(1);
        customerPaymentResponse.setOrderId(1);
        customerPaymentResponse.setAmount(new BigDecimal("150000"));
        customerPaymentResponse.setPaymentMethod(PaymentMethod.CASH);
        customerPaymentResponse.setStatus(PaymentStatus.PENDING);
        customerPaymentResponse.setCreatedAt(LocalDateTime.now());

        adminPaymentResponse = new AdminPaymentResponseDTO();
        adminPaymentResponse.setId(1);
        adminPaymentResponse.setOrderId(1);
        adminPaymentResponse.setCustomerName("Test User");
        adminPaymentResponse.setAmount(new BigDecimal("150000"));
        adminPaymentResponse.setPaymentMethod(PaymentMethod.CASH);
        adminPaymentResponse.setStatus(PaymentStatus.PENDING);

        statusUpdateRequest = new AdminPaymentStatusUpdateDTO();
        statusUpdateRequest.setStatus(PaymentStatus.COMPLETED);
        statusUpdateRequest.setNotes("Payment verified");
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePayment_Success() throws Exception {
        when(paymentService.createPaymentForCustomer(any(CustomerPaymentRequestDTO.class), eq("testuser")))
                .thenReturn(customerPaymentResponse);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.amount").value(150000))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testCreatePayment_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePayment_InvalidData() throws Exception {
        CustomerPaymentRequestDTO invalidRequest = new CustomerPaymentRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePayment_InvalidAmount() throws Exception {
        CustomerPaymentRequestDTO invalidRequest = new CustomerPaymentRequestDTO();
        invalidRequest.setOrderId(1);
        invalidRequest.setPaymentMethod(PaymentMethod.CASH);
        invalidRequest.setAmount(new BigDecimal("-100")); // Negative amount

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreatePayment_OrderNotFound() throws Exception {
        when(paymentService.createPaymentForCustomer(any(CustomerPaymentRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser") 
    void testCreatePayment_UnauthorizedAccess() throws Exception {
        when(paymentService.createPaymentForCustomer(any(CustomerPaymentRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Unauthorized access to order"));

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllPayments_Success() throws Exception {
        List<AdminPaymentResponseDTO> payments = Arrays.asList(adminPaymentResponse);
        when(paymentService.getAllAdminPaymentDTOs()).thenReturn(payments);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllPayments_Forbidden() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPaymentById_Success() throws Exception {
        when(paymentService.getAdminPaymentDTOById(1)).thenReturn(adminPaymentResponse);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Test User"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPaymentById_NotFound() throws Exception {
        when(paymentService.getAdminPaymentDTOById(999))
                .thenThrow(new RuntimeException("Payment not found"));

        mockMvc.perform(get("/api/payments/999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetMyPayments_Success() throws Exception {
        List<CustomerPaymentResponseDTO> payments = Arrays.asList(customerPaymentResponse);
        when(paymentService.getCustomerPaymentDTOsByUsername("testuser")).thenReturn(payments);

        mockMvc.perform(get("/api/payments/my-payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetMyPayments_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/payments/my-payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPaymentsByCustomer_Success() throws Exception {
        List<CustomerPaymentResponseDTO> payments = Arrays.asList(customerPaymentResponse);
        when(paymentService.getCustomerPaymentDTOsByCustomerId(1)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/by-customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetPaymentsByCustomer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/payments/by-customer/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPaymentsByOrder_Success() throws Exception {
        List<CustomerPaymentResponseDTO> payments = Arrays.asList(customerPaymentResponse);
        when(paymentService.getCustomerPaymentDTOsByOrderId(1)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/by-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdatePaymentStatus_Success() throws Exception {
        AdminPaymentResponseDTO updatedPayment = new AdminPaymentResponseDTO();
        updatedPayment.setId(1);
        updatedPayment.setStatus(PaymentStatus.COMPLETED);
        
        when(paymentService.updatePaymentStatusByAdminAndReturnDTO(eq(1), any(AdminPaymentStatusUpdateDTO.class)))
                .thenReturn(updatedPayment);

        mockMvc.perform(patch("/api/payments/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdatePaymentStatus_Forbidden() throws Exception {
        mockMvc.perform(patch("/api/payments/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdatePaymentStatus_InvalidData() throws Exception {
        AdminPaymentStatusUpdateDTO invalidRequest = new AdminPaymentStatusUpdateDTO();
        // Missing required status field

        mockMvc.perform(patch("/api/payments/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdatePaymentStatus_PaymentNotFound() throws Exception {
        when(paymentService.updatePaymentStatusByAdminAndReturnDTO(eq(999), any(AdminPaymentStatusUpdateDTO.class)))
                .thenThrow(new RuntimeException("Payment not found"));

        mockMvc.perform(patch("/api/payments/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().is5xxServerError());
    }
}