package com.coffeeshop.dto.shared.response;

public class UserProfileResponseDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String profileImage;
    private String role;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;

    public UserProfileResponseDTO() {}
    public UserProfileResponseDTO(Integer id, String username, String fullName, String email, String phone, String profileImage, String role, Boolean isActive, String createdAt, String updatedAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.profileImage = profileImage;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public static UserProfileResponseDTO fromEntity(com.coffeeshop.entity.User u) {
        return new UserProfileResponseDTO(
            u.getId(),
            u.getUsername(),
            u.getFullName(),
            u.getEmail(),
            u.getPhone(),
            u.getProfileImage(),
            u.getRole() != null ? u.getRole().name() : null,
            u.getIsActive(),
            u.getCreatedAt() != null ? u.getCreatedAt().toString() : null,
            u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null
        );
    }
} 