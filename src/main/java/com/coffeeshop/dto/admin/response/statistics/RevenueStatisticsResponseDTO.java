package com.coffeeshop.dto.response.statistics;

import java.time.LocalDate;

public class RevenueStatisticsResponseDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private double totalRevenue;

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
} 