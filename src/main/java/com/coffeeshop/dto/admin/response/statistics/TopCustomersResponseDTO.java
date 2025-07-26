package com.coffeeshop.dto.admin.response.statistics;

import java.util.List;

public class TopCustomersResponseDTO {
    public static class TopCustomerInfo {
        private Integer id;
        private String name;
        private String email;
        private double totalSpent;
        private int totalOrders;

        // Constructor mặc định
        public TopCustomerInfo() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    }
    private List<TopCustomerInfo> topCustomers;

    // Constructor mặc định
    public TopCustomersResponseDTO() {}

    public List<TopCustomerInfo> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<TopCustomerInfo> topCustomers) { this.topCustomers = topCustomers; }
} 