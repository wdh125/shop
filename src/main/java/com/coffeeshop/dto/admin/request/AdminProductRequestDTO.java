package com.coffeeshop.dto.admin.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO dùng cho admin tạo/sửa sản phẩm.
 */
public class AdminProductRequestDTO {
    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Integer categoryId;      // ID danh mục sản phẩm
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;             // Tên sản phẩm
    
    private String description;      // Mô tả sản phẩm
    
    @NotNull(message = "Giá sản phẩm không được để trống")
    @Positive(message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;        // Giá sản phẩm (BigDecimal cho chính xác tiền tệ)
    
    private String imageUrl;         // URL hình ảnh sản phẩm
    private Boolean isAvailable;     // Trạng thái có sẵn để bán
    @Positive(message = "Thời gian chế biến phải lớn hơn 0")
    private Integer preparationTime; // Thời gian chuẩn bị (phút)
    private Integer displayOrder;    // Thứ tự hiển thị
    
    // Constructor mặc định
    public AdminProductRequestDTO() {}
    
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
} 