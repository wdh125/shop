package com.coffeeshop.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.coffeeshop.entity.User;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.service.impl.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
class SecurityAuthorizationTest {

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    void testCanAccessUserNotifications_ValidAuthentication() {
        // Arrange
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanAccessUserNotifications_NullAuthentication() {
        // Act
        boolean result = notificationService.canAccessUserNotifications(1, null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_InvalidUserId() {
        // Act
        boolean result = notificationService.canAccessUserNotifications(null, authentication);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_AuthenticationWithoutPrincipal() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(null);

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_AdminRole() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanAccessUserNotifications_StaffRole() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList(new SimpleGrantedAuthority("ROLE_STAFF")));

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanAccessUserNotifications_DifferentUserAccess() {
        // Arrange
        User testUser = new User();
        testUser.setId(2); // Different user ID
        testUser.setUsername("anotheruser");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("anotheruser");
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList());
        when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_ZeroUserId() {
        // Arrange
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = notificationService.canAccessUserNotifications(0, authentication);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_NegativeUserId() {
        // Arrange
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = notificationService.canAccessUserNotifications(-1, authentication);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_UserNotFoundInDatabase() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenAnswer(invocation -> Arrays.asList());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessUserNotifications_UnauthenticatedUser() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        boolean result = notificationService.canAccessUserNotifications(1, authentication);

        // Assert
        assertFalse(result);
    }
}