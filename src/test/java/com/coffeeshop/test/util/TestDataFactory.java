package com.coffeeshop.test.util;

import com.coffeeshop.entity.*;
import com.coffeeshop.enums.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Utility class for creating test data objects
 */
public class TestDataFactory {

    public static User createTestUser(String username, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("encodedPassword");
        user.setFullName("Test " + username);
        user.setPhone("1234567890");
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static Category createTestCategory(String name, int displayOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Test " + name);
        category.setDisplayOrder(displayOrder);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    public static Product createTestProduct(String name, Category category, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("Test " + name);
        product.setPrice(price);
        product.setImageUrl(name.toLowerCase() + ".jpg");
        product.setIsAvailable(true);
        product.setPreparationTime(10);
        product.setDisplayOrder(1);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    public static TableEntity createTestTable(String tableNumber, int capacity, TableStatus status) {
        TableEntity table = new TableEntity();
        table.setTableNumber(tableNumber);
        table.setLocation("Test Floor");
        table.setCapacity(capacity);
        table.setStatus(status);
        table.setIsActive(true);
        table.setCreatedAt(LocalDateTime.now());
        table.setUpdatedAt(LocalDateTime.now());
        return table;
    }

    public static Order createTestOrder(User user, OrderStatus status) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.valueOf(100000));
        order.setOrderNotes("Test order");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    public static Reservation createTestReservation(User user, TableEntity table) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setTable(table);
        reservation.setReservationTime(LocalDateTime.now().plusHours(2));
        reservation.setPartySize(4);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setSpecialRequests("Test reservation");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        return reservation;
    }

    public static Payment createTestPayment(Order order, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("TEST_TXN_" + System.currentTimeMillis());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }
}