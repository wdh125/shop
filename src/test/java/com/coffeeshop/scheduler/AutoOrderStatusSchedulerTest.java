package com.coffeeshop.scheduler;

import com.coffeeshop.entity.Order;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.test.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AutoOrderStatusScheduler
 */
@ExtendWith(MockitoExtension.class)
class AutoOrderStatusSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SchedulerConfig schedulerConfig;

    @InjectMocks
    private AutoOrderStatusScheduler autoOrderStatusScheduler;

    private Order testOrder;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        testOrder.setId(1);
        testOrder.setCreatedAt(now.minusMinutes(30));
        testOrder.setUpdatedAt(now.minusMinutes(30));

        // Set default scheduler config values
        schedulerConfig.orderPendingToCancelledMinutes = 15;
        schedulerConfig.orderPreparingToReadyMinutes = 10;
        schedulerConfig.orderServedToCompletedMinutes = 30;
    }

    @Test
    @DisplayName("Auto cancel pending orders - Should cancel old pending orders")
    void autoCancelPendingOrders_ShouldCancelOldPendingOrders() {
        // Given
        Order oldPendingOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        oldPendingOrder.setId(1);
        oldPendingOrder.setCreatedAt(now.minusMinutes(20)); // 20 minutes ago
        oldPendingOrder.setUpdatedAt(now.minusMinutes(20));

        Order recentPendingOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser2", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        recentPendingOrder.setId(2);
        recentPendingOrder.setCreatedAt(now.minusMinutes(5)); // 5 minutes ago
        recentPendingOrder.setUpdatedAt(now.minusMinutes(5));

        List<Order> pendingOrders = Arrays.asList(oldPendingOrder, recentPendingOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);

        // When
        autoOrderStatusScheduler.autoCancelPendingOrders();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, times(1)).save(argThat(order -> 
            order.getId().equals(1) && order.getStatus() == OrderStatus.CANCELLED
        ));
        verify(orderRepository, never()).save(argThat(order -> 
            order.getId().equals(2)
        ));
    }

    @Test
    @DisplayName("Auto cancel pending orders - No orders to cancel")
    void autoCancelPendingOrders_NoOrdersToCancel() {
        // Given
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Collections.emptyList());

        // When
        autoOrderStatusScheduler.autoCancelPendingOrders();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Auto cancel pending orders - Recent orders not cancelled")
    void autoCancelPendingOrders_RecentOrdersNotCancelled() {
        // Given
        Order recentOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        recentOrder.setCreatedAt(now.minusMinutes(5)); // Only 5 minutes ago

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Arrays.asList(recentOrder));

        // When
        autoOrderStatusScheduler.autoCancelPendingOrders();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Auto pending paid to preparing - Should move paid orders to preparing")
    void autoPendingPaidToPreparing_ShouldMovePaidOrdersToPreparing() {
        // Given
        Order paidPendingOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        paidPendingOrder.setId(1);
        paidPendingOrder.setPaymentStatus(PaymentStatus.PAID);
        paidPendingOrder.setUpdatedAt(now.minusMinutes(15)); // 15 minutes ago

        Order unpaidPendingOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser2", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        unpaidPendingOrder.setId(2);
        unpaidPendingOrder.setPaymentStatus(PaymentStatus.UNPAID);
        unpaidPendingOrder.setUpdatedAt(now.minusMinutes(15));

        List<Order> pendingOrders = Arrays.asList(paidPendingOrder, unpaidPendingOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);

        // When
        autoOrderStatusScheduler.autoPendingPaidToPreparing();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, times(1)).save(argThat(order -> 
            order.getId().equals(1) && order.getStatus() == OrderStatus.PREPARING
        ));
        verify(orderRepository, never()).save(argThat(order -> 
            order.getId().equals(2)
        ));
    }

    @Test
    @DisplayName("Auto pending paid to preparing - Recent paid orders not moved")
    void autoPendingPaidToPreparing_RecentPaidOrdersNotMoved() {
        // Given
        Order recentPaidOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        recentPaidOrder.setPaymentStatus(PaymentStatus.PAID);
        recentPaidOrder.setUpdatedAt(now.minusMinutes(5)); // Only 5 minutes ago

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Arrays.asList(recentPaidOrder));

        // When
        autoOrderStatusScheduler.autoPendingPaidToPreparing();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Auto served to completed - Should complete old served orders")
    void autoServedToCompleted_ShouldCompleteOldServedOrders() {
        // Given
        Order oldServedOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.SERVED
        );
        oldServedOrder.setId(1);
        oldServedOrder.setUpdatedAt(now.minusMinutes(45)); // 45 minutes ago

        Order recentServedOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser2", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.SERVED
        );
        recentServedOrder.setId(2);
        recentServedOrder.setUpdatedAt(now.minusMinutes(15)); // 15 minutes ago

        List<Order> servedOrders = Arrays.asList(oldServedOrder, recentServedOrder);
        when(orderRepository.findByStatus(OrderStatus.SERVED)).thenReturn(servedOrders);

        // When
        autoOrderStatusScheduler.autoServedToCompleted();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.SERVED);
        verify(orderRepository, times(1)).save(argThat(order -> 
            order.getId().equals(1) && order.getStatus() == OrderStatus.COMPLETED
        ));
        verify(orderRepository, never()).save(argThat(order -> 
            order.getId().equals(2)
        ));
    }

    @Test
    @DisplayName("Auto served to completed - No served orders")
    void autoServedToCompleted_NoServedOrders() {
        // Given
        when(orderRepository.findByStatus(OrderStatus.SERVED)).thenReturn(Collections.emptyList());

        // When
        autoOrderStatusScheduler.autoServedToCompleted();

        // Then
        verify(orderRepository).findByStatus(OrderStatus.SERVED);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Scheduler updates timestamps correctly")
    void schedulerUpdatesTimestampsCorrectly() {
        // Given
        Order oldPendingOrder = TestDataFactory.createTestOrder(
                TestDataFactory.createTestUser("testuser", com.coffeeshop.enums.UserRole.ROLE_CUSTOMER),
                OrderStatus.PENDING
        );
        oldPendingOrder.setId(1);
        oldPendingOrder.setCreatedAt(now.minusMinutes(20));
        oldPendingOrder.setUpdatedAt(now.minusMinutes(20));

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Arrays.asList(oldPendingOrder));

        // When
        autoOrderStatusScheduler.autoCancelPendingOrders();

        // Then - Verify that updatedAt is set
        verify(orderRepository).save(argThat(order -> 
            order.getUpdatedAt().isAfter(now.minusMinutes(1))
        ));
    }
}