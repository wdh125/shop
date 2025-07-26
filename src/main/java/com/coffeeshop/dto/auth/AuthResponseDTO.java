package com.coffeeshop.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO cho response đăng nhập thành công
 * Trả về token và thông tin user cơ bản
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private String accessToken;   // JWT access token
    private String refreshToken;  // Refresh token để lấy access token mới
    private String username;      // Tên đăng nhập
    private String fullName;      // Họ tên đầy đủ
    private String role;          // Vai trò (ROLE_CUSTOMER, ROLE_ADMIN)

    // Constructor mặc định
    public AuthResponseDTO() {}

    public AuthResponseDTO(String accessToken, String refreshToken, String username, String fullName, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
} 