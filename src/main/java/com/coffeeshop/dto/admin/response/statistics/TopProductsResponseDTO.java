package com.coffeeshop.dto.response.statistics;

import java.util.List;

public class TopProductsResponseDTO {
    public static class TopProductInfo {
        private Integer id;
        private String name;
        private int totalSold;
        private double totalRevenue;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getTotalSold() { return totalSold; }
        public void setTotalSold(int totalSold) { this.totalSold = totalSold; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    }
    private List<TopProductInfo> topProducts;
    public List<TopProductInfo> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProductInfo> topProducts) { this.topProducts = topProducts; }
} 