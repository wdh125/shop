package com.coffeeshop.dto.admin.request;

import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.enums.PaymentProcessStatus;

public class AdminPaymentRequestDTO {
    private Integer orderId;
    private PaymentMethod paymentMethod;
    private Integer paidByUserId;
    private PaymentProcessStatus status = PaymentProcessStatus.COMPLETED; // Mặc định là completed

    // Getters
    public Integer getOrderId() { return orderId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public Integer getPaidByUserId() { return paidByUserId; }
    public PaymentProcessStatus getStatus() { return status; }

    // Setters
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaidByUserId(Integer paidByUserId) { this.paidByUserId = paidByUserId; }
    public void setStatus(PaymentProcessStatus status) { this.status = status; }
} 