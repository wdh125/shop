package com.coffeeshop.scheduler;

import com.coffeeshop.entity.*;
import com.coffeeshop.enums.*;
import com.coffeeshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
public class AutoScheduler {
    @Autowired private TableRepository tableRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private SchedulerConfig schedulerConfig;

    private boolean isWithinOpeningHours() {
        LocalTime now = LocalTime.now();
        LocalTime opening = LocalTime.parse(schedulerConfig.openingTime);
        LocalTime closing = LocalTime.parse(schedulerConfig.closingTime);
        return !now.isBefore(opening) && !now.isAfter(closing);
    }

    // 1. PENDING -> CANCELLED nếu quá 15 phút
    @Scheduled(fixedDelay = 60000)
    public void autoCancelPendingOrders() {
        if (!isWithinOpeningHours()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            if (order.getCreatedAt().plusMinutes(schedulerConfig.orderPendingToCancelledMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setUpdatedAt(now);
                orderRepository.save(order);
                setTableAvailableIfNoActiveOrder(order.getTable());
            }
        }
    }

    // 2. PAID -> PREPARING sau Y phút
    @Scheduled(fixedDelay = 60000)
    public void autoPaidToPreparing() {
        if (!isWithinOpeningHours()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Order> paidOrders = orderRepository.findByStatus(OrderStatus.PAID);
        for (Order order : paidOrders) {
            if (order.getUpdatedAt().plusMinutes(schedulerConfig.orderPreparingToReadyMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.PREPARING);
                order.setUpdatedAt(now);
                orderRepository.save(order);
            }
        }
    }

    // 3. PREPARING -> SERVED sau 10 phút
    @Scheduled(fixedDelay = 60000)
    public void autoPreparingToServed() {
        if (!isWithinOpeningHours()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Order> preparingOrders = orderRepository.findByStatus(OrderStatus.PREPARING);
        for (Order order : preparingOrders) {
            if (order.getUpdatedAt().plusMinutes(schedulerConfig.orderPreparingToReadyMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.SERVED);
                order.setUpdatedAt(now);
                orderRepository.save(order);
            }
        }
    }

    // 4. SERVED -> COMPLETED sau 45 phút
    @Scheduled(fixedDelay = 60000)
    public void autoServedToCompleted() {
        if (!isWithinOpeningHours()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Order> servedOrders = orderRepository.findByStatus(OrderStatus.SERVED);
        for (Order order : servedOrders) {
            if (order.getUpdatedAt().plusMinutes(schedulerConfig.orderServedToCompletedMinutes).isBefore(now)) {
                order.setStatus(OrderStatus.COMPLETED);
                order.setUpdatedAt(now);
                orderRepository.save(order);
                // Nếu có reservation liên quan, chuyển reservation sang COMPLETED
                if (order.getReservation() != null) {
                    Reservation reservation = order.getReservation();
                    if (reservation.getStatus() != ReservationStatus.COMPLETED) {
                        reservation.setStatus(ReservationStatus.COMPLETED);
                        reservation.setUpdatedAt(now);
                        reservationRepository.save(reservation);
                    }
                }
                setTableAvailableIfNoActiveOrder(order.getTable());
            }
        }
    }

    // 5. COMPLETED/CANCELLED -> bàn AVAILABLE
    private void setTableAvailableIfNoActiveOrder(TableEntity table) {
        List<Order> activeOrders = orderRepository.findByTable(table);
        boolean hasActive = activeOrders.stream().anyMatch(o ->
                o.getStatus() != OrderStatus.COMPLETED && o.getStatus() != OrderStatus.CANCELLED);
        if (!hasActive && table.getStatus() != TableStatus.AVAILABLE) {
            table.setStatus(TableStatus.AVAILABLE);
            table.setUpdatedAt(LocalDateTime.now());
            tableRepository.save(table);
        }
    }

    // 6. Giờ đặt bàn - 30 phút -> bàn reserved
    @Scheduled(fixedDelay = 60000)
    public void autoReserveTableForUpcomingReservation() {
        if (!isWithinOpeningHours()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        for (Reservation reservation : reservations) {
            TableEntity table = reservation.getTable();
            LocalDateTime blockTime = reservation.getReservationDatetime().minusMinutes(schedulerConfig.reservationAutoReserveBeforeMinutes);
            if (now.isAfter(blockTime) && now.isBefore(reservation.getReservationDatetime())
                    && table.getStatus() == TableStatus.AVAILABLE) {
                table.setStatus(TableStatus.RESERVED);
                table.setUpdatedAt(now);
                tableRepository.save(table);
            }
        }
    }

    // 7. Sau giờ đặt + 15 phút chưa đến -> hủy reservation, bàn available
    @Scheduled(fixedDelay = 60000)
    public void autoCancelNoShowReservation() {
        if (!isWithinOpeningHours()) return;
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        for (Reservation reservation : reservations) {
            LocalDateTime graceEnd = reservation.getReservationDatetime().plusMinutes(schedulerConfig.reservationGracePeriodMinutes);
            TableEntity table = reservation.getTable();
            if (now.isAfter(graceEnd)) {
                boolean hasActiveOrder = orderRepository.findByTable(table).stream()
                        .anyMatch(o -> o.getReservation() != null
                                && o.getReservation().getId().equals(reservation.getId())
                                && o.getStatus() != OrderStatus.CANCELLED);
                if (!hasActiveOrder) {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                    reservation.setUpdatedAt(now);
                    reservationRepository.save(reservation);
                    if (table.getStatus() == TableStatus.RESERVED) {
                        table.setStatus(TableStatus.AVAILABLE);
                        table.setUpdatedAt(now);
                        tableRepository.save(table);
                    }
                }
            }
        }
    }
} 