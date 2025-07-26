package com.coffeeshop.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO cho request refresh token
 * Dùng cho API refresh token
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenRequestDTO {
    private String refreshToken;  // Refresh token để lấy access token mới

    // Constructor mặc định
    public RefreshTokenRequestDTO() {}

    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
} 