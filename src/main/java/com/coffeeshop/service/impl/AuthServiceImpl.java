package com.coffeeshop.service.impl;

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
import com.coffeeshop.service.AuthService;
import com.coffeeshop.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.coffeeshop.entity.RefreshToken;

@Service
public class AuthServiceImpl implements AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Override
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
        
        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        
        return response;
    }

    @Override
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        var tokenOpt = refreshTokenService.findByToken(request.getRefreshToken());
        
        if (tokenOpt.isPresent() && refreshTokenService.isValid(tokenOpt.get())) {
            String accessToken = jwtUtils.generateJwtToken(tokenOpt.get().getUser().getUsername());
            RefreshTokenResponseDTO response = new RefreshTokenResponseDTO();
            response.setAccessToken(accessToken);
            return response;
        } else {
            throw new InvalidCredentialsException("Refresh token không hợp lệ!");
        }
    }

    @Override
    public String logout(String refreshToken) {
        var tokenOpt = refreshTokenService.findByToken(refreshToken);
        
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            User user = token.getUser();
            
            logger.info("User {} logging out", user.getUsername());
            
            // Revoke the current refresh token
            refreshTokenService.revokeToken(token);
            
            // Optionally revoke ALL refresh tokens for this user (more secure)
            refreshTokenService.deleteByUser(user);
            
            return "Đăng xuất thành công!";
        }
        
        return "Đăng xuất thành công!"; // Still return success even if token not found
    }

    @Override
    public String register(RegisterRequestDTO request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username không được để trống!");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email không được để trống!");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống!");
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new ValidationException("Họ tên không được để trống!");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username đã tồn tại!", request.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã được sử dụng!", request.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.ROLE_CUSTOMER); // Default role for registration
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        
        logger.info("New user registered: {}", user.getUsername());
        
        return "Đăng ký thành công!";
    }
}