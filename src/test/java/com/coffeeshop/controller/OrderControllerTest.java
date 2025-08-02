package com.coffeeshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.coffeeshop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerOrderRequestDTO orderRequest;
    private CustomerOrderResponseDTO orderResponse;
    private OrderItemDTO orderItemDTO;

    @BeforeEach
    void setUp() {
        orderItemDTO = new OrderItemDTO();
        orderItemDTO.setProductId(1);
        orderItemDTO.setQuantity(2);

        orderRequest = new CustomerOrderRequestDTO();
        orderRequest.setTableId(1);
        orderRequest.setItems(Arrays.asList(orderItemDTO));
        orderRequest.setNote("Extra napkins");

        orderResponse = new CustomerOrderResponseDTO();
        orderResponse.setId(1);
        orderResponse.setOrderNumber("ORD-123456");
        // Note: Setting only fields that exist in the actual DTO
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_Success() throws Exception {
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456"));
    }

    @Test
    void testCreateOrderWithItems_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_InvalidData() throws Exception {
        CustomerOrderRequestDTO invalidRequest = new CustomerOrderRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_ServiceException() throws Exception {
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetMyOrders_Success() throws Exception {
        List<CustomerOrderResponseDTO> orders = Arrays.asList(orderResponse);
        when(orderService.getCustomerOrdersByUsername("testuser")).thenReturn(orders);

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123456"));
    }

    @Test
    void testGetMyOrders_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetMyOrders_EmptyList() throws Exception {
        when(orderService.getCustomerOrdersByUsername("testuser")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_WithReservation() throws Exception {
        orderRequest.setReservationId(1);
        
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_EmptyItems() throws Exception {
        orderRequest.setItems(Arrays.asList()); // Empty items list

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }
}