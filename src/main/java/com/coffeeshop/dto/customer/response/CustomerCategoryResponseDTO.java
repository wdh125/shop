package com.coffeeshop.dto.customer.response;

public class CustomerCategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer displayOrder;
    private Integer productCount;

    public CustomerCategoryResponseDTO() {}
    public CustomerCategoryResponseDTO(Integer id, String name, String description, Boolean isActive, Integer displayOrder, Integer productCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.displayOrder = displayOrder;
        this.productCount = productCount;
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
    public static CustomerCategoryResponseDTO fromEntity(com.coffeeshop.entity.Category c, int productCount) {
        return new CustomerCategoryResponseDTO(
            c.getId(),
            c.getName(),
            c.getDescription(),
            c.getIsActive(),
            c.getDisplayOrder(),
            productCount
        );
    }
} 