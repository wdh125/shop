package com.coffeeshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coffeeshop.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.PaymentStatus;

// Repository interface cho entity Order
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByStatus(OrderStatus status); // Tìm đơn hàng theo trạng thái
    List<Order> findByTable(TableEntity table); // Tìm đơn hàng theo bàn
    List<Order> findByStatusAndTable(OrderStatus status, TableEntity table); // Tìm đơn hàng theo trạng thái và bàn
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end); // Tìm đơn hàng trong khoảng thời gian
    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end); // Tìm đơn hàng theo trạng thái và thời gian
    List<Order> findByCustomer_Id(Integer customerId); // Tìm đơn hàng theo customer ID
    List<Order> findByCustomerOrderByCreatedAtDesc(com.coffeeshop.entity.User customer); // Tìm đơn hàng của customer, sắp xếp theo thời gian tạo giảm dần
    Order findByReservation_Id(Integer reservationId); // Tìm đơn hàng theo reservation ID
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus); // Tìm đơn hàng theo trạng thái thanh toán
}