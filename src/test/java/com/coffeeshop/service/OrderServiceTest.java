package com.coffeeshop.service;

import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.coffeeshop.entity.*;
import com.coffeeshop.enums.*;
import com.coffeeshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService
 * Tests order creation, status updates, and filtering functionality
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private TableRepository tableRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private SettingService settingService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private OrderService orderService;
    
    private User testUser;
    private TableEntity testTable;
    private Product testProduct;
    private Order testOrder;
    private Reservation testReservation;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(UserRole.ROLE_CUSTOMER);
        
        testTable = new TableEntity();
        testTable.setId(1);
        testTable.setTableNumber("T01");
        testTable.setLocation("Main Floor");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        
        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("Espresso");
        testProduct.setPrice(new BigDecimal("50000"));
        testProduct.setIsAvailable(true);
        
        testReservation = new Reservation();
        testReservation.setId(1);
        testReservation.setTable(testTable);
        testReservation.setCustomer(testUser);
        testReservation.setStatus(ReservationStatus.CONFIRMED);
        
        testOrder = new Order();
        testOrder.setId(1);
        testOrder.setOrderNumber("ORD-123456789");
        testOrder.setCustomer(testUser);
        testOrder.setTable(testTable);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentStatus(PaymentStatus.UNPAID);
        testOrder.setSubtotal(new BigDecimal("100000"));
        testOrder.setTaxAmount(new BigDecimal("10000"));
        testOrder.setTotalAmount(new BigDecimal("110000"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }
    
    private OrderItem createTestOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1);
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(new BigDecimal("50000"));
        orderItem.setTotalPrice(new BigDecimal("50000"));
        return orderItem;
    }
    
    @Test
    @DisplayName("Get order by ID should return order when exists")
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        
        // Act
        Order result = orderService.getOrderById(1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-123456789");
        
        verify(orderRepository).findById(1);
    }
    
    @Test
    @DisplayName("Get order by ID should throw exception when order not found")
    void getOrderById_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(999))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Order not found with id: 999");
        
        verify(orderRepository).findById(999);
    }
    
    @Test
    @DisplayName("Create order with items should create order successfully")
    void createOrderWithItems_WithValidData_ShouldCreateOrder() {
        // Arrange
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1);
        itemDTO.setQuantity(2);
        
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setTableId(1);
        request.setNote("Test order");
        request.setItems(Arrays.asList(itemDTO));
        
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(settingService.getTaxRate()).thenReturn(new BigDecimal("0.1"));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(new OrderItem()));
        
        // Act
        Order result = orderService.createOrderWithItems(request, testUser);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(tableRepository).findById(1);
        verify(productRepository).findById(1);
        verify(settingService).getTaxRate();
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(notificationService).createOrderNotification(
            eq(testUser), any(Order.class), eq(NotificationType.ORDER_CREATED), 
            anyString(), anyString());
    }
    
    @Test
    @DisplayName("Create order with reservation should use reservation's table")
    void createOrderWithItems_WithReservation_ShouldUseReservationTable() {
        // Arrange
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(1);
        itemDTO.setQuantity(1);
        
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setReservationId(1);
        request.setItems(Arrays.asList(itemDTO));
        
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(orderRepository.findByReservation_Id(1)).thenReturn(null);
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(settingService.getTaxRate()).thenReturn(new BigDecimal("0.1"));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(anyList())).thenReturn(Arrays.asList(new OrderItem()));
        
        // Act
        Order result = orderService.createOrderWithItems(request, testUser);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(reservationRepository).findById(1);
        verify(orderRepository).findByReservation_Id(1);
        verify(productRepository).findById(1);
        verify(settingService).getTaxRate();
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }
    
    @Test
    @DisplayName("Create order with cancelled reservation should throw exception")
    void createOrderWithItems_WithCancelledReservation_ShouldThrowException() {
        // Arrange
        testReservation.setStatus(ReservationStatus.CANCELLED);
        
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setReservationId(1);
        request.setItems(Arrays.asList(new OrderItemDTO()));
        
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrderWithItems(request, testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Reservation is cancelled, cannot create order");
        
        verify(reservationRepository).findById(1);
        verifyNoInteractions(orderRepository, productRepository);
    }
    
    @Test
    @DisplayName("Create order with existing reservation order should throw exception")
    void createOrderWithItems_WithExistingReservationOrder_ShouldThrowException() {
        // Arrange
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setReservationId(1);
        request.setItems(Arrays.asList(new OrderItemDTO()));
        
        when(reservationRepository.findById(1)).thenReturn(Optional.of(testReservation));
        when(orderRepository.findByReservation_Id(1)).thenReturn(testOrder);
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrderWithItems(request, testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("This reservation already has an order");
        
        verify(reservationRepository).findById(1);
        verify(orderRepository).findByReservation_Id(1);
    }
    
    @Test
    @DisplayName("Create order without table or reservation should throw exception")
    void createOrderWithItems_WithoutTableOrReservation_ShouldThrowException() {
        // Arrange
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setItems(Arrays.asList(new OrderItemDTO()));
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrderWithItems(request, testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TableId or reservationId must be provided");
        
        verifyNoInteractions(tableRepository, reservationRepository);
    }
    
    @Test
    @DisplayName("Update order status should update status and create notification")
    void updateOrderStatus_WithValidData_ShouldUpdateStatusAndCreateNotification() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // Act
        Order result = orderService.updateOrderStatus(1, OrderStatus.PREPARING);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(orderRepository).findById(1);
        verify(orderRepository).save(argThat(order -> 
            order.getStatus().equals(OrderStatus.PREPARING)
        ));
        verify(notificationService).createOrderNotification(
            eq(testUser), any(Order.class), eq(NotificationType.ORDER_STATUS_CHANGED), 
            anyString(), anyString());
    }
    
    @Test
    @DisplayName("Update payment method should update payment method")
    void updatePaymentMethod_WithValidData_ShouldUpdatePaymentMethod() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // Act
        Order result = orderService.updatePaymentMethod(1, PaymentMethod.CASH);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(orderRepository).findById(1);
        verify(orderRepository).save(argThat(order -> 
            order.getPaymentMethod().equals(PaymentMethod.CASH)
        ));
    }
    
    @Test
    @DisplayName("Filter orders by status should return filtered orders")
    void filterOrdersByStatus_WithValidStatus_ShouldReturnFilteredOrders() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(expectedOrders);
        
        // Act
        List<Order> result = orderService.filterOrdersByStatus(OrderStatus.PENDING);
        
        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }
    
    @Test
    @DisplayName("Filter orders by table should return orders for specific table")
    void filterOrdersByTable_WithValidTableId_ShouldReturnFilteredOrders() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(orderRepository.findByTable(testTable)).thenReturn(expectedOrders);
        
        // Act
        List<Order> result = orderService.filterOrdersByTable(1);
        
        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        
        verify(tableRepository).findById(1);
        verify(orderRepository).findByTable(testTable);
    }
    
    @Test
    @DisplayName("Filter orders by date range should return orders within range")
    void filterOrdersByDateRange_WithValidRange_ShouldReturnFilteredOrders() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findByCreatedAtBetween(start, end)).thenReturn(expectedOrders);
        
        // Act
        List<Order> result = orderService.filterOrdersByDateRange(start, end);
        
        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        
        verify(orderRepository).findByCreatedAtBetween(start, end);
    }
    
    @Test
    @DisplayName("Get customer orders by username should return orders for user")
    void getCustomerOrdersByUsername_WithValidUsername_ShouldReturnOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByCustomerOrderByCreatedAtDesc(testUser)).thenReturn(orders);
        when(orderItemRepository.findByOrder_Id(1)).thenReturn(Arrays.asList(createTestOrderItem()));
        
        // Act
        List<CustomerOrderResponseDTO> result = orderService.getCustomerOrdersByUsername("testuser");
        
        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        
        verify(userRepository).findByUsername("testuser");
        verify(orderRepository).findByCustomerOrderByCreatedAtDesc(testUser);
    }
    
    @Test
    @DisplayName("Get customer orders with invalid username should throw exception")
    void getCustomerOrdersByUsername_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.getCustomerOrdersByUsername("invaliduser"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found with username: invaliduser");
        
        verify(userRepository).findByUsername("invaliduser");
        verifyNoInteractions(orderRepository);
    }
    
    @Test
    @DisplayName("Save order should set timestamps correctly")
    void saveOrder_WithNewOrder_ShouldSetTimestamps() {
        // Arrange
        Order newOrder = new Order();
        newOrder.setOrderNumber("NEW-ORDER");
        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);
        
        // Act
        Order result = orderService.saveOrder(newOrder);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(orderRepository).save(argThat(order -> 
            order.getCreatedAt() != null && 
            order.getUpdatedAt() != null
        ));
    }
    
    @Test
    @DisplayName("Save existing order should update timestamp only")
    void saveOrder_WithExistingOrder_ShouldUpdateTimestampOnly() {
        // Arrange
        testOrder.setCreatedAt(LocalDateTime.now().minusHours(1));
        LocalDateTime originalCreatedAt = testOrder.getCreatedAt();
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // Act
        Order result = orderService.saveOrder(testOrder);
        
        // Assert
        assertThat(result).isNotNull();
        
        verify(orderRepository).save(argThat(order -> 
            order.getCreatedAt().equals(originalCreatedAt) && 
            order.getUpdatedAt() != null
        ));
    }
}