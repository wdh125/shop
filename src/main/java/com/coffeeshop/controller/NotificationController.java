package com.coffeeshop.controller;

import com.coffeeshop.dto.common.NotificationRequestDTO;
import com.coffeeshop.dto.common.NotificationResponseDTO;
import com.coffeeshop.entity.Notification;
import com.coffeeshop.entity.User;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Reservation;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.ReservationRepository;
import com.coffeeshop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;

    // Lấy tất cả thông báo của user hiện tại
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationService.getAllByUser(user);
        return ResponseEntity.ok(notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    // Lấy thông báo chưa đọc
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationService.getUnreadByUser(user);
        return ResponseEntity.ok(notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    // Đếm thông báo chưa đọc
    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        long count = notificationService.countUnreadByUser(user);
        return ResponseEntity.ok(count);
    }

    // Lấy thông báo theo type
    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByType(
            @PathVariable NotificationType type,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Notification> notifications = notificationService.getByUserAndType(user, type);
        return ResponseEntity.ok(notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    // Đánh dấu đã đọc 1 thông báo
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    // Đánh dấu đã đọc tất cả thông báo
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    // Xóa thông báo
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer id) {
        notificationService.delete(id);
        return ResponseEntity.ok().build();
    }

    // Tạo thông báo mới (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO request) {
        User user = userRepository.findByUsername(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setContent(request.getContent());
        
        // Handle Order entity if provided
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId()).orElse(null);
            notification.setOrder(order);
        }
        
        // Handle Reservation entity if provided  
        if (request.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(request.getReservationId()).orElse(null);
            notification.setReservation(reservation);
        }
        
        Notification savedNotification = notificationService.createNotification(notification);
        return ResponseEntity.ok(convertToDTO(savedNotification));
    }

    // Converter method
    private NotificationResponseDTO convertToDTO(Notification notification) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getUsername());
        dto.setUserFullName(notification.getUser().getFullName());
        dto.setType(notification.getType());
        dto.setContent(notification.getContent());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        
        if (notification.getOrder() != null) {
            dto.setOrderId(notification.getOrder().getId());
            dto.setOrderNumber(notification.getOrder().getOrderNumber());
        }
        
        if (notification.getReservation() != null) {
            dto.setReservationId(notification.getReservation().getId());
        }
        
        return dto;
    }
}