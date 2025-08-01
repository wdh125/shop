package com.coffeeshop.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coffeeshop.dto.notification.request.NotificationCreateRequestDTO;
import com.coffeeshop.dto.notification.response.NotificationListResponseDTO;
import com.coffeeshop.dto.notification.response.NotificationResponseDTO;
import com.coffeeshop.entity.Notification;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Payment;
import com.coffeeshop.entity.Reservation;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.repository.NotificationRepository;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.PaymentRepository;
import com.coffeeshop.repository.ReservationRepository;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.service.NotificationService;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public NotificationResponseDTO createNotification(NotificationCreateRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(requestDTO.getType());
        notification.setTitle(requestDTO.getTitle());
        notification.setMessage(requestDTO.getMessage());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        // Set related entities using proper entity relationships
        if (requestDTO.getRelatedOrderId() != null) {
            Order order = orderRepository.findById(requestDTO.getRelatedOrderId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            notification.setRelatedOrder(order);
        }

        if (requestDTO.getRelatedPaymentId() != null) {
            Payment payment = paymentRepository.findById(requestDTO.getRelatedPaymentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
            notification.setRelatedPayment(payment);
        }

        if (requestDTO.getRelatedReservationId() != null) {
            Reservation reservation = reservationRepository.findById(requestDTO.getRelatedReservationId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đặt bàn"));
            notification.setRelatedReservation(reservation);
        }

        notification = notificationRepository.save(notification);
        return convertToResponseDTO(notification);
    }

    @Override
    public NotificationListResponseDTO getUserNotifications(Integer userId, int page, int size, Boolean onlyUnread) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage;

        if (onlyUnread != null && onlyUnread) {
            notificationPage = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false, pageable);
        } else {
            notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        List<NotificationResponseDTO> notifications = notificationPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);

        NotificationListResponseDTO response = new NotificationListResponseDTO();
        response.setNotifications(notifications);
        response.setUnreadCount(unreadCount);
        response.setTotalPages(notificationPage.getTotalPages());
        response.setTotalElements(notificationPage.getTotalElements());
        response.setCurrentPage(page);
        response.setPageSize(size);

        return response;
    }

    @Override
    public void markAsRead(Integer notificationId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        int updated = notificationRepository.markAsReadById(notificationId, user);
        if (updated == 0) {
            throw new RuntimeException("Không tìm thấy thông báo hoặc không có quyền truy cập");
        }
    }

    @Override
    public void markAllAsRead(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        notificationRepository.markAllAsReadForUser(user);
    }

    @Override
    public long getUnreadCount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public void createOrderNotification(User user, Order order, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedOrder(order);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void createPaymentNotification(User user, Payment payment, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedPayment(payment);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void createReservationNotification(User user, Reservation reservation, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedReservation(reservation);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private NotificationResponseDTO convertToResponseDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());

        // Set related entity information
        if (notification.getRelatedOrder() != null) {
            dto.setRelatedOrderId(notification.getRelatedOrder().getId());
            dto.setRelatedOrderNumber(notification.getRelatedOrder().getOrderNumber());
        }

        if (notification.getRelatedPayment() != null) {
            dto.setRelatedPaymentId(notification.getRelatedPayment().getId());
        }

        if (notification.getRelatedReservation() != null) {
            dto.setRelatedReservationId(notification.getRelatedReservation().getId());
        }

        return dto;
    }
}