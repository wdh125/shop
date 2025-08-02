package com.coffeeshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.coffeeshop.dto.admin.response.statistics.OrderStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.OverviewStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.RevenueStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopCustomersResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopProductsResponseDTO;
import com.coffeeshop.entity.Order;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.repository.OrderItemRepository;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.service.impl.StatisticsServiceImpl;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private Order paidOrder;
    private Order unpaidOrder;

    @BeforeEach
    void setUp() {
        paidOrder = new Order();
        paidOrder.setId(1);
        paidOrder.setOrderNumber("ORD-001");
        paidOrder.setTotalAmount(new BigDecimal("150000"));
        paidOrder.setPaymentStatus(PaymentStatus.PAID);

        unpaidOrder = new Order();
        unpaidOrder.setId(2);
        unpaidOrder.setOrderNumber("ORD-002");
        unpaidOrder.setTotalAmount(new BigDecimal("200000"));
        unpaidOrder.setPaymentStatus(PaymentStatus.UNPAID);
    }

    @Test
    void testGetOverviewStatistics_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(paidOrder, unpaidOrder);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(10L);
        when(productRepository.count()).thenReturn(5L);
        when(categoryRepository.count()).thenReturn(3L);

        // Act
        OverviewStatisticsResponseDTO result = statisticsService.getOverviewStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(150000.0, result.getTotalRevenue()); // Only paid orders
        assertEquals(2, result.getTotalOrders());
        assertEquals(10, result.getTotalCustomers());
        assertEquals(5, result.getTotalProducts());
        assertEquals(3, result.getTotalCategories());
        
        verify(orderRepository).findAll();
        verify(orderRepository).count();
        verify(userRepository).count();
        verify(productRepository).count();
        verify(categoryRepository).count();
    }

    @Test
    void testGetOverviewStatistics_NoOrders() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Arrays.asList());
        when(orderRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(productRepository.count()).thenReturn(0L);
        when(categoryRepository.count()).thenReturn(0L);

        // Act
        OverviewStatisticsResponseDTO result = statisticsService.getOverviewStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotalRevenue());
        assertEquals(0, result.getTotalOrders());
        assertEquals(0, result.getTotalCustomers());
        assertEquals(0, result.getTotalProducts());
        assertEquals(0, result.getTotalCategories());
    }

    @Test
    void testGetOverviewStatistics_OnlyUnpaidOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(unpaidOrder);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderRepository.count()).thenReturn(1L);
        when(userRepository.count()).thenReturn(5L);
        when(productRepository.count()).thenReturn(3L);
        when(categoryRepository.count()).thenReturn(2L);

        // Act
        OverviewStatisticsResponseDTO result = statisticsService.getOverviewStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotalRevenue()); // No paid orders
        assertEquals(1, result.getTotalOrders());
        assertEquals(5, result.getTotalCustomers());
        assertEquals(3, result.getTotalProducts());
        assertEquals(2, result.getTotalCategories());
    }

    @Test
    void testGetRevenueStatistics_ValidDateRange() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 31);

        // Act
        RevenueStatisticsResponseDTO result = statisticsService.getRevenueStatistics(from, to);

        // Assert
        assertNotNull(result);
        // Note: The actual implementation details would need to be checked
        // but this test ensures the method can be called without errors
    }

    @Test
    void testGetRevenueStatistics_InvalidDateRange() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 2, 1);
        LocalDate to = LocalDate.of(2024, 1, 31); // from > to

        // Act & Assert
        // This test depends on whether the implementation validates date ranges
        assertDoesNotThrow(() -> {
            statisticsService.getRevenueStatistics(from, to);
        });
    }

    @Test
    void testGetOrderStatistics_ValidDateRange() {
        // Arrange
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 31);

        // Act
        OrderStatisticsResponseDTO result = statisticsService.getOrderStatistics(from, to);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetTopCustomers_ValidLimit() {
        // Arrange
        int limit = 5;

        // Act
        TopCustomersResponseDTO result = statisticsService.getTopCustomers(limit);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetTopCustomers_ZeroLimit() {
        // Arrange
        int limit = 0;

        // Act
        TopCustomersResponseDTO result = statisticsService.getTopCustomers(limit);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetTopCustomers_NegativeLimit() {
        // Arrange
        int limit = -5;

        // Act & Assert - negative limit should be handled gracefully
        assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.getTopCustomers(limit);
        });
    }

    @Test
    void testGetTopProducts_ValidLimit() {
        // Arrange
        int limit = 10;

        // Act
        TopProductsResponseDTO result = statisticsService.getTopProducts(limit);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetTopProducts_LargeLimit() {
        // Arrange
        int limit = 1000;

        // Act
        TopProductsResponseDTO result = statisticsService.getTopProducts(limit);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetRevenueStatistics_SameDate() {
        // Arrange
        LocalDate sameDate = LocalDate.of(2024, 1, 15);

        // Act
        RevenueStatisticsResponseDTO result = statisticsService.getRevenueStatistics(sameDate, sameDate);

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetOrderStatistics_SameDate() {
        // Arrange
        LocalDate sameDate = LocalDate.of(2024, 1, 15);

        // Act
        OrderStatisticsResponseDTO result = statisticsService.getOrderStatistics(sameDate, sameDate);

        // Assert
        assertNotNull(result);
    }
}