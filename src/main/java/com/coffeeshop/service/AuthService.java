package com.coffeeshop.service;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.exception.InvalidCredentialsException;
import com.coffeeshop.exception.UserAlreadyExistsException;
import com.coffeeshop.exception.ValidationException;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.coffeeshop.entity.RefreshToken;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RefreshTokenService refreshTokenService;

    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login attempt failed for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Sai tên đăng nhập hoặc mật khẩu!", request.getUsername());
        }
        
        String accessToken = jwtUtils.generateJwtToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        
        logger.info("User {} logged in successfully", user.getUsername());
        
        return new AuthResponseDTO(
            accessToken,
            refreshToken,
            user.getUsername(),
            user.getFullName(),
            user.getRole().name()
        );
    }

    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        var tokenOpt = refreshTokenService.findByToken(request.getRefreshToken());
        
        if (tokenOpt.isPresent() && refreshTokenService.isValid(tokenOpt.get())) {
            String accessToken = jwtUtils.generateJwtToken(tokenOpt.get().getUser().getUsername());
            logger.info("Access token refreshed for user: {}", tokenOpt.get().getUser().getUsername());
            return new RefreshTokenResponseDTO(accessToken);
        } else {
            logger.warn("Invalid or expired refresh token provided");
            throw new InvalidCredentialsException("Refresh token không hợp lệ hoặc đã hết hạn!");
        }
    }

    public String logout(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new ValidationException("Refresh token không được để trống!");
        }
        
        // Tìm và revoke refresh token
        var tokenOpt = refreshTokenService.findByToken(refreshToken);
        if (tokenOpt.isEmpty()) {
            throw new ValidationException("Refresh token không hợp lệ!", refreshToken);
        }
        
        RefreshToken token = tokenOpt.get();
        User user = token.getUser();
        
        // Revoke refresh token
        refreshTokenService.revokeToken(token);
        
        // Xóa tất cả refresh token của user
        refreshTokenService.deleteByUser(user);
        
        logger.info("User {} logged out successfully", user.getUsername());
        return "Đăng xuất thành công!";
    }

    public String register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username đã tồn tại!", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã được sử dụng!", request.getEmail());
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        logger.info("New user registered: {}", user.getUsername());
        
        return "Đăng ký thành công! Bạn có thể đăng nhập ngay.";
    }
} 