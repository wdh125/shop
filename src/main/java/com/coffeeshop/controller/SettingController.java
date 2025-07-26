package com.coffeeshop.controller;

import com.coffeeshop.service.SettingService;
import com.coffeeshop.dto.admin.response.AdminSettingResponseDTO;
import com.coffeeshop.dto.admin.request.AdminSettingRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerSettingResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SettingController {
    @Autowired
    private SettingService settingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminSettingResponseDTO> getAllSettings() {
        return settingService.getAllAdminSettings();
    }

    @GetMapping("/public-list")
    public List<CustomerSettingResponseDTO> getPublicSettings() {
        return settingService.getPublicSettings();
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminSettingResponseDTO getSettingByKey(@PathVariable String key) {
        return settingService.getAdminSettingByKey(key);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminSettingResponseDTO createSetting(@Valid @RequestBody AdminSettingRequestDTO request) {
        return settingService.createSetting(request);
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminSettingResponseDTO updateSetting(@PathVariable String key, @Valid @RequestBody AdminSettingRequestDTO request) {
        return settingService.updateSetting(key, request);
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSetting(@PathVariable String key) {
        settingService.deleteSetting(key);
    }
}