package com.coffeeshop.dto.customer.response;

public class CustomerProductResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean isAvailable;
    private String categoryName;
    private Integer preparationTime;

    public CustomerProductResponseDTO() {}
    public CustomerProductResponseDTO(Integer id, String name, String description, Double price, String imageUrl, Boolean isAvailable, String categoryName, Integer preparationTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.categoryName = categoryName;
        this.preparationTime = preparationTime;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    
    public static CustomerProductResponseDTO fromEntity(com.coffeeshop.entity.Product p) {
        return new CustomerProductResponseDTO(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getPrice() != null ? p.getPrice().doubleValue() : null,
            p.getImageUrl(),
            p.getIsAvailable(),
            p.getCategory() != null ? p.getCategory().getName() : null,
            p.getPreparationTime()
        );
    }
} 