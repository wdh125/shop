package com.coffeeshop.controller;

import com.coffeeshop.dto.SettingDTO;
import com.coffeeshop.entity.Setting;
import com.coffeeshop.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SettingController {
    @Autowired
    private SettingService settingService;

    @GetMapping
    public List<SettingDTO> getAllSettings() {
        return settingService.getAllSettings().stream().map(SettingDTO::fromEntity).toList();
    }

    @GetMapping("/{key}")
    public SettingDTO getSettingByKey(@PathVariable String key) {
        return settingService.getSettingByKey(key).map(SettingDTO::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy setting!"));
    }

    @PostMapping
    public SettingDTO createSetting(@RequestBody Setting setting) {
        return SettingDTO.fromEntity(settingService.saveSetting(setting));
    }

    @PutMapping("/{key}")
    public SettingDTO updateSetting(@PathVariable String key, @RequestBody Setting setting) {
        setting.setSettingKey(key);
        return SettingDTO.fromEntity(settingService.saveSetting(setting));
    }

    @DeleteMapping("/{key}")
    public void deleteSetting(@PathVariable String key) {
        settingService.deleteSetting(key);
    }
}