package com.coffeeshop.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for custom exception classes to verify they work correctly.
 * This test can run without database connection.
 */
class ExceptionHandlingTest {

    @Test
    void testUserNotFoundException() {
        // Given
        String message = "User not found";
        String rejectedValue = "testuser";
        
        // When
        UserNotFoundException exception = new UserNotFoundException(message, rejectedValue);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        assertEquals(rejectedValue, exception.getRejectedValue());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testInvalidCredentialsException() {
        // Given
        String message = "Invalid credentials";
        
        // When
        InvalidCredentialsException exception = new InvalidCredentialsException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testUserAlreadyExistsException() {
        // Given
        String message = "User already exists";
        String rejectedValue = "existinguser";
        
        // When
        UserAlreadyExistsException exception = new UserAlreadyExistsException(message, rejectedValue);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("USER_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals(rejectedValue, exception.getRejectedValue());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testValidationException() {
        // Given
        String message = "Validation failed";
        String rejectedValue = "invalid_data";
        
        // When
        ValidationException exception = new ValidationException(message, rejectedValue);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(rejectedValue, exception.getRejectedValue());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testBusinessLogicExceptionWithErrorCode() {
        // Given
        String message = "Business logic error";
        String errorCode = "CUSTOM_ERROR";
        String rejectedValue = "test_value";
        
        // When
        BusinessLogicException exception = new BusinessLogicException(message, errorCode, rejectedValue);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(rejectedValue, exception.getRejectedValue());
    }

    @Test
    void testOrderNotFoundException() {
        // Given
        String message = "Order not found";
        Integer orderId = 123;
        
        // When
        OrderNotFoundException exception = new OrderNotFoundException(message, orderId);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals(orderId, exception.getRejectedValue());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testProductNotFoundException() {
        // Given
        String message = "Product not found";
        Integer productId = 456;
        
        // When
        ProductNotFoundException exception = new ProductNotFoundException(message, productId);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("PRODUCT_NOT_FOUND", exception.getErrorCode());
        assertEquals(productId, exception.getRejectedValue());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testReservationNotFoundException() {
        // Given
        String message = "Reservation not found";
        Integer reservationId = 789;
        
        // When
        ReservationNotFoundException exception = new ReservationNotFoundException(message, reservationId);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("RESERVATION_NOT_FOUND", exception.getErrorCode());
        assertEquals(reservationId, exception.getRejectedValue());
        assertTrue(exception instanceof BusinessLogicException);
    }

    @Test
    void testUnauthorizedAccessException() {
        // Given
        String message = "Unauthorized access";
        
        // When
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertEquals("UNAUTHORIZED_ACCESS", exception.getErrorCode());
        assertTrue(exception instanceof BusinessLogicException);
    }
}