package com.coffeeshop.entity;

import com.coffeeshop.enums.NotificationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void testNotificationEntityCreation() {
        // Test creating a notification with entity relationships
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");
        user.setFullName("Test User");

        Order order = new Order();
        order.setId(1);
        order.setOrderNumber("ORD001");

        Notification notification = new Notification();
        notification.setType(NotificationType.ORDER_CREATED);
        notification.setUser(user);
        notification.setOrder(order);
        notification.setContent("Your order has been created");
        notification.setIsRead(false);

        // Verify that we're using entity relationships, not primitive IDs
        assertNotNull(notification.getUser());
        assertNotNull(notification.getOrder());
        assertNull(notification.getReservation()); // Should be null if not set
        assertEquals("testuser", notification.getUser().getUsername());
        assertEquals("ORD001", notification.getOrder().getOrderNumber());
        assertEquals(NotificationType.ORDER_CREATED, notification.getType());
        assertEquals("Your order has been created", notification.getContent());
        assertFalse(notification.getIsRead());
    }

    @Test
    void testNotificationWithReservation() {
        User user = new User();
        user.setId(1);
        user.setUsername("testuser");

        Reservation reservation = new Reservation();
        reservation.setId(1);

        Notification notification = new Notification();
        notification.setType(NotificationType.RESERVATION_CONFIRMED);
        notification.setUser(user);
        notification.setReservation(reservation);
        notification.setContent("Your reservation has been confirmed");

        // Verify entity relationships
        assertNotNull(notification.getUser());
        assertNotNull(notification.getReservation());
        assertNull(notification.getOrder()); // Should be null if not set
        assertEquals(1, notification.getReservation().getId());
    }

    @Test
    void testNotificationTypeEnum() {
        // Test that all notification types are available
        assertNotNull(NotificationType.ORDER_CREATED);
        assertNotNull(NotificationType.ORDER_CONFIRMED);
        assertNotNull(NotificationType.ORDER_READY);
        assertNotNull(NotificationType.ORDER_COMPLETED);
        assertNotNull(NotificationType.ORDER_CANCELLED);
        assertNotNull(NotificationType.RESERVATION_CONFIRMED);
        assertNotNull(NotificationType.RESERVATION_REMINDER);
        assertNotNull(NotificationType.RESERVATION_CANCELLED);
        assertNotNull(NotificationType.GENERAL);
    }
}