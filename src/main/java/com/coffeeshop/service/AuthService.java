package com.coffeeshop.service;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;

/**
 * Authentication service interface for managing user authentication
 */
public interface AuthService {

    /**
     * Authenticate user with credentials
     */
    AuthResponseDTO login(AuthRequestDTO request);

    /**
     * Refresh access token using refresh token
     */
    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request);

    /**
     * Logout user by invalidating refresh token
     */
    String logout(String refreshToken);

    /**
     * Register new user
     */
    String register(RegisterRequestDTO request);
}
