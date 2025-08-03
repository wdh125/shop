package com.coffeeshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulingConfig {

    /**
     * Cung cấp một bean ScheduledExecutorService với một luồng duy nhất.
     * Dùng để thực thi các tác vụ được lên lịch trong ứng dụng.
     * @return một instance của ScheduledExecutorService.
     */
    @Bean
    ScheduledExecutorService taskScheduler() {
        return Executors.newSingleThreadScheduledExecutor();
    }
} 