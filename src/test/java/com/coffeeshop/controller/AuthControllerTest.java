package com.coffeeshop.controller;

import com.coffeeshop.config.TestSecurityConfig;
import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.exception.InvalidCredentialsException;
import com.coffeeshop.exception.UserAlreadyExistsException;
import com.coffeeshop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 * Tests REST API endpoints for authentication
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequestDTO validLoginRequest;
    private RegisterRequestDTO validRegisterRequest;
    private RefreshTokenRequestDTO validRefreshRequest;
    private AuthResponseDTO authResponse;
    private RefreshTokenResponseDTO refreshResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new AuthRequestDTO("testuser", "password123");
        
        validRegisterRequest = new RegisterRequestDTO();
        validRegisterRequest.setUsername("newuser");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setEmail("newuser@example.com");
        validRegisterRequest.setFullName("New User");
        validRegisterRequest.setPhone("1234567890");
        
        validRefreshRequest = new RefreshTokenRequestDTO("refresh-token-123");
        
        authResponse = new AuthResponseDTO();
        authResponse.setAccessToken("jwt-token-123");
        authResponse.setRefreshToken("refresh-token-123");
        authResponse.setUsername("testuser");
        authResponse.setRole("ROLE_CUSTOMER");
        
        refreshResponse = new RefreshTokenResponseDTO("new-jwt-token-456");
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials should return 200 and auth response")
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        // Arrange
        when(authService.login(any(AuthRequestDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));

        verify(authService).login(any(AuthRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid credentials should return 401")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Arrange
        when(authService.login(any(AuthRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("Sai tên đăng nhập hoặc mật khẩu!", "invaliduser"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService).login(any(AuthRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/login with missing username should return 400")
    void login_WithMissingUsername_ShouldReturn400() throws Exception {
        // Arrange
        AuthRequestDTO invalidRequest = new AuthRequestDTO("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/login with missing password should return 400")
    void login_WithMissingPassword_ShouldReturn400() throws Exception {
        // Arrange
        AuthRequestDTO invalidRequest = new AuthRequestDTO("testuser", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/register with valid data should return 200")
    void register_WithValidData_ShouldReturn200() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn("Đăng ký thành công!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Đăng ký thành công!"));

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register with existing username should return 409")
    void register_WithExistingUsername_ShouldReturn409() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Username đã tồn tại!", "existinguser"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict());

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/register with invalid email should return 400")
    void register_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Arrange
        validRegisterRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/register with short username should return 400")
    void register_WithShortUsername_ShouldReturn400() throws Exception {
        // Arrange
        validRegisterRequest.setUsername("ab"); // Less than 4 characters

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/register with short password should return 400")
    void register_WithShortPassword_ShouldReturn400() throws Exception {
        // Arrange
        validRegisterRequest.setPassword("123"); // Less than 6 characters

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/refresh with valid token should return 200")
    void refreshToken_WithValidToken_ShouldReturn200() throws Exception {
        // Arrange
        when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(refreshResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token-456"));

        verify(authService).refreshToken(any(RefreshTokenRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh with invalid token should return 401")
    void refreshToken_WithInvalidToken_ShouldReturn401() throws Exception {
        // Arrange
        when(authService.refreshToken(any(RefreshTokenRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("Refresh token không hợp lệ!"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRefreshRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService).refreshToken(any(RefreshTokenRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/auth/refresh with missing token should return 400")
    void refreshToken_WithMissingToken_ShouldReturn400() throws Exception {
        // Arrange
        RefreshTokenRequestDTO invalidRequest = new RefreshTokenRequestDTO("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/logout with valid token should return 200")
    void logout_WithValidToken_ShouldReturn200() throws Exception {
        // Arrange
        when(authService.logout(anyString())).thenReturn("Đăng xuất thành công!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Đăng xuất thành công!"));

        verify(authService).logout("refresh-token-123");
    }

    @Test
    @DisplayName("POST /api/auth/logout with missing token should return 400")
    void logout_WithMissingToken_ShouldReturn400() throws Exception {
        // Arrange
        RefreshTokenRequestDTO invalidRequest = new RefreshTokenRequestDTO("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/login with malformed JSON should return 400")
    void login_WithMalformedJson_ShouldReturn400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST requests without Content-Type should return 415")
    void authEndpoints_WithoutContentType_ShouldReturn415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(authService);
    }
}