package com.coffeeshop.dto.admin.response;

import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;

public class AdminUserResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String profileImage;
    private UserRole role;
    private Boolean isActive;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public static AdminUserResponseDTO fromEntity(User u) {
        AdminUserResponseDTO dto = new AdminUserResponseDTO();
        dto.id = u.getId();
        dto.username = u.getUsername();
        dto.email = u.getEmail();
        dto.fullName = u.getFullName();
        dto.phone = u.getPhone();
        dto.profileImage = u.getProfileImage();
        dto.role = u.getRole();
        dto.isActive = u.getIsActive();
        dto.createdAt = u.getCreatedAt();
        dto.updatedAt = u.getUpdatedAt();
        return dto;
    }
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getProfileImage() { return profileImage; }
    public UserRole getRole() { return role; }
    public Boolean getIsActive() { return isActive; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
} 