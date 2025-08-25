package com.coffeeshop.dto.admin.response;

import java.time.LocalDateTime;
import java.util.List;

public class AdminPaymentResponseDTO {
    private Integer id;
    private Integer orderId;
    private CustomerInfo customer;
    private OrderInfo order; // Thêm thông tin order
    private Double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor mặc định
    public AdminPaymentResponseDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public CustomerInfo getCustomer() { return customer; }
    public void setCustomer(CustomerInfo customer) { this.customer = customer; }
    public OrderInfo getOrder() { return order; }
    public void setOrder(OrderInfo order) { this.order = order; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Inner class CustomerInfo
    public static class CustomerInfo {
        private Integer id;
        private String username;
        private String fullName;
        private String phone;

        // Constructor mặc định
        public CustomerInfo() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    // Inner class OrderInfo
    public static class OrderInfo {
        private Integer id;
        private String orderNumber;
        private String status;
        private String paymentStatus;
        private Double totalAmount;
        private Double subtotal;
        private Double taxAmount;
        private List<OrderItemInfo> orderItems;

        // Constructor mặc định
        public OrderInfo() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public Double getSubtotal() { return subtotal; }
        public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
        public Double getTaxAmount() { return taxAmount; }
        public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }
        public List<OrderItemInfo> getOrderItems() { return orderItems; }
        public void setOrderItems(List<OrderItemInfo> orderItems) { this.orderItems = orderItems; }
    }

    // Inner class OrderItemInfo
    public static class OrderItemInfo {
        private Integer id;
        private String productName;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;

        // Constructor mặc định
        public OrderItemInfo() {}

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