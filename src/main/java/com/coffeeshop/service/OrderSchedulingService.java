package com.coffeeshop.service;

import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.OrderItem;
import com.coffeeshop.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.coffeeshop.enums.OrderItemStatus;
import com.coffeeshop.repository.OrderItemRepository;

@Service
public class OrderSchedulingService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ScheduledExecutorService taskScheduler;

    @Autowired
    private OrderItemRepository orderItemRepository;

    /**
     * Lên lịch để tự động cập nhật trạng thái đơn hàng.
     * Hỗ trợ cả đơn hàng đặt tại chỗ và đơn hàng đặt trước qua reservation.
     *
     * @param orderId ID của đơn hàng cần theo dõi.
     */
    @Async
    public void scheduleOrderStatusUpdate(Integer orderId) {
        Order order = orderService.getOrderById(orderId);
        List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);

        if (orderItems.isEmpty()) return;

        int maxPreparationTimeMinutes = orderItems.stream()
                .mapToInt(item -> item.getProduct().getPreparationTime())
                .max()
                .orElse(0);

        if (order.getReservation() != null) {
            // Trường hợp có đặt bàn trước (pre-order)
            handlePreOrderScheduling(order, maxPreparationTimeMinutes, orderItems);
        } else {
            // Trường hợp đặt tại chỗ
            handleImmediateOrderScheduling(order, maxPreparationTimeMinutes, orderItems);
        }
    }

    private void handlePreOrderScheduling(Order order, int prepTimeMinutes, List<OrderItem> orderItems) {
        LocalDateTime reservationTime = order.getReservation().getReservationDatetime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = reservationTime.minusMinutes(prepTimeMinutes);

        // Lên lịch để chuyển sang "preparing"
        long delayToStartCooking = Duration.between(now, startTime).toMillis();
        if (delayToStartCooking < 0) delayToStartCooking = 0; // Nếu sát giờ quá, nấu ngay

        taskScheduler.schedule(() -> {
            Order currentOrder = orderService.getOrderById(order.getId());
            if (currentOrder.getStatus() == OrderStatus.PENDING) { // Chỉ chuyển nếu vẫn là pending
                orderService.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
                System.out.println("Order " + order.getId() + " (Pre-order) is now preparing.");
            }
        }, delayToStartCooking, TimeUnit.MILLISECONDS);

        // Lên lịch để chuyển sang "ready"
        long delayToReady = Duration.between(now, reservationTime).toMillis();
        if (delayToReady < 0) delayToReady = prepTimeMinutes * 60 * 1000L; // Nếu đã quá giờ hẹn, tính từ bây giờ

        taskScheduler.schedule(() -> {
            Order currentOrder = orderService.getOrderById(order.getId());
            if (currentOrder.getStatus() == OrderStatus.PREPARING) {
                // Nếu logic là chuyển trạng thái của từng OrderItem sang READY:
                for (OrderItem item : orderItems) {
                    item.setStatus(OrderItemStatus.READY);
                }
                orderItemRepository.saveAll(orderItems);
                System.out.println("Order " + order.getId() + " (Pre-order) items are now ready for pickup.");
            }
        }, delayToReady, TimeUnit.MILLISECONDS);

        System.out.println("Scheduled pre-order " + order.getId() + ". Preparing starts in " + delayToStartCooking/1000/60 + " mins. Ready in " + delayToReady/1000/60 + " mins.");
    }

    private void handleImmediateOrderScheduling(Order order, int prepTimeMinutes, List<OrderItem> orderItems) {
        // Chuyển sang "preparing" ngay lập tức
        orderService.updateOrderStatus(order.getId(), OrderStatus.PREPARING);
        System.out.println("Order " + order.getId() + " (Immediate) is now preparing.");

        // Lên lịch để chuyển trạng thái từng OrderItem sang READY
        taskScheduler.schedule(() -> {
            Order currentOrder = orderService.getOrderById(order.getId());
            if (currentOrder.getStatus() == OrderStatus.PREPARING) {
                for (OrderItem item : orderItems) {
                    item.setStatus(OrderItemStatus.READY);
                }
                orderItemRepository.saveAll(orderItems);
                System.out.println("Order " + order.getId() + " (Immediate) items are now ready.");
            }
        }, prepTimeMinutes, TimeUnit.MINUTES);

        System.out.println("Scheduled immediate order " + order.getId() + ". Will be ready in " + prepTimeMinutes + " minutes.");
    }

    public void saveOrderItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            // Giả sử có repository cho OrderItem
            orderItemRepository.save(item);
        }
    }
} 