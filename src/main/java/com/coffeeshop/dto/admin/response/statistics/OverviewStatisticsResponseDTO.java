package com.coffeeshop.dto.admin.response.statistics;

public class OverviewStatisticsResponseDTO {
    private double totalRevenue;
    private int totalOrders;
    private int totalCustomers;
    private int totalProducts;
    private int totalCategories;
    private int newCustomers; // Khách hàng mới trong tháng hiện tại

    // Constructor mặc định
    public OverviewStatisticsResponseDTO() {}

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }
    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public int getTotalCategories() { return totalCategories; }
    public void setTotalCategories(int totalCategories) { this.totalCategories = totalCategories; }
    public int getNewCustomers() { return newCustomers; }
    public void setNewCustomers(int newCustomers) { this.newCustomers = newCustomers; }
    
    // Alias for totalOrders to maintain frontend compatibility
    public int getOrderCount() { return totalOrders; }
    public void setOrderCount(int orderCount) { this.totalOrders = orderCount; }
} 