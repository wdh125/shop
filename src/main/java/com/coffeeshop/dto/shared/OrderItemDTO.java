package com.coffeeshop.dto.shared;

public class OrderItemDTO {
    private Integer productId;
    private Integer quantity;

    public OrderItemDTO() {}
    
    public OrderItemDTO(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
} 