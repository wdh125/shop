package com.coffeeshop.dto.notification.request;

import com.coffeeshop.enums.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationCreateRequestDTO {
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotNull(message = "Loại thông báo không được để trống")
    private NotificationType type;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String message;
    
    private Integer relatedOrderId;
    private Integer relatedPaymentId;
    private Integer relatedReservationId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public Integer getRelatedOrderId() {
        return relatedOrderId;
    }

    public void setRelatedOrderId(Integer relatedOrderId) {
        this.relatedOrderId = relatedOrderId;
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