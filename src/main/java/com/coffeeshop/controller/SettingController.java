package com.coffeeshop.controller;

import com.coffeeshop.entity.Setting;
import com.coffeeshop.service.SettingService;
import com.coffeeshop.dto.admin.response.AdminSettingResponseDTO;
import com.coffeeshop.dto.admin.request.AdminSettingRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerSettingResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SettingController {
    @Autowired
    private SettingService settingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminSettingResponseDTO> getAllSettings() {
        return settingService.getAllSettings().stream().map(this::toAdminSettingResponseDTO).toList();
    }

    @GetMapping("/public")
    public List<CustomerSettingResponseDTO> getPublicSettings() {
        return settingService.getAllSettings().stream()
            .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
            .map(this::toCustomerSettingResponseDTO)
            .toList();
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminSettingResponseDTO getSettingByKey(@PathVariable String key) {
        return settingService.getSettingByKey(key).map(this::toAdminSettingResponseDTO)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy setting!"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminSettingResponseDTO createSetting(@RequestBody AdminSettingRequestDTO request) {
        Setting setting = new Setting();
        setting.setSettingKey(request.getSettingKey());
        setting.setSettingValue(request.getSettingValue());
        setting.setIsActive(request.getIsActive());
        return toAdminSettingResponseDTO(settingService.saveSetting(setting));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminSettingResponseDTO updateSetting(@PathVariable String key, @RequestBody AdminSettingRequestDTO request) {
        Setting setting = new Setting();
        setting.setSettingKey(key);
        setting.setSettingValue(request.getSettingValue());
        setting.setIsActive(request.getIsActive());
        return toAdminSettingResponseDTO(settingService.saveSetting(setting));
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSetting(@PathVariable String key) {
        settingService.deleteSetting(key);
    }

    private AdminSettingResponseDTO toAdminSettingResponseDTO(Setting setting) {
        AdminSettingResponseDTO dto = new AdminSettingResponseDTO();
        dto.setKey(setting.getSettingKey());
        dto.setValue(setting.getSettingValue());
        dto.setDescription(setting.getDescription());
        dto.setIsActive(setting.getIsActive());
        dto.setCreatedAt(setting.getCreatedAt());
        dto.setUpdatedAt(setting.getUpdatedAt());
        return dto;
    }
    
    private CustomerSettingResponseDTO toCustomerSettingResponseDTO(Setting setting) {
        CustomerSettingResponseDTO dto = new CustomerSettingResponseDTO();
        dto.setKey(setting.getSettingKey());
        dto.setValue(setting.getSettingValue());
        dto.setDescription(setting.getDescription());
        dto.setIsActive(setting.getIsActive());
        return dto;
    }
}