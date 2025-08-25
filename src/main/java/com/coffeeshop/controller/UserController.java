package com.coffeeshop.controller;

import com.coffeeshop.dto.shared.request.UserProfileUpdateRequestDTO;
import com.coffeeshop.dto.admin.response.AdminUserResponseDTO;
import com.coffeeshop.dto.admin.request.AdminUserRequestDTO;
import com.coffeeshop.service.UserService;
import com.coffeeshop.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @ModelAttribute UserProfileUpdateRequestDTO request,
        @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = fileStorageService.saveFile(image);
                request.setProfileImage(imageUrl);
            } catch (IOException e) {
                // Trả về lỗi cho FE nếu lưu thất bại
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lưu ảnh thất bại: " + e.getMessage());
            }
        }
        userService.updateUserProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

// ========== ADMIN APIs ==========

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllAdminUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getAdminUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody AdminUserRequestDTO request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @Valid @RequestBody AdminUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Xóa người dùng thành công!");
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserResponseDTO> toggleUserActive(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.toggleUserActive(id));
    }
}