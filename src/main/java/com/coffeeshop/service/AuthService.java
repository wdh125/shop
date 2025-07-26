package com.coffeeshop.service;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RefreshTokenService refreshTokenService;

    public ResponseEntity<?> login(AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Sai tên đăng nhập hoặc mật khẩu!");
        }
        String accessToken = jwtUtils.generateJwtToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        AuthResponseDTO response = new AuthResponseDTO(
            accessToken,
            refreshToken,
            user.getUsername(),
            user.getFullName(),
            user.getRole().name()
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> refreshToken(RefreshTokenRequestDTO request) {
        var tokenOpt = refreshTokenService.findByToken(request.getRefreshToken());
        if (tokenOpt.isPresent() && refreshTokenService.isValid(tokenOpt.get())) {
            String accessToken = jwtUtils.generateJwtToken(tokenOpt.get().getUser().getUsername());
            return ResponseEntity.ok(new RefreshTokenResponseDTO(accessToken));
        } else {
            return ResponseEntity.status(401).body("Refresh token không hợp lệ hoặc đã hết hạn!");
        }
    }

    public ResponseEntity<?> logout(UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().body("Không xác định được người dùng!");
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng!");
        }
        refreshTokenService.deleteByUser(user);
        return ResponseEntity.ok("Đăng xuất thành công!");
    }

    public ResponseEntity<?> register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng!");
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
        return ResponseEntity.ok("Đăng ký thành công! Bạn có thể đăng nhập ngay.");
    }
} 