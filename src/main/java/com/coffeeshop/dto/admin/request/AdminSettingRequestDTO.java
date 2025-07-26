package com.coffeeshop.dto.admin.request;

public class AdminSettingRequestDTO {
    private String key;          // Tên setting
    private String value;        // Giá trị setting
    private String description;  // Mô tả setting (tùy chọn)
    private Boolean isActive;    // Trạng thái hoạt động
    
    // Constructor mặc định
    public AdminSettingRequestDTO() {}
    
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
} 