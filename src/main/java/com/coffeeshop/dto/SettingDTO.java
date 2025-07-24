package com.coffeeshop.dto;

import com.coffeeshop.entity.Setting;

public class SettingDTO {
    private String settingKey;
    private String settingValue;
    private java.time.LocalDateTime updatedAt;

    public static SettingDTO fromEntity(Setting s) {
        SettingDTO dto = new SettingDTO();
        dto.settingKey = s.getSettingKey();
        dto.settingValue = s.getSettingValue();
        dto.updatedAt = s.getUpdatedAt();
        return dto;
    }
    public String getSettingKey() { return settingKey; }
    public String getSettingValue() { return settingValue; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
} 