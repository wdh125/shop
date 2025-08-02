package com.coffeeshop.controller;

import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.coffeeshop.service.OrderService;
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
 * Integration tests for OrderController
 * Tests REST API endpoints for order management
 */
@WebMvcTest(OrderController.class)
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerOrderRequestDTO validOrderRequest;
    private CustomerOrderResponseDTO orderResponse;

    @BeforeEach
    void setUp() {
        // Setup order request DTO
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1);
        itemDTO.setQuantity(2);

        validOrderRequest = new CustomerOrderRequestDTO();
        validOrderRequest.setTableId(1);
        validOrderRequest.setNote("Test order note");
        validOrderRequest.setItems(Arrays.asList(itemDTO));

        // Setup order response DTO
        orderResponse = new CustomerOrderResponseDTO();
        orderResponse.setId(1);
        orderResponse.setOrderNumber("ORD-123456789");
        orderResponse.setStatus("PENDING");
        orderResponse.setPaymentStatus("UNPAID");
        orderResponse.setNotes("Test order note");
        orderResponse.setCreatedAt(LocalDateTime.now());
        orderResponse.setUpdatedAt(LocalDateTime.now());

        // Setup table info
        CustomerOrderResponseDTO.TableInfo tableInfo = new CustomerOrderResponseDTO.TableInfo();
        tableInfo.setId(1);
        tableInfo.setTableNumber("T01");
        tableInfo.setLocation("Main Floor");
        orderResponse.setTable(tableInfo);

        // Setup order items
        CustomerOrderResponseDTO.OrderItemInfo itemInfo = new CustomerOrderResponseDTO.OrderItemInfo();
        itemInfo.setId(1);
        itemInfo.setProductName("Espresso");
        itemInfo.setQuantity(2);
        itemInfo.setUnitPrice(50000.0);
        itemInfo.setTotalPrice(100000.0);
        orderResponse.setItems(Arrays.asList(itemInfo));
    }

    @Test
    @DisplayName("POST /api/orders with authenticated user should create order and return 200")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithAuthenticatedUser_ShouldReturn200() throws Exception {
        // Arrange
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value("ORD-123456789"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentStatus").value("UNPAID"))
                .andExpect(jsonPath("$.notes").value("Test order note"))
                .andExpect(jsonPath("$.table.id").value(1))
                .andExpect(jsonPath("$.table.tableNumber").value("T01"))
                .andExpect(jsonPath("$.table.location").value("Main Floor"))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].productName").value("Espresso"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(50000.0))
                .andExpect(jsonPath("$.items[0].totalPrice").value(100000.0));

        verify(orderService).createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser"));
    }

    @Test
    @DisplayName("POST /api/orders without authentication should return 401")
    void createOrder_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("POST /api/orders with invalid request should return 400")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithInvalidRequest_ShouldReturn400() throws Exception {
        // Arrange - Create invalid request (missing required fields)
        CustomerOrderRequestDTO invalidRequest = new CustomerOrderRequestDTO();
        invalidRequest.setItems(null); // Invalid: items cannot be null

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("POST /api/orders with service exception should return appropriate error status")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithServiceException_ShouldReturnErrorStatus() throws Exception {
        // Arrange
        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Table not found"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isInternalServerError());

        verify(orderService).createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser"));
    }

    @Test
    @DisplayName("GET /api/orders/my-orders with authenticated user should return user orders")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void getMyOrders_WithAuthenticatedUser_ShouldReturnOrders() throws Exception {
        // Arrange
        List<CustomerOrderResponseDTO> orders = Arrays.asList(orderResponse);
        when(orderService.getCustomerOrdersByUsername("testuser")).thenReturn(orders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-123456789"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].paymentStatus").value("UNPAID"))
                .andExpect(jsonPath("$[0].table.tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].items").isArray())
                .andExpect(jsonPath("$[0].items[0].productName").value("Espresso"));

        verify(orderService).getCustomerOrdersByUsername("testuser");
    }

    @Test
    @DisplayName("GET /api/orders/my-orders without authentication should return 401")
    void getMyOrders_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("GET /api/orders/my-orders with empty orders should return empty array")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void getMyOrders_WithEmptyOrders_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(orderService.getCustomerOrdersByUsername("testuser")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(orderService).getCustomerOrdersByUsername("testuser");
    }

    @Test
    @DisplayName("POST /api/orders with reservation ID should create order with reservation")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithReservationId_ShouldCreateOrderWithReservation() throws Exception {
        // Arrange
        validOrderRequest.setTableId(null);
        validOrderRequest.setReservationId(1);
        orderResponse.setReservationId(1);

        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1));

        verify(orderService).createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser"));
    }

    @Test
    @DisplayName("POST /api/orders with multiple items should handle multiple items correctly")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithMultipleItems_ShouldHandleCorrectly() throws Exception {
        // Arrange
        OrderItemDTO item1 = new OrderItemDTO();
        item1.setProductId(1);
        item1.setQuantity(2);

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setProductId(2);
        item2.setQuantity(1);

        validOrderRequest.setItems(Arrays.asList(item1, item2));

        // Setup response with multiple items
        CustomerOrderResponseDTO.OrderItemInfo itemInfo1 = new CustomerOrderResponseDTO.OrderItemInfo();
        itemInfo1.setId(1);
        itemInfo1.setProductName("Espresso");
        itemInfo1.setQuantity(2);

        CustomerOrderResponseDTO.OrderItemInfo itemInfo2 = new CustomerOrderResponseDTO.OrderItemInfo();
        itemInfo2.setId(2);
        itemInfo2.setProductName("Cappuccino");
        itemInfo2.setQuantity(1);

        orderResponse.setItems(Arrays.asList(itemInfo1, itemInfo2));

        when(orderService.createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser")))
                .thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").hasJsonPath())
                .andExpect(jsonPath("$.items[0].productName").value("Espresso"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[1].productName").value("Cappuccino"))
                .andExpect(jsonPath("$.items[1].quantity").value(1));

        verify(orderService).createOrderWithItems(any(CustomerOrderRequestDTO.class), eq("testuser"));
    }

    @Test
    @DisplayName("POST /api/orders without CSRF token should return 403")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithoutCsrfToken_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOrderRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("POST /api/orders with malformed JSON should return 400")
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void createOrder_WithMalformedJson_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}