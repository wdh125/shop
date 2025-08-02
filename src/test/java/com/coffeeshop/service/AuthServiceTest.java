package com.coffeeshop.service;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.entity.RefreshToken;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.exception.InvalidCredentialsException;
import com.coffeeshop.exception.UserAlreadyExistsException;
import com.coffeeshop.exception.ValidationException;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.security.JwtUtils;
import com.coffeeshop.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests authentication logic including login, register, refresh token, and logout
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @Mock
    private RefreshTokenService refreshTokenService;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private User testUser;
    private RefreshToken testRefreshToken;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedpassword");
        testUser.setFullName("Test User");
        testUser.setRole(UserRole.ROLE_CUSTOMER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken("refresh-token-123");
        testRefreshToken.setUser(testUser);
    }
    
    @Test
    @DisplayName("Login with valid credentials should return AuthResponseDTO")
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedpassword")).thenReturn(true);
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("jwt-token-123");
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);
        
        // Act
        AuthResponseDTO response = authService.login(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedpassword");
        verify(jwtUtils).generateJwtToken("testuser");
        verify(refreshTokenService).createRefreshToken(testUser);
    }
    
    @Test
    @DisplayName("Login with invalid username should throw InvalidCredentialsException")
    void login_WithInvalidUsername_ShouldThrowInvalidCredentialsException() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("invaliduser", "password123");
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Sai tên đăng nhập hoặc mật khẩu!");
        
        verify(userRepository).findByUsername("invaliduser");
        verifyNoInteractions(passwordEncoder, jwtUtils, refreshTokenService);
    }
    
    @Test
    @DisplayName("Login with invalid password should throw InvalidCredentialsException")
    void login_WithInvalidPassword_ShouldThrowInvalidCredentialsException() {
        // Arrange
        AuthRequestDTO request = new AuthRequestDTO("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedpassword")).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Sai tên đăng nhập hoặc mật khẩu!");
        
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "encodedpassword");
        verifyNoInteractions(jwtUtils, refreshTokenService);
    }
    
    @Test
    @DisplayName("Refresh token with valid token should return new access token")
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-123");
        when(refreshTokenService.findByToken("refresh-token-123")).thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenService.isValid(testRefreshToken)).thenReturn(true);
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("new-jwt-token-456");
        
        // Act
        RefreshTokenResponseDTO response = authService.refreshToken(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-jwt-token-456");
        
        verify(refreshTokenService).findByToken("refresh-token-123");
        verify(refreshTokenService).isValid(testRefreshToken);
        verify(jwtUtils).generateJwtToken("testuser");
    }
    
    @Test
    @DisplayName("Refresh token with invalid token should throw InvalidCredentialsException")
    void refreshToken_WithInvalidToken_ShouldThrowInvalidCredentialsException() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("invalid-token");
        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Refresh token không hợp lệ!");
        
        verify(refreshTokenService).findByToken("invalid-token");
        verifyNoInteractions(jwtUtils);
    }
    
    @Test
    @DisplayName("Refresh token with expired token should throw InvalidCredentialsException")
    void refreshToken_WithExpiredToken_ShouldThrowInvalidCredentialsException() {
        // Arrange
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("expired-token");
        when(refreshTokenService.findByToken("expired-token")).thenReturn(Optional.of(testRefreshToken));
        when(refreshTokenService.isValid(testRefreshToken)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(request))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Refresh token không hợp lệ!");
        
        verify(refreshTokenService).findByToken("expired-token");
        verify(refreshTokenService).isValid(testRefreshToken);
        verifyNoInteractions(jwtUtils);
    }
    
    @Test
    @DisplayName("Logout with valid token should revoke token and return success message")
    void logout_WithValidToken_ShouldRevokeTokenAndReturnSuccessMessage() {
        // Arrange
        when(refreshTokenService.findByToken("refresh-token-123")).thenReturn(Optional.of(testRefreshToken));
        
        // Act
        String result = authService.logout("refresh-token-123");
        
        // Assert
        assertThat(result).isEqualTo("Đăng xuất thành công!");
        
        verify(refreshTokenService).findByToken("refresh-token-123");
        verify(refreshTokenService).revokeToken(testRefreshToken);
    }
    
    @Test
    @DisplayName("Logout with invalid token should still return success message")
    void logout_WithInvalidToken_ShouldReturnSuccessMessage() {
        // Arrange
        when(refreshTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());
        
        // Act
        String result = authService.logout("invalid-token");
        
        // Assert
        assertThat(result).isEqualTo("Đăng xuất thành công!");
        
        verify(refreshTokenService).findByToken("invalid-token");
        verify(refreshTokenService, never()).revokeToken(any());
    }
    
    @Test
    @DisplayName("Register with valid data should create new user")
    void register_WithValidData_ShouldCreateNewUser() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("newuser", "password123", 
                "newuser@example.com", "New User", "1234567890");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedpassword123");
        
        // Act
        String result = authService.register(request);
        
        // Assert
        assertThat(result).isEqualTo("Đăng ký thành công!");
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user -> 
            user.getUsername().equals("newuser") &&
            user.getEmail().equals("newuser@example.com") &&
            user.getFullName().equals("New User") &&
            user.getPhone().equals("1234567890") &&
            user.getRole().equals(UserRole.ROLE_CUSTOMER) &&
            user.getIsActive().equals(true)
        ));
    }
    
    @Test
    @DisplayName("Register with existing username should throw UserAlreadyExistsException")
    void register_WithExistingUsername_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("existinguser", "password123", 
                "newuser@example.com", "New User", null);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("Username đã tồn tại!");
        
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Register with existing email should throw UserAlreadyExistsException")
    void register_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("newuser", "password123", 
                "existing@example.com", "New User", null);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("Email đã được sử dụng!");
        
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Register with empty username should throw ValidationException")
    void register_WithEmptyUsername_ShouldThrowValidationException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("", "password123", 
                "test@example.com", "Test User", null);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Username không được để trống!");
        
        verifyNoInteractions(userRepository);
    }
    
    @Test
    @DisplayName("Register with null email should throw ValidationException")
    void register_WithNullEmail_ShouldThrowValidationException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("testuser", "password123", 
                null, "Test User", null);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Email không được để trống!");
        
        verifyNoInteractions(userRepository);
    }
    
    @Test
    @DisplayName("Register with empty password should throw ValidationException")
    void register_WithEmptyPassword_ShouldThrowValidationException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("testuser", "   ", 
                "test@example.com", "Test User", null);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Mật khẩu không được để trống!");
        
        verifyNoInteractions(userRepository);
    }
    
    @Test
    @DisplayName("Register with empty fullName should throw ValidationException")
    void register_WithEmptyFullName_ShouldThrowValidationException() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("testuser", "password123", 
                "test@example.com", "", null);
        
        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Họ tên không được để trống!");
        
        verifyNoInteractions(userRepository);
    }
}