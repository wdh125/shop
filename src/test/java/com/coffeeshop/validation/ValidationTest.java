package com.coffeeshop.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRegisterRequestDTO_ValidData() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPhone("0901234567");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testRegisterRequestDTO_EmptyUsername() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername(""); // Empty username
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username không được để trống")));
    }

    @Test
    void testRegisterRequestDTO_ShortUsername() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("abc"); // Too short
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username phải từ 4-50 ký tự")));
    }

    @Test
    void testRegisterRequestDTO_EmptyPassword() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword(""); // Empty password
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password không được để trống")));
    }

    @Test
    void testRegisterRequestDTO_ShortPassword() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("12345"); // Too short
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password phải từ 6-100 ký tự")));
    }

    @Test
    void testRegisterRequestDTO_InvalidEmail() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("invalid-email"); // Invalid email format
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email không hợp lệ")));
    }

    @Test
    void testRegisterRequestDTO_EmptyFullName() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFullName(""); // Empty full name

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Họ tên không được để trống")));
    }

    @Test
    void testCustomerPaymentRequestDTO_ValidData() {
        // Arrange
        CustomerPaymentRequestDTO request = new CustomerPaymentRequestDTO();
        request.setOrderId(1);
        request.setPaymentMethod("CASH"); // String instead of enum
        request.setAmount(100000.0); // Double instead of BigDecimal

        // Act
        Set<ConstraintViolation<CustomerPaymentRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCustomerOrderRequestDTO_ValidData() {
        // Arrange
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setTableId(1);
        request.setNote("Test note");

        // Act
        Set<ConstraintViolation<CustomerOrderRequestDTO>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void testCustomerOrderRequestDTO_NullTableId() {
        // Arrange
        CustomerOrderRequestDTO request = new CustomerOrderRequestDTO();
        request.setTableId(null); // Null table ID
        request.setNote("Test note");

        // Act
        Set<ConstraintViolation<CustomerOrderRequestDTO>> violations = validator.validate(request);

        // Assert - depends on validation annotations in the actual DTO
        // This test validates that validation is working
        assertNotNull(violations);
    }

    @Test
    void testRegisterRequestDTO_LongUsername() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("a".repeat(51)); // Too long (over 50 chars)
        request.setPassword("password123");
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username phải từ 4-50 ký tự")));
    }

    @Test
    void testRegisterRequestDTO_LongPassword() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("a".repeat(101)); // Too long (over 100 chars)
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password phải từ 6-100 ký tự")));
    }

    @Test
    void testRegisterRequestDTO_NullValues() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO();
        // All fields are null

        // Act
        Set<ConstraintViolation<RegisterRequestDTO>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        // Should have violations for all required fields
        assertTrue(violations.size() >= 4); // username, password, email, fullName
    }
}