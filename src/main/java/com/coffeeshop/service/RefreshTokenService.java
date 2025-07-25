package com.coffeeshop.service;

import com.coffeeshop.entity.RefreshToken;
import com.coffeeshop.entity.User;
import com.coffeeshop.repository.RefreshTokenRepository;
import com.coffeeshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private static final int REFRESH_TOKEN_LENGTH = 64;
    private static final int REFRESH_TOKEN_EXPIRE_DAYS = 7;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Sinh refresh token ngẫu nhiên
    public String generateRandomToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_LENGTH];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // Tạo và lưu refresh token mới cho user
    public RefreshToken createRefreshToken(User user) {
        // Xóa token cũ nếu có (1 user chỉ giữ 1 refresh token)
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateRandomToken());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRE_DAYS));
        refreshToken.setIsRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    // Kiểm tra token hợp lệ
    public boolean isValid(RefreshToken token) {
        return token != null && !token.getIsRevoked() && token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    // Thu hồi token
    public void revokeToken(RefreshToken token) {
        token.setIsRevoked(true);
        refreshTokenRepository.save(token);
    }

    // Lấy token theo chuỗi token
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Xóa tất cả token của user (logout all)
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
} 