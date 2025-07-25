package com.coffeeshop.dto.auth;

public class RefreshTokenResponseDTO {
    private String accessToken;

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