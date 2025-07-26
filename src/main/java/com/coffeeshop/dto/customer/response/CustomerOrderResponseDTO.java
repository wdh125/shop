package com.coffeeshop.dto.customer.response;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerOrderResponseDTO {
    private Integer id;
    private String orderNumber;
    private TableInfo table;
    private Integer reservationId;
    private String status;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemInfo> items;
    // ... các trường khác nếu cần
    // getter/setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public TableInfo getTable() { return table; }
    public void setTable(TableInfo table) { this.table = table; }
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItemInfo> getItems() { return items; }
    public void setItems(List<OrderItemInfo> items) { this.items = items; }
    // Inner class getter/setter
    public static class TableInfo {
        private Integer id;
        private String tableNumber;
        private String location;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTableNumber() { return tableNumber; }
        public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    public static class OrderItemInfo {
        private Integer id;
        private String productName;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    }
} 