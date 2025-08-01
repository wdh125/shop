package com.coffeeshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coffeeshop.dto.common.MessageResponseDTO;
import com.coffeeshop.dto.notification.request.NotificationCreateRequestDTO;
import com.coffeeshop.dto.notification.response.NotificationListResponseDTO;
import com.coffeeshop.dto.notification.response.NotificationResponseDTO;
import com.coffeeshop.security.JwtUtils;
import com.coffeeshop.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationCreateRequestDTO requestDTO) {
        
        NotificationResponseDTO response = notificationService.createNotification(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or (#userId == authentication.principal.id)")
    public ResponseEntity<NotificationListResponseDTO> getUserNotifications(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean onlyUnread) {
        
        NotificationListResponseDTO response = notificationService.getUserNotifications(userId, page, size, onlyUnread);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<NotificationListResponseDTO> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean onlyUnread) {
        
        Integer userId = jwtUtils.getUserIdFromAuthentication(authentication);
        NotificationListResponseDTO response = notificationService.getUserNotifications(userId, page, size, onlyUnread);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<MessageResponseDTO> markAsRead(
            @PathVariable Integer notificationId,
            Authentication authentication) {
        
        Integer userId = jwtUtils.getUserIdFromAuthentication(authentication);
        notificationService.markAsRead(notificationId, userId);
        
        MessageResponseDTO response = new MessageResponseDTO("Đã đánh dấu thông báo là đã đọc");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<MessageResponseDTO> markAllAsRead(Authentication authentication) {
        
        Integer userId = jwtUtils.getUserIdFromAuthentication(authentication);
        notificationService.markAllAsRead(userId);
        
        MessageResponseDTO response = new MessageResponseDTO("Đã đánh dấu tất cả thông báo là đã đọc");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        
        Integer userId = jwtUtils.getUserIdFromAuthentication(authentication);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(unreadCount);
    }
}