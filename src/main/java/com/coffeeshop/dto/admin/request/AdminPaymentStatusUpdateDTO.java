package com.coffeeshop.dto.admin.request;

public class AdminPaymentStatusUpdateDTO {
    private String status;
    private String note;
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
} 