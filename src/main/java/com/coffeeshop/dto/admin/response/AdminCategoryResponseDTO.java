package com.coffeeshop.dto.admin.response;

import java.util.List;

public class AdminCategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer displayOrder;
    private Integer productCount;
    private List<AdminProductResponseDTO> products;
    private String imageUrl; // THÊM FIELD NÀY!
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public AdminCategoryResponseDTO() {}
    public AdminCategoryResponseDTO(
        Integer id, String name, String description, Boolean isActive,
        Integer displayOrder, Integer productCount, List<AdminProductResponseDTO> products,
        String imageUrl, // THÊM FIELD NÀY!
        java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
        this.productCount = productCount;
        this.products = products;
        this.imageUrl = imageUrl; // THÊM FIELD NÀY!
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }
    public List<AdminProductResponseDTO> getProducts() { return products; }
    public void setProducts(List<AdminProductResponseDTO> products) { this.products = products; }
    public String getImageUrl() { return imageUrl; } // THÊM GETTER
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; } // THÊM SETTER
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static AdminCategoryResponseDTO fromEntity(com.coffeeshop.entity.Category c, List<AdminProductResponseDTO> products) {
        return new AdminCategoryResponseDTO(
            c.getId(),
            c.getName(),
            c.getDescription(),
            c.getIsActive(),
            c.getDisplayOrder(),
            products != null ? products.size() : 0,
            products,
            c.getImageUrl(), // THÊM TRUYỀN IMAGE!
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}