package com.coffeeshop.dto.admin.response;

import com.coffeeshop.entity.Category;
import java.util.List;

public class CategoryDTO {
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private Integer displayOrder;
    private List<ProductInfo> products;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public static CategoryDTO fromEntity(Category c, List<ProductInfo> products) {
        CategoryDTO dto = new CategoryDTO();
        dto.id = c.getId();
        dto.name = c.getName();
        dto.description = c.getDescription();
        dto.imageUrl = c.getImageUrl();
        dto.isActive = c.getIsActive();
        dto.displayOrder = c.getDisplayOrder();
        dto.createdAt = c.getCreatedAt();
        dto.updatedAt = c.getUpdatedAt();
        dto.products = products;
        return dto;
    }
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getIsActive() { return isActive; }
    public Integer getDisplayOrder() { return displayOrder; }
    public List<ProductInfo> getProducts() { return products; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class ProductInfo {
        private Integer id;
        private String name;
        private java.math.BigDecimal price;
        private Boolean isAvailable;
        public ProductInfo(Integer id, String name, java.math.BigDecimal price, Boolean isAvailable) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.isAvailable = isAvailable;
        }
        public Integer getId() { return id; }
        public String getName() { return name; }
        public java.math.BigDecimal getPrice() { return price; }
        public Boolean getIsAvailable() { return isAvailable; }
    }
} 