package com.coffeeshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Product;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.OrderItemRepository;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.repository.ReservationRepository;
import com.coffeeshop.repository.TableRepository;
import com.coffeeshop.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SettingService settingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;
    private Product testProduct;
    private TableEntity testTable;
    private CustomerOrderRequestDTO orderRequest;
    private OrderItemDTO orderItemDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");

        testProduct = new Product();
        testProduct.setId(1);
        testProduct.setName("Americano");
        testProduct.setPrice(new BigDecimal("50000"));
        testProduct.setIsAvailable(true);

        testTable = new TableEntity();
        testTable.setId(1);
        testTable.setTableNumber("T001");

        testOrder = new Order();
        testOrder.setId(1);
        testOrder.setOrderNumber("ORD-123456");
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("100000"));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setTable(testTable); // Set the table relationship

        orderItemDTO = new OrderItemDTO();
        orderItemDTO.setProductId(1);
        orderItemDTO.setQuantity(2);

        orderRequest = new CustomerOrderRequestDTO();
        orderRequest.setTableId(1);
        orderRequest.setItems(Arrays.asList(orderItemDTO));
        orderRequest.setNote("Extra napkins");
    }

    @Test
    void testGetAllOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORD-123456", result.get(0).getOrderNumber());
        verify(orderRepository).findAll();
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("ORD-123456", result.getOrderNumber());
        verify(orderRepository).findById(1);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(999);
        });
        verify(orderRepository).findById(999);
    }

    @Test
    void testCreateOrderWithItems_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(settingService.getTaxRate()).thenReturn(new BigDecimal("0.10")); // 10% tax
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(any())).thenReturn(Arrays.asList());

        // Act
        CustomerOrderResponseDTO result = orderService.createOrderWithItems(orderRequest, "testuser");

        // Assert
        assertNotNull(result);
        verify(userRepository).findByUsername("testuser");
        verify(tableRepository).findById(1);
        verify(productRepository).findById(1);
        verify(settingService).getTaxRate();
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(any());
    }

    @Test
    void testCreateOrderWithItems_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderWithItems(orderRequest, "nonexistent");
        });
        verify(userRepository).findByUsername("nonexistent");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithItems_TableNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tableRepository.findById(999)).thenReturn(Optional.empty());

        orderRequest.setTableId(999);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderWithItems(orderRequest, "testuser");
        });
        verify(userRepository).findByUsername("testuser");
        verify(tableRepository).findById(999);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithItems_ProductNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderWithItems(orderRequest, "testuser");
        });
        verify(userRepository).findByUsername("testuser");
        verify(tableRepository).findById(1);
        verify(productRepository).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithItems_ProductNotAvailable() {
        // Arrange
        testProduct.setIsAvailable(false);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderWithItems(orderRequest, "testuser");
        });
        verify(productRepository).findById(1);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetCustomerOrdersByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByCustomerOrderByCreatedAtDesc(testUser)).thenReturn(Arrays.asList(testOrder));

        // Act
        List<CustomerOrderResponseDTO> result = orderService.getCustomerOrdersByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByUsername("testuser");
        verify(orderRepository).findByCustomerOrderByCreatedAtDesc(testUser);
    }

    @Test
    void testGetCustomerOrdersByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getCustomerOrdersByUsername("nonexistent");
        });
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void testFindOrderByReservationId_Success() {
        // Arrange
        when(orderRepository.findByReservation_Id(1)).thenReturn(testOrder);

        // Act
        Order result = orderService.findOrderByReservationId(1);

        // Assert
        assertNotNull(result);
        assertEquals("ORD-123456", result.getOrderNumber());
        verify(orderRepository).findByReservation_Id(1);
    }

    @Test
    void testCreateOrderWithItems_EmptyItems() {
        // Arrange
        orderRequest.setItems(Arrays.asList()); // Empty items list
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderWithItems(orderRequest, "testuser");
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrderWithItems_InvalidQuantity() {
        // Arrange
        orderItemDTO.setQuantity(0); // Invalid quantity
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderWithItems(orderRequest, "testuser");
        });
        verify(orderRepository, never()).save(any(Order.class));
    }
}