package com.coffeeshop.dto.notification.response;

import java.time.LocalDateTime;

import com.coffeeshop.enums.NotificationType;

public class NotificationResponseDTO {
    
    private Integer id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Related entity information (only IDs for lightweight response)
    private Integer relatedOrderId;
    private String relatedOrderNumber;
    private Integer relatedPaymentId;
    private Integer relatedReservationId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Integer getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Integer relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
    }

    public String getRelatedOrderNumber() {
        return relatedOrderNumber;
    }

    public void setRelatedOrderNumber(String relatedOrderNumber) {
        this.relatedOrderNumber = relatedOrderNumber;
    }

    public Integer getRelatedPaymentId() {
        return relatedPaymentId;
    }

    public void setRelatedPaymentId(Integer relatedPaymentId) {
        this.relatedPaymentId = relatedPaymentId;
    }

    public Integer getRelatedReservationId() {
        return relatedReservationId;
    }

    public void setRelatedReservationId(Integer relatedReservationId) {
        this.relatedReservationId = relatedReservationId;
    }
}