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

import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.request.OrderItemRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.customer.response.OrderItemResponseDTO;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerOrderRequestDTO orderRequest;
    private CustomerOrderResponseDTO orderResponse;
    private OrderItemRequestDTO orderItemRequest;
    private OrderItemResponseDTO orderItemResponse;

    @BeforeEach
    void setUp() {
        orderItemRequest = new OrderItemRequestDTO();
        orderItemRequest.setProductId(1);
        orderItemRequest.setQuantity(2);
        orderItemRequest.setSpecialInstructions("No sugar");

        orderItemResponse = new OrderItemResponseDTO();
        orderItemResponse.setId(1);
        orderItemResponse.setProductId(1);
        orderItemResponse.setProductName("Americano");
        orderItemResponse.setQuantity(2);
        orderItemResponse.setUnitPrice(new BigDecimal("50000"));
        orderItemResponse.setTotalPrice(new BigDecimal("100000"));
        orderItemResponse.setSpecialInstructions("No sugar");

        orderRequest = new CustomerOrderRequestDTO();
        orderRequest.setTableId(1);
        orderRequest.setOrderItems(Arrays.asList(orderItemRequest));
        orderRequest.setSpecialRequests("Extra napkins");

        orderResponse = new CustomerOrderResponseDTO();
        orderResponse.setId(1);
        orderResponse.setOrderNumber("ORD-123456");
        orderResponse.setTableNumber("T001");
        orderResponse.setStatus(OrderStatus.PENDING);
        orderResponse.setTotalAmount(new BigDecimal("100000"));
        orderResponse.setCreatedAt(LocalDateTime.now());
        orderResponse.setOrderItems(Arrays.asList(orderItemResponse));
        orderResponse.setSpecialRequests("Extra napkins");
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
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456"))
                .andExpect(jsonPath("$.tableNumber").value("T001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(100000))
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems[0].productName").value("Americano"))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.specialRequests").value("Extra napkins"));
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
    void testCreateOrderWithItems_EmptyOrderItems() throws Exception {
        CustomerOrderRequestDTO invalidRequest = new CustomerOrderRequestDTO();
        invalidRequest.setTableId(1);
        invalidRequest.setOrderItems(Arrays.asList()); // Empty list

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_InvalidQuantity() throws Exception {
        OrderItemRequestDTO invalidItem = new OrderItemRequestDTO();
        invalidItem.setProductId(1);
        invalidItem.setQuantity(0); // Invalid quantity

        CustomerOrderRequestDTO invalidRequest = new CustomerOrderRequestDTO();
        invalidRequest.setTableId(1);
        invalidRequest.setOrderItems(Arrays.asList(invalidItem));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_ProductNotFound() throws Exception {
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_TableNotAvailable() throws Exception {
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Table is not available"));

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
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123456"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
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
    void testCreateOrderWithItems_MultipleItems() throws Exception {
        OrderItemRequestDTO item1 = new OrderItemRequestDTO();
        item1.setProductId(1);
        item1.setQuantity(2);

        OrderItemRequestDTO item2 = new OrderItemRequestDTO();
        item2.setProductId(2);
        item2.setQuantity(1);

        CustomerOrderRequestDTO multiItemRequest = new CustomerOrderRequestDTO();
        multiItemRequest.setTableId(1);
        multiItemRequest.setOrderItems(Arrays.asList(item1, item2));

        CustomerOrderResponseDTO multiItemResponse = new CustomerOrderResponseDTO();
        multiItemResponse.setId(1);
        multiItemResponse.setOrderItems(Arrays.asList(orderItemResponse, orderItemResponse));
        multiItemResponse.setTotalAmount(new BigDecimal("200000"));

        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenReturn(multiItemResponse);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(multiItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItems").isArray())
                .andExpect(jsonPath("$.orderItems").value(org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(200000));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrderWithItems_LargeQuantity() throws Exception {
        OrderItemRequestDTO largeQuantityItem = new OrderItemRequestDTO();
        largeQuantityItem.setProductId(1);
        largeQuantityItem.setQuantity(100); // Large quantity

        CustomerOrderRequestDTO largeQuantityRequest = new CustomerOrderRequestDTO();
        largeQuantityRequest.setTableId(1);
        largeQuantityRequest.setOrderItems(Arrays.asList(largeQuantityItem));

        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Quantity exceeds available stock"));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeQuantityRequest)))
                .andExpect(status().is5xxServerError());
    }
}