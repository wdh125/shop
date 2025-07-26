package com.coffeeshop.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO dùng cho request tạo order (customer/admin)
 * Truyền danh sách sản phẩm và số lượng
 * Dùng chung cho cả customer và admin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDTO {
    private Integer productId;   // ID sản phẩm
    private Integer quantity;    // Số lượng sản phẩm

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