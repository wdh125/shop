package com.coffeeshop.dto.admin.response;

public class AdminCategoryStatisticsDTO {
    private int totalCategories;
    private int totalProducts;
    private int avgProductsPerCategory;

    public AdminCategoryStatisticsDTO() {}
    public AdminCategoryStatisticsDTO(int totalCategories, int totalProducts, int avgProductsPerCategory) {
        this.totalCategories = totalCategories;
        this.totalProducts = totalProducts;
        this.avgProductsPerCategory = avgProductsPerCategory;
    }
    public int getTotalCategories() { return totalCategories; }
    public void setTotalCategories(int totalCategories) { this.totalCategories = totalCategories; }
    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public int getAvgProductsPerCategory() { return avgProductsPerCategory; }
    public void setAvgProductsPerCategory(int avgProductsPerCategory) { this.avgProductsPerCategory = avgProductsPerCategory; }
} 