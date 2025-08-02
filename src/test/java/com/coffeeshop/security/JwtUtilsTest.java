package com.coffeeshop.security;

import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.test.util.TestDataFactory;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JwtUtils
 */
@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtUtils jwtUtils;

    private User testUser;
    private final String testSecret = "testSecretKey123456789012345678901234567890";
    private final long testExpirationMs = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser("testuser", UserRole.ROLE_CUSTOMER);
        testUser.setId(1);

        // Set test values using reflection
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    @DisplayName("Generate JWT token - Success")
    void generateJwtToken_Success() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtils.generateJwtToken(username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).contains(".");
        
        // Verify it has the typical JWT structure (header.payload.signature)
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    @DisplayName("Get username from JWT token - Success")
    void getUsernameFromJwtToken_Success() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // When
        String extractedUsername = jwtUtils.getUsernameFromJwtToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Get username from JWT token - Invalid token")
    void getUsernameFromJwtToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUsernameFromJwtToken(invalidToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Get username from JWT token - Null token")
    void getUsernameFromJwtToken_NullToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUsernameFromJwtToken(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Get username from JWT token - Empty token")
    void getUsernameFromJwtToken_EmptyToken() {
        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUsernameFromJwtToken(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Validate JWT token - Valid token")
    void validateJwtToken_ValidToken() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Validate JWT token - Invalid signature")
    void validateJwtToken_InvalidSignature() {
        // Given - token with wrong signature
        String tokenWithWrongSignature = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMn0.wrong_signature";

        // When
        boolean isValid = jwtUtils.validateJwtToken(tokenWithWrongSignature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Validate JWT token - Malformed token")
    void validateJwtToken_MalformedToken() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Validate JWT token - Null token")
    void validateJwtToken_NullToken() {
        // When
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Validate JWT token - Empty token")
    void validateJwtToken_EmptyToken() {
        // When
        boolean isValid = jwtUtils.validateJwtToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Get user role from token - Success")
    void getUserRoleFromToken_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        String token = jwtUtils.generateJwtToken("testuser");

        // When
        UserRole role = jwtUtils.getUserRoleFromToken(token);

        // Then
        assertThat(role).isEqualTo(UserRole.ROLE_CUSTOMER);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Get user role from token - User not found")
    void getUserRoleFromToken_UserNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        String token = jwtUtils.generateJwtToken("testuser");

        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUserRoleFromToken(token))
                .isInstanceOf(RuntimeException.class);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Get user role from token - Invalid token")
    void getUserRoleFromToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token";

        // When & Then
        assertThatThrownBy(() -> jwtUtils.getUserRoleFromToken(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Token expiration test - Success")
    void tokenExpiration_Success() throws InterruptedException {
        // Given - Set very short expiration time
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1000L); // 1 second
        String token = jwtUtils.generateJwtToken("testuser");

        // Initially valid
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();

        // Wait for token to expire
        Thread.sleep(1100);

        // When & Then - Should be expired
        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }

    @Test
    @DisplayName("Generate different tokens for different users")
    void generateDifferentTokensForDifferentUsers() {
        // Given
        String user1 = "user1";
        String user2 = "user2";

        // When
        String token1 = jwtUtils.generateJwtToken(user1);
        String token2 = jwtUtils.generateJwtToken(user2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtils.getUsernameFromJwtToken(token1)).isEqualTo(user1);
        assertThat(jwtUtils.getUsernameFromJwtToken(token2)).isEqualTo(user2);
    }
}