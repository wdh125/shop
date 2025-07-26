package com.coffeeshop.service;

import com.coffeeshop.entity.User;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.dto.shared.request.UserProfileUpdateRequestDTO;
import com.coffeeshop.dto.shared.response.UserProfileResponseDTO;
import com.coffeeshop.dto.admin.response.AdminUserResponseDTO;
import com.coffeeshop.dto.admin.request.AdminUserRequestDTO;
import com.coffeeshop.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setCreatedAt(LocalDateTime.now());
        } else if (user.getCreatedAt() == null) {
            user.setCreatedAt(userRepository.findById(user.getId())
                .map(User::getCreatedAt)
                .orElse(LocalDateTime.now()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
    
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // New methods for DTO mapping and business logic
    public UserProfileResponseDTO getCurrentUserProfile(String username) {
        User user = findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        return UserProfileResponseDTO.fromEntity(user);
    }

    public String updateUserProfile(String username, UserProfileUpdateRequestDTO request) {
        User user = findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        
        // Kiểm tra email đã tồn tại chưa (nếu thay đổi email)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng!");
            }
            user.setEmail(request.getEmail());
        }
        
        // Cập nhật thông tin
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        
        // Xử lý đổi mật khẩu
        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            // Kiểm tra mật khẩu cũ
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại!");
            }
            if (!matchesPassword(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Mật khẩu hiện tại không đúng!");
            }
            // Kiểm tra xác nhận mật khẩu mới
            if (request.getConfirmPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
            }
            user.setPassword(encodePassword(request.getNewPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        saveUser(user);
        
        return "Cập nhật thông tin thành công!";
    }

    public List<AdminUserResponseDTO> getAllAdminUsers() {
        return getAllUsers().stream()
            .map(AdminUserResponseDTO::fromEntity)
            .toList();
    }

    public AdminUserResponseDTO getAdminUserById(Integer id) {
        return getUserById(id)
            .map(AdminUserResponseDTO::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
    }

    public AdminUserResponseDTO createUser(AdminUserRequestDTO request) {
        // Kiểm tra username và email đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encodePassword(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setProfileImage(request.getProfileImage());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.ROLE_CUSTOMER);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = saveUser(user);
        return AdminUserResponseDTO.fromEntity(savedUser);
    }

    public AdminUserResponseDTO updateUser(Integer id, AdminUserRequestDTO request) {
        User user = getUserById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        
        // Kiểm tra username và email đã tồn tại (nếu thay đổi)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username đã tồn tại!");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng!");
            }
            user.setEmail(request.getEmail());
        }
        
        // Cập nhật thông tin
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(encodePassword(request.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = saveUser(user);
        return AdminUserResponseDTO.fromEntity(savedUser);
    }

    public AdminUserResponseDTO toggleUserActive(Integer id) {
        User user = getUserById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        user.setIsActive(user.getIsActive() == null ? false : !user.getIsActive());
        User savedUser = saveUser(user);
        return AdminUserResponseDTO.fromEntity(savedUser);
    }
}