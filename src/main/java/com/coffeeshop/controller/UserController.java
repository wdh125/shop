package com.coffeeshop.controller;

import com.coffeeshop.dto.shared.request.UserProfileUpdateRequestDTO;
import com.coffeeshop.dto.shared.response.UserProfileResponseDTO;
import com.coffeeshop.dto.admin.response.AdminUserResponseDTO;
import com.coffeeshop.dto.admin.request.AdminUserRequestDTO;
import com.coffeeshop.entity.User;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Không xác định được người dùng!");
        }
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
        }
        return ResponseEntity.ok(UserProfileResponseDTO.fromEntity(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails, 
                                         @RequestBody UserProfileUpdateRequestDTO request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Không xác định được người dùng!");
        }
        
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("Không tìm thấy người dùng!");
        }
        
        // Kiểm tra email đã tồn tại chưa (nếu thay đổi email)
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email đã được sử dụng!");
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
                return ResponseEntity.badRequest().body("Vui lòng nhập mật khẩu hiện tại!");
            }
            if (!userService.matchesPassword(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng!");
            }
            // Kiểm tra xác nhận mật khẩu mới
            if (request.getConfirmPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp!");
            }
            user.setPassword(userService.encodePassword(request.getNewPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    // ========== ADMIN APIs ==========

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<AdminUserResponseDTO> userDTOs = users.stream()
                .map(AdminUserResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        return ResponseEntity.ok(AdminUserResponseDTO.fromEntity(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody AdminUserRequestDTO request) {
        // Kiểm tra username và email đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng!");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(userService.encodePassword(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setProfileImage(request.getProfileImage());
        user.setRole(request.getRole() != null ? request.getRole() : com.coffeeshop.enums.UserRole.ROLE_CUSTOMER);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(AdminUserResponseDTO.fromEntity(savedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody AdminUserRequestDTO request) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        
        // Kiểm tra username và email đã tồn tại (nếu thay đổi)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body("Username đã tồn tại!");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("Email đã được sử dụng!");
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
            user.setPassword(userService.encodePassword(request.getPassword()));
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(AdminUserResponseDTO.fromEntity(savedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa user thành công!");
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> toggleUserActive(@PathVariable Integer id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        user.setIsActive(user.getIsActive() == null ? false : !user.getIsActive());
        User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(AdminUserResponseDTO.fromEntity(savedUser));
    }
}