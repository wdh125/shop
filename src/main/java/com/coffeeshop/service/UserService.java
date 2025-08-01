package com.coffeeshop.service;

import com.coffeeshop.entity.User;
import com.coffeeshop.dto.shared.request.UserProfileUpdateRequestDTO;
import com.coffeeshop.dto.shared.response.UserProfileResponseDTO;
import com.coffeeshop.dto.admin.response.AdminUserResponseDTO;
import com.coffeeshop.dto.admin.request.AdminUserRequestDTO;

import java.util.List;
import java.util.Optional;

/**
 * User service interface for managing users
 */
public interface UserService {

    /**
     * Get all users
     */
    List<User> getAllUsers();

    /**
     * Get user by ID
     */
    Optional<User> getUserById(Integer id);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Save or update user
     */
    User saveUser(User user);

    /**
     * Delete user by ID
     */
    void deleteUser(Integer id);

    /**
     * Encode password
     */
    String encodePassword(String rawPassword);

    /**
     * Check if password matches
     */
    boolean matchesPassword(String rawPassword, String encodedPassword);

    /**
     * Get current user profile
     */
    UserProfileResponseDTO getCurrentUserProfile(String username);

    /**
     * Update user profile
     */
    String updateUserProfile(String username, UserProfileUpdateRequestDTO request);

    /**
     * Get all admin users
     */
    List<AdminUserResponseDTO> getAllAdminUsers();

    /**
     * Get admin user by ID
     */
    AdminUserResponseDTO getAdminUserById(Integer id);

    /**
     * Create new user
     */
    AdminUserResponseDTO createUser(AdminUserRequestDTO request);

    /**
     * Update existing user
     */
    AdminUserResponseDTO updateUser(Integer id, AdminUserRequestDTO request);

    /**
     * Toggle user active status
     */
    AdminUserResponseDTO toggleUserActive(Integer id);
}