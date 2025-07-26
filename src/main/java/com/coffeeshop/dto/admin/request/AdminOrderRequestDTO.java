package com.coffeeshop.dto.admin.request;

import com.coffeeshop.dto.shared.OrderItemDTO;
import java.util.List;

public class AdminOrderRequestDTO {
    private Integer tableId; // Tùy chọn nếu có reservationId
    private Integer userId; // Tùy chọn nếu có reservationId
    private Integer reservationId; // Tùy chọn, dùng cho việc đặt món trước
    private String note;
    private List<OrderItemDTO> items;

    // Constructor mặc định
    public AdminOrderRequestDTO() {}

    // Getters and Setters
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
} 