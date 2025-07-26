package com.coffeeshop.dto.customer.response;

public class CustomerProductResponseDTO {
    private Integer id;
    private String name;
    private Double price;
    private String imageUrl;
    private Boolean isAvailable;

    public CustomerProductResponseDTO() {}
    public CustomerProductResponseDTO(Integer id, String name, Double price, String imageUrl, Boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public static CustomerProductResponseDTO fromEntity(com.coffeeshop.entity.Product p) {
        return new CustomerProductResponseDTO(
            p.getId(),
            p.getName(),
            p.getPrice() != null ? p.getPrice().doubleValue() : null,
            p.getImageUrl(),
            p.getIsAvailable()
        );
    }
} 