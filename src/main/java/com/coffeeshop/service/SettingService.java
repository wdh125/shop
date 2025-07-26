package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Setting;
import com.coffeeshop.repository.SettingRepository;
import com.coffeeshop.dto.admin.response.AdminSettingResponseDTO;
import com.coffeeshop.dto.admin.request.AdminSettingRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerSettingResponseDTO;

@Service
public class SettingService {
	@Autowired
	private SettingRepository settingRepository;

	public List<Setting> getAllSettings() {
		return settingRepository.findAll();
	}

	public Optional<Setting> getSettingByKey(String key) {
		return settingRepository.findBySettingKey(key);
	}

	public Setting saveSetting(Setting setting) {
		setting.setUpdatedAt(LocalDateTime.now());
		return settingRepository.save(setting);
	}

	public void deleteSetting(String key) {
		settingRepository.deleteById(key);
	}

	public BigDecimal getTaxRate() {
		return getSettingByKey("tax_rate")
				.map(setting -> new BigDecimal(setting.getSettingValue()))
				.orElse(new BigDecimal("0.08")); // Default tax rate 8%
	}

	// New methods for DTO mapping and business logic
	public List<AdminSettingResponseDTO> getAllAdminSettings() {
		return getAllSettings().stream()
			.map(this::toAdminSettingResponseDTO)
			.toList();
	}

	public List<CustomerSettingResponseDTO> getPublicSettings() {
		return getAllSettings().stream()
			.filter(s -> Boolean.TRUE.equals(s.getIsActive()))
			.map(this::toCustomerSettingResponseDTO)
			.toList();
	}

	public AdminSettingResponseDTO getAdminSettingByKey(String key) {
		return getSettingByKey(key)
			.map(this::toAdminSettingResponseDTO)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy setting với key: " + key));
	}

	public AdminSettingResponseDTO createSetting(AdminSettingRequestDTO request) {
		Setting setting = new Setting();
		setting.setSettingKey(request.getKey());
		setting.setSettingValue(request.getValue());
		setting.setDescription(request.getDescription());
		setting.setIsActive(request.getIsActive());
		setting.setCreatedAt(LocalDateTime.now());
		setting.setUpdatedAt(LocalDateTime.now());
		
		Setting savedSetting = saveSetting(setting);
		return toAdminSettingResponseDTO(savedSetting);
	}

	public AdminSettingResponseDTO updateSetting(String key, AdminSettingRequestDTO request) {
		Setting existingSetting = getSettingByKey(key)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy setting với key: " + key));
		
		existingSetting.setSettingValue(request.getValue());
		existingSetting.setDescription(request.getDescription());
		existingSetting.setIsActive(request.getIsActive());
		existingSetting.setUpdatedAt(LocalDateTime.now());
		
		Setting updatedSetting = saveSetting(existingSetting);
		return toAdminSettingResponseDTO(updatedSetting);
	}

	// Private mapping methods
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