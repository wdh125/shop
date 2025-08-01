package com.coffeeshop.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO cho request đăng nhập
 * Dùng cho API login
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequestDTO {
    @NotBlank(message = "Username không được để trống")
    private String username;     // Tên đăng nhập
    
    @NotBlank(message = "Password không được để trống")
    private String password;     // Mật khẩu

    // Constructor mặc định
    public AuthRequestDTO() {}

    public AuthRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 