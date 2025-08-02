package com.coffeeshop.controller;

import com.coffeeshop.enums.ReservationStatus;
import com.coffeeshop.service.PaymentService;
import com.coffeeshop.service.StatisticsService;
import com.coffeeshop.controller.admin.StatisticsController;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for high priority bugfixes
 */
public class HighPriorityBugfixTest {

    // Test 1: Test enum validation logic (unit test the logic, not web layer)
    @Test
    public void testReservationStatusEnumValidation() {
        // Test valid enum values
        assertDoesNotThrow(() -> ReservationStatus.valueOf("PENDING"));
        assertDoesNotThrow(() -> ReservationStatus.valueOf("CONFIRMED"));
        assertDoesNotThrow(() -> ReservationStatus.valueOf("CANCELLED"));
        
        // Test invalid enum value throws exception
        assertThrows(IllegalArgumentException.class, () -> ReservationStatus.valueOf("INVALID_STATUS"));
    }

    // Test 2: Test PaymentService has updated method signature
    @Test
    public void testPaymentServiceMethodSignature() throws NoSuchMethodException {
        // Verify the createPaymentForCustomer method now takes username parameter
        java.lang.reflect.Method method = PaymentService.class.getMethod(
            "createPaymentForCustomer", 
            CustomerPaymentRequestDTO.class, 
            String.class
        );
        assertNotNull(method, "PaymentService should have createPaymentForCustomer method with username parameter");
    }

    // Test 3: Test StatisticsController date validation logic
    @Test
    public void testDateRangeValidation() {
        StatisticsController controller = new StatisticsController();
        
        // Test valid date range (from <= to) - should not throw exception
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 2, 1);
        
        // Use reflection to test the private validation method
        try {
            java.lang.reflect.Method validateMethod = StatisticsController.class.getDeclaredMethod("validateDateRange", LocalDate.class, LocalDate.class);
            validateMethod.setAccessible(true);
            
            // Valid range should not throw exception
            assertDoesNotThrow(() -> {
                try {
                    validateMethod.invoke(controller, from, to);
                } catch (Exception e) {
                    if (e.getCause() instanceof RuntimeException) {
                        throw (RuntimeException) e.getCause();
                    }
                    throw new RuntimeException(e);
                }
            });
            
            // Invalid range (from > to) should throw exception
            LocalDate invalidFrom = LocalDate.of(2024, 2, 1);
            LocalDate invalidTo = LocalDate.of(2024, 1, 1);
            
            assertThrows(IllegalArgumentException.class, () -> {
                try {
                    validateMethod.invoke(controller, invalidFrom, invalidTo);
                } catch (Exception e) {
                    if (e.getCause() instanceof IllegalArgumentException) {
                        throw (IllegalArgumentException) e.getCause();
                    }
                    throw new RuntimeException(e);
                }
            });
            
        } catch (NoSuchMethodException e) {
            fail("StatisticsController should have validateDateRange method");
        }
    }

    // Test 4: Test that MethodArgumentTypeMismatchException handler exists
    @Test
    public void testGlobalExceptionHandlerHasEnumHandler() throws NoSuchMethodException {
        // Verify GlobalExceptionHandler has method to handle MethodArgumentTypeMismatchException
        java.lang.reflect.Method method = com.coffeeshop.exception.GlobalExceptionHandler.class.getMethod(
            "handleMethodArgumentTypeMismatchException", 
            MethodArgumentTypeMismatchException.class,
            org.springframework.web.context.request.WebRequest.class
        );
        assertNotNull(method, "GlobalExceptionHandler should have handleMethodArgumentTypeMismatchException method");
    }
}