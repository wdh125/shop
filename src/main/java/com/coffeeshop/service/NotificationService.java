package com.coffeeshop.service;

import com.coffeeshop.dto.notification.request.NotificationCreateRequestDTO;
import com.coffeeshop.dto.notification.response.NotificationListResponseDTO;
import com.coffeeshop.dto.notification.response.NotificationResponseDTO;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Payment;
import com.coffeeshop.entity.Reservation;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.NotificationType;

/**
 * Notification service interface for managing user notifications
 */
public interface NotificationService {

    /**
     * Create a new notification
     */
    NotificationResponseDTO createNotification(NotificationCreateRequestDTO requestDTO);

    /**
     * Get notifications for a specific user with pagination
     */
    NotificationListResponseDTO getUserNotifications(Integer userId, int page, int size, Boolean onlyUnread);

    /**
     * Mark a specific notification as read
     */
    void markAsRead(Integer notificationId, Integer userId);

    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Integer userId);

    /**
     * Get unread notification count for a user
     */
    long getUnreadCount(Integer userId);

    /**
     * Helper method to create order-related notifications
     */
    void createOrderNotification(User user, Order order, NotificationType type, String title, String message);

    /**
     * Helper method to create payment-related notifications
     */
    void createPaymentNotification(User user, Payment payment, NotificationType type, String title, String message);

    /**
     * Helper method to create reservation-related notifications
     */
    void createReservationNotification(User user, Reservation reservation, NotificationType type, String title, String message);

    /**
     * Check if the authenticated user can access notifications for the given user ID
     */
    boolean canAccessUserNotifications(Integer userId, org.springframework.security.core.Authentication authentication);
}