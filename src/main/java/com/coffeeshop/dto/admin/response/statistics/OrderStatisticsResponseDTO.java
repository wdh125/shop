package com.coffeeshop.dto.response.statistics;

import java.time.LocalDate;

public class OrderStatisticsResponseDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalOrders;

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
} 