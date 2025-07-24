package com.coffeeshop.scheduler;

import com.coffeeshop.entity.Order;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutoOrderStatusScheduler {
    @Autowired private OrderRepository orderRepository;
    @Autowired private SchedulerConfig schedulerConfig;

    // 1. PENDING -> CANCELLED nếu quá X phút
    @Scheduled(fixedDelay = 60000)
    public void autoCancelPendingOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            if (order.getCreatedAt().plusMinutes(schedulerConfig.orderPendingToCancelledMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(now);
                orderRepository.save(order);
            }
        }
    }

    // 2. PENDING + paymentStatus = paid -> PREPARING nếu updatedAt quá Y phút
    @Scheduled(fixedDelay = 60000)
    public void autoPendingPaidToPreparing() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            if (order.getPaymentStatus() == PaymentStatus.PAID &&
                order.getUpdatedAt().plusMinutes(schedulerConfig.orderPreparingToReadyMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.PREPARING);
                order.setUpdatedAt(now);
                orderRepository.save(order);
            }
        }
    }

    // 3. SERVED -> COMPLETED nếu updatedAt quá Z phút
    @Scheduled(fixedDelay = 60000)
    public void autoServedToCompleted() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> servedOrders = orderRepository.findByStatus(OrderStatus.SERVED);
        for (Order order : servedOrders) {
            if (order.getUpdatedAt().plusMinutes(schedulerConfig.orderServedToCompletedMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.COMPLETED);
                order.setUpdatedAt(now);
                orderRepository.save(order);
            }
        }
    }
} 