package com.coffeeshop.service;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;

// Interface service xử lý xác thực người dùng
public interface AuthService {

    // Đăng nhập người dùng với thông tin đăng nhập
    AuthResponseDTO login(AuthRequestDTO request);

    // Làm mới access token bằng refresh token
    RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request);

    // Đăng xuất người dùng bằng cách vô hiệu hóa refresh token
    String logout(String refreshToken);

    // Đăng ký người dùng mới
    String register(RegisterRequestDTO request);
}
