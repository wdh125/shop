package com.coffeeshop.dto.admin.response;

import com.coffeeshop.entity.Product;

public class AdminProductResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private java.math.BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer preparationTime;
    private Integer displayOrder;
    private CategoryInfo category;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public static AdminProductResponseDTO fromEntity(Product p) {
        AdminProductResponseDTO dto = new AdminProductResponseDTO();
        dto.id = p.getId();
        dto.name = p.getName();
        dto.description = p.getDescription();
        dto.price = p.getPrice();
        dto.imageUrl = p.getImageUrl();
        dto.isAvailable = p.getIsAvailable();
        dto.preparationTime = p.getPreparationTime();
        dto.displayOrder = p.getDisplayOrder();
        dto.createdAt = p.getCreatedAt();
        dto.updatedAt = p.getUpdatedAt();
        if (p.getCategory() != null) {
            dto.category = new CategoryInfo(p.getCategory().getId(), p.getCategory().getName());
        }
        return dto;
    }
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public java.math.BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getIsAvailable() { return isAvailable; }
    public Integer getPreparationTime() { return preparationTime; }
    public Integer getDisplayOrder() { return displayOrder; }
    public CategoryInfo getCategory() { return category; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class CategoryInfo {
        private Integer id;
        private String name;
        public CategoryInfo(Integer id, String name) { this.id = id; this.name = name; }
        public Integer getId() { return id; }
        public String getName() { return name; }
    }
} 