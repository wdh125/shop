package com.coffeeshop.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.coffeeshop.dto.notification.request.NotificationCreateRequestDTO;
import com.coffeeshop.dto.notification.response.NotificationResponseDTO;
import com.coffeeshop.entity.Notification;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.repository.NotificationRepository;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.PaymentRepository;
import com.coffeeshop.repository.ReservationRepository;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.service.impl.NotificationServiceImpl;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Order testOrder;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");

        testOrder = new Order();
        testOrder.setId(1);
        testOrder.setOrderNumber("ORD-123456");

        testNotification = new Notification();
        testNotification.setId(1);
        testNotification.setUser(testUser);
        testNotification.setType(NotificationType.ORDER_CREATED);
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("Test Message");
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setRelatedOrder(testOrder);
    }

    @Test
    void testCreateNotification() {
        // Arrange
        NotificationCreateRequestDTO requestDTO = new NotificationCreateRequestDTO();
        requestDTO.setUserId(1);
        requestDTO.setType(NotificationType.ORDER_CREATED);
        requestDTO.setTitle("Test Notification");
        requestDTO.setMessage("Test Message");
        requestDTO.setRelatedOrderId(1);

        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(orderRepository.findById(1)).thenReturn(Optional.of(testOrder));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        NotificationResponseDTO result = notificationService.createNotification(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(NotificationType.ORDER_CREATED, result.getType());
        assertEquals("Test Notification", result.getTitle());
        assertEquals("Test Message", result.getMessage());
        assertEquals(false, result.getIsRead());
        assertEquals(1, result.getRelatedOrderId());
        assertEquals("ORD-123456", result.getRelatedOrderNumber());

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateOrderNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.createOrderNotification(
                testUser,
                testOrder,
                NotificationType.ORDER_CREATED,
                "Order Created",
                "Your order has been created"
        );

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetUserNotifications() {
        // Arrange
        List<Notification> notifications = List.of(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications, PageRequest.of(0, 20), 1);
        
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any())).thenReturn(notificationPage);
        when(notificationRepository.countByUserAndIsReadFalse(testUser)).thenReturn(1L);

        // Act
        var result = notificationService.getUserNotifications(1, 0, 20, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNotifications().size());
        assertEquals(1L, result.getUnreadCount());
        assertEquals(1, result.getTotalPages());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void testMarkAsRead() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(notificationRepository.markAsReadById(1, testUser)).thenReturn(1);

        // Act & Assert
        assertDoesNotThrow(() -> notificationService.markAsRead(1, 1));
        verify(notificationRepository).markAsReadById(1, testUser);
    }

    @Test
    void testGetUnreadCount() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByUserAndIsReadFalse(testUser)).thenReturn(5L);

        // Act
        long result = notificationService.getUnreadCount(1);

        // Assert
        assertEquals(5L, result);
    }

    @Test
    void testCanAccessUserNotifications_NullAuthentication() {
        // Act
        boolean result = notificationService.canAccessUserNotifications(1, null);

        // Assert
        assertFalse(result);
    }
}