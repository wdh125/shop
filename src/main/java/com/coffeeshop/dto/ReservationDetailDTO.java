package com.coffeeshop.dto;

import java.time.LocalDateTime;

public class ReservationDetailDTO {
    private Integer id;
    private CustomerInfo customer;
    private TableInfo table;
    private LocalDateTime reservationDatetime;
    private Integer partySize;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderSummaryDTO order; // Có thể null nếu chưa có đơn

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public CustomerInfo getCustomer() { return customer; }
    public void setCustomer(CustomerInfo customer) { this.customer = customer; }
    public TableInfo getTable() { return table; }
    public void setTable(TableInfo table) { this.table = table; }
    public LocalDateTime getReservationDatetime() { return reservationDatetime; }
    public void setReservationDatetime(LocalDateTime reservationDatetime) { this.reservationDatetime = reservationDatetime; }
    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public OrderSummaryDTO getOrder() { return order; }
    public void setOrder(OrderSummaryDTO order) { this.order = order; }

    public static class CustomerInfo {
        private Integer id;
        private String username;
        private String fullName;
        private String phone;
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    public static class TableInfo {
        private Integer id;
        private String tableNumber;
        private String location;
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTableNumber() { return tableNumber; }
        public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    public static class OrderSummaryDTO {
        private Integer id;
        private String orderNumber;
        private String status;
        private String paymentStatus;
        private java.math.BigDecimal totalAmount;
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public java.math.BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
} 