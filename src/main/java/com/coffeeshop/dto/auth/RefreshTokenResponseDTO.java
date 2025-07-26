package com.coffeeshop.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO cho response refresh token thành công
 * Trả về access token mới
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenResponseDTO {
    private String accessToken;   // JWT access token mới

    // Constructor mặc định
    public RefreshTokenResponseDTO() {}

    public RefreshTokenResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
} 