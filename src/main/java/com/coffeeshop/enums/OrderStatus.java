package com.coffeeshop.enums;

public enum OrderStatus {
    PENDING,      // Mới tạo, chưa thanh toán
    CANCELLED,    // Bị hủy
    PAID,         // Đã thanh toán, chưa làm
    PREPARING,    // Đang chế biến món
    SERVED,       // Món đã hoàn thành và đã tới khách
    COMPLETED     // Kết thúc sau một khoảng thời gian
}