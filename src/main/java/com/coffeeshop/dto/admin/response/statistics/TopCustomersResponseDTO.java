package com.coffeeshop.dto.admin.response.statistics;

import java.util.List;

public class TopCustomersResponseDTO {
    public static class TopCustomerInfo {
        private Integer id;
        private String name;
        private String email;
        private double totalSpent;
        private int totalOrders;
        private String description; // Mô tả khách hàng
        private String profileImage; // Ảnh đại diện khách hàng

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
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        
        // Alias for totalOrders to maintain frontend compatibility
        public int getOrderCount() { return totalOrders; }
        public void setOrderCount(int orderCount) { this.totalOrders = orderCount; }
    }
    private List<TopCustomerInfo> topCustomers;

    // Constructor mặc định
    public TopCustomersResponseDTO() {}

    public List<TopCustomerInfo> getTopCustomers() { return topCustomers; }
    public void setTopCustomers(List<TopCustomerInfo> topCustomers) { this.topCustomers = topCustomers; }
} 