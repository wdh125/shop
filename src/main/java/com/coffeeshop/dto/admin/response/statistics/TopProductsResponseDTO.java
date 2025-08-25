package com.coffeeshop.dto.admin.response.statistics;

import java.util.List;

public class TopProductsResponseDTO {
    public static class TopProductInfo {
        private Integer id;
        private String name;
        private int totalSold;
        private double totalRevenue;
        private int salesCount; // Alias for totalSold to match frontend

        // Constructor mặc định
        public TopProductInfo() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getTotalSold() { return totalSold; }
        public void setTotalSold(int totalSold) { 
            this.totalSold = totalSold; 
            this.salesCount = totalSold; // Sync with salesCount
        }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        public int getSalesCount() { return salesCount; }
        public void setSalesCount(int salesCount) { 
            this.salesCount = salesCount; 
            this.totalSold = salesCount; // Sync with totalSold
        }
        
        // Alias for totalSold to maintain frontend compatibility
        public int getOrderCount() { return totalSold; }
        public void setOrderCount(int orderCount) { 
            this.totalSold = orderCount; 
            this.salesCount = orderCount; // Sync with salesCount
        }
    }
    private List<TopProductInfo> topProducts;

    // Constructor mặc định
    public TopProductsResponseDTO() {}

    public List<TopProductInfo> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProductInfo> topProducts) { this.topProducts = topProducts; }
} 