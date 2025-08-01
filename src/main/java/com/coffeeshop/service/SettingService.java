package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import com.coffeeshop.entity.Setting;
import com.coffeeshop.dto.admin.response.AdminSettingResponseDTO;
import com.coffeeshop.dto.admin.request.AdminSettingRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerSettingResponseDTO;

/**
 * Setting service interface for managing application settings
 */
public interface SettingService {

    /**
     * Get all settings
     */
    List<Setting> getAllSettings();

    /**
     * Get setting by key
     */
    Optional<Setting> getSettingByKey(String key);

    /**
     * Save or update setting
     */
    Setting saveSetting(Setting setting);

    /**
     * Delete setting by key
     */
    void deleteSetting(String key);

    /**
     * Get tax rate from settings
     */
    BigDecimal getTaxRate();

    /**
     * Get all settings for admin view
     */
    List<AdminSettingResponseDTO> getAllAdminSettings();

    /**
     * Get public settings for customer view
     */
    List<CustomerSettingResponseDTO> getPublicSettings();

    /**
     * Get admin setting by key
     */
    AdminSettingResponseDTO getAdminSettingByKey(String key);

    /**
     * Create new setting
     */
    AdminSettingResponseDTO createSetting(AdminSettingRequestDTO request);

    /**
     * Update existing setting
     */
    AdminSettingResponseDTO updateSetting(String key, AdminSettingRequestDTO request);
}