package com.coffeeshop.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.coffeeshop.entity.User;
import com.coffeeshop.repository.UserRepository;

/**
 * Test JwtUtils methods to ensure they work correctly for NotificationController
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilsSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtUtils jwtUtils;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(123);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testGetUserIdFromAuthentication_ValidUser_ShouldReturnUserId() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Integer userId = jwtUtils.getUserIdFromAuthentication(authentication);

        // Assert
        assertNotNull(userId);
        assertEquals(123, userId);
    }

    @Test
    void testGetUserIdFromAuthentication_UserNotFound_ShouldThrowException() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtils.getUserIdFromAuthentication(authentication);
        });
        
        assertTrue(exception.getMessage().contains("Không tìm thấy người dùng"));
    }

    @Test
    void testGetUserIdFromAuthentication_NullAuthentication_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtils.getUserIdFromAuthentication(null);
        });
        
        assertTrue(exception.getMessage().contains("Không thể lấy thông tin người dùng"));
    }

    @Test
    void testGetUserIdFromAuthentication_InvalidPrincipal_ShouldThrowException() {
        // Arrange  
        when(authentication.getPrincipal()).thenReturn("invalid_principal");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            jwtUtils.getUserIdFromAuthentication(authentication);
        });
        
        assertTrue(exception.getMessage().contains("Không thể lấy thông tin người dùng"));
    }
}