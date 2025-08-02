package com.coffeeshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequestDTO loginRequest;
    private AuthResponseDTO authResponse;
    private RegisterRequestDTO registerRequest;
    private RefreshTokenRequestDTO refreshTokenRequest;
    private RefreshTokenResponseDTO refreshTokenResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new AuthRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        authResponse = new AuthResponseDTO("mock-jwt-token", "mock-refresh-token", 
                                          "testuser", "Test User", "ROLE_CUSTOMER");

        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setFullName("New User");
        registerRequest.setPhone("0901234567");

        refreshTokenRequest = new RefreshTokenRequestDTO();
        refreshTokenRequest.setRefreshToken("mock-refresh-token");

        refreshTokenResponse = new RefreshTokenResponseDTO();
        refreshTokenResponse.setAccessToken("new-jwt-token");
        // Note: RefreshTokenResponseDTO only has accessToken field
    }

    @Test
    void testLogin_Success() throws Exception {
        when(authService.login(any(AuthRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        when(authService.login(any(AuthRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testLogin_EmptyRequest() throws Exception {
        AuthRequestDTO emptyRequest = new AuthRequestDTO();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        when(authService.refreshToken(any(RefreshTokenRequestDTO.class))).thenReturn(refreshTokenResponse);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"));
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        when(authService.refreshToken(any(RefreshTokenRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testLogout_Success() throws Exception {
        when(authService.logout("mock-refresh-token")).thenReturn("Đăng xuất thành công!");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Đăng xuất thành công!"));
    }

    @Test
    void testRegister_Success() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn("Đăng ký thành công!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Đăng ký thành công!"));
    }

    @Test
    void testRegister_UserExists() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testRegister_InvalidData() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_InvalidEmail() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
        invalidRequest.setUsername("testuser");
        invalidRequest.setPassword("password123");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setFullName("Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}