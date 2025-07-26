package com.coffeeshop.dto.admin.request;

public class AdminSettingRequestDTO {
    private String settingKey;
    private String settingValue;
    private Boolean isActive;
    
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
} 