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

// Service implementation xử lý xác thực người dùng
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
        // Tìm user theo username
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        
        // Kiểm tra mật khẩu và trả về lỗi nếu không đúng
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login attempt failed for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Sai tên đăng nhập hoặc mật khẩu!", request.getUsername());
        }
        
        // Tạo access token và refresh token
        String accessToken = jwtUtils.generateJwtToken(user.getUsername());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();
        
        logger.info("User {} logged in successfully", user.getUsername());
        
        // Tạo response DTO
        AuthResponseDTO response = new AuthResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());
        
        return response;
    }

    @Override
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        // Tìm refresh token trong database
        var tokenOpt = refreshTokenService.findByToken(request.getRefreshToken());
        
        // Kiểm tra tính hợp lệ và tạo access token mới
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
        // Tìm refresh token để vô hiệu hóa
        var tokenOpt = refreshTokenService.findByToken(refreshToken);
        
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            User user = token.getUser();
            
            logger.info("User {} logging out", user.getUsername());
            
            // Vô hiệu hóa refresh token
            refreshTokenService.revokeToken(token);
            
            return "Đăng xuất thành công!";
        }
        
        return "Đăng xuất thành công!";
    }

    @Override
    public String register(RegisterRequestDTO request) {
        // Validate dữ liệu đầu vào
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

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username đã tồn tại!", request.getUsername());
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã được sử dụng!", request.getEmail());
        }

        // Tạo user mới với thông tin đã validate
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(UserRole.ROLE_CUSTOMER); // Vai trò mặc định cho đăng ký
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        
        logger.info("New user registered: {}", user.getUsername());
        
        return "Đăng ký thành công!";
    }
}