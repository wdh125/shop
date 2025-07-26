package com.coffeeshop.dto.customer.response;

public class CustomerSettingResponseDTO {
    private String key;
    private String value;
    private String description;
    private Boolean isActive;
    // getter/setter
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
} 