package com.coffeeshop.dto.customer.request;

import com.coffeeshop.dto.shared.OrderItemDTO;
import java.util.List;

public class CustomerOrderRequestDTO {
    private Integer tableId;
    private Integer reservationId; // Tùy chọn, dùng cho việc đặt món trước
    private String note;
    private List<OrderItemDTO> items;

    // Getters and Setters
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
} 