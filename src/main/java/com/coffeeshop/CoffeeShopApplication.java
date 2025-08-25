package com.coffeeshop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.coffeeshop.repository.UserRepository;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CoffeeShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeShopApplication.class, args);
	}

	@Bean
	@Profile("!test")
	CommandLineRunner testDatabase(UserRepository userRepository) {
		return args -> {
			System.out.println("📦 Danh sách người dùng từ MySQL:");
			userRepository.findAll().forEach(user -> {
				System.out.println("👤 " + user.getUsername() + " - Vai trò: " + user.getRole());
			});
		};
	}
}
