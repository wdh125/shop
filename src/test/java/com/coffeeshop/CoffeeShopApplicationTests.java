package com.coffeeshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Main application context test to verify Spring Boot application loads correctly
 */
@SpringBootTest
@ActiveProfiles("test")
class CoffeeShopApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring Boot application context loads successfully
		// No additional assertions needed - if context fails to load, the test will fail
	}
}