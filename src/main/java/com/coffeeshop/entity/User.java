package com.coffeeshop.entity;

import java.time.LocalDateTime;

import com.coffeeshop.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Entity đại diện cho bảng users trong database
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(length = 50, nullable = false, unique = true)
	private String username; // Tên đăng nhập duy nhất

	@Column(length = 100, nullable = false, unique = true)
	private String email; // Email duy nhất

	@Column(length = 255, nullable = false)
	private String password; // Mật khẩu đã mã hóa

	@Column(length = 100, nullable = false)
	private String fullName; // Họ và tên đầy đủ

	@Column(length = 15)
	private String phone; // Số điện thoại

	@Column(length = 255)
	private String profileImage; // Đường dẫn ảnh đại diện

	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private UserRole role = UserRole.ROLE_CUSTOMER; // Vai trò người dùng

	@Column(nullable = false)
	private Boolean isActive = true; // Trạng thái hoạt động

	@Column(nullable = false)
	private LocalDateTime createdAt; // Thời gian tạo

	@Column(nullable = false)
	private LocalDateTime updatedAt; // Thời gian cập nhật cuối

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}