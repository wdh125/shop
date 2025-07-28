package com.coffeeshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coffeeshop.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.PaymentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByTable(TableEntity table);
    List<Order> findByStatusAndTable(OrderStatus status, TableEntity table);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);
    List<Order> findByCustomer_Id(Integer customerId);
    List<Order> findByCustomerOrderByCreatedAtDesc(com.coffeeshop.entity.User customer);
    Order findByReservation_Id(Integer reservationId);
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);
}