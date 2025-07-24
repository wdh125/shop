package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Setting;
import com.coffeeshop.repository.SettingRepository;

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
		setting.setUpdatedAt(java.time.LocalDateTime.now());
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
}