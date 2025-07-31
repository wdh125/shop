package com.coffeeshop.dto.common;

import com.coffeeshop.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request tạo thông báo mới
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationRequestDTO {
    @NotBlank(message = "User ID không được để trống")
    private String userId;

    @NotNull(message = "Loại thông báo không được để trống")
    private NotificationType type;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String content;

    private Integer orderId;
    private Integer reservationId;

    // Constructors
    public NotificationRequestDTO() {}

    public NotificationRequestDTO(String userId, NotificationType type, String content) {
        this.userId = userId;
        this.type = type;
        this.content = content;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }
}