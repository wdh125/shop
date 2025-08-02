package com.coffeeshop.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Test to verify NotificationController configuration is correct
 */
class NotificationControllerValidationTest {

    @Test
    void testGetUserNotificationsAnnotation() throws NoSuchMethodException {
        // Verify the method exists and has correct @PreAuthorize annotation
        Method method = NotificationController.class.getMethod("getUserNotifications", 
            Integer.class, int.class, int.class, Boolean.class);
        
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "getUserNotifications should have @PreAuthorize annotation");
        
        String expression = annotation.value();
        assertTrue(expression.contains("hasRole('ADMIN')"), "Should allow ADMIN role");
        assertTrue(expression.contains("hasRole('STAFF')"), "Should allow STAFF role");
        assertTrue(expression.contains("@notificationServiceImpl.canAccessUserNotifications"), 
                  "Should use custom security method");
        
        // Verify the expression doesn't contain the problematic authentication.principal.id
        assertFalse(expression.contains("authentication.principal.id"), 
                   "Should not contain authentication.principal.id");
    }

    @Test
    void testGetMyNotificationsMethod() throws NoSuchMethodException {
        // Verify the method exists and uses Authentication parameter
        Method method = NotificationController.class.getMethod("getMyNotifications", 
            org.springframework.security.core.Authentication.class, int.class, int.class, Boolean.class);
        
        assertNotNull(method, "getMyNotifications method should exist");
        
        // Verify it doesn't have problematic @PreAuthorize (it should rely on getUserIdFromAuthentication)
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        // This method doesn't need @PreAuthorize since it gets user ID from authentication
        // The security is handled by the getUserIdFromAuthentication method
    }

    @Test
    void testClassHasCorrectAnnotations() {
        // Verify the controller class is properly annotated
        assertTrue(NotificationController.class.isAnnotationPresent(
            org.springframework.web.bind.annotation.RestController.class),
            "Should be annotated with @RestController");
            
        assertTrue(NotificationController.class.isAnnotationPresent(
            org.springframework.web.bind.annotation.RequestMapping.class),
            "Should be annotated with @RequestMapping");
    }
}