package com.coffeeshop.repository;

import com.coffeeshop.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> { // ID is String
    Optional<Setting> findBySettingKey(String settingKey);
}