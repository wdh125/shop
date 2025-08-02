package com.coffeeshop.repository;

import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for UserRepository
 * Tests data access methods for User entity
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedpassword");
        testUser.setFullName("Test User");
        testUser.setPhone("1234567890");
        testUser.setRole(UserRole.ROLE_CUSTOMER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Save and find user should work correctly")
    void saveAndFindUser_ShouldWorkCorrectly() {
        // Arrange & Act
        User savedUser = entityManager.persistAndFlush(testUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getFullName()).isEqualTo("Test User");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(foundUser.get().getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Find by username should return user when exists")
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> result = userRepository.findByUsername("testuser");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Find by username should return empty when user not exists")
    void findByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        // Act
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find by email should return user when exists")
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act  
        User result = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Find by email should return null when user not exists")
    void findByEmail_WhenUserNotExists_ShouldReturnNull() {
        // Act
        User result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Exists by username should return true when user exists")
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by username should return false when user not exists")
    void existsByUsername_WhenUserNotExists_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Exists by email should return true when user exists")
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by email should return false when user not exists")
    void existsByEmail_WhenUserNotExists_ShouldReturnFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Save user with admin role should work correctly")
    void saveUser_WithAdminRole_ShouldWorkCorrectly() {
        // Arrange
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("adminpassword");
        adminUser.setFullName("Admin User");
        adminUser.setRole(UserRole.ROLE_ADMIN);
        adminUser.setIsActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());

        // Act
        User savedUser = userRepository.save(adminUser);

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
        assertThat(savedUser.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Update user should work correctly")
    void updateUser_ShouldWorkCorrectly() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser);
        
        // Act
        savedUser.setFullName("Updated Name");
        savedUser.setPhone("9876543210");
        savedUser.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getPhone()).isEqualTo("9876543210");
        assertThat(updatedUser.getUsername()).isEqualTo("testuser"); // Should remain unchanged
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com"); // Should remain unchanged
    }

    @Test
    @DisplayName("Delete user should work correctly")
    void deleteUser_ShouldWorkCorrectly() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser);
        Integer userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Find all users should return all users")
    void findAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password2");
        user2.setFullName("Test User 2");
        user2.setRole(UserRole.ROLE_ADMIN);
        user2.setIsActive(true);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(user2);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getUsername)
                .containsExactlyInAnyOrder("testuser", "testuser2");
        assertThat(allUsers).extracting(User::getRole)
                .containsExactlyInAnyOrder(UserRole.ROLE_CUSTOMER, UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Username should be case sensitive")
    void findByUsername_ShouldBeCaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> result1 = userRepository.findByUsername("testuser");
        Optional<User> result2 = userRepository.findByUsername("TESTUSER");
        Optional<User> result3 = userRepository.findByUsername("TestUser");

        // Assert
        assertThat(result1).isPresent();
        assertThat(result2).isEmpty();
        assertThat(result3).isEmpty();
    }

    @Test
    @DisplayName("Email should be case sensitive")  
    void findByEmail_ShouldBeCaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        User result1 = userRepository.findByEmail("test@example.com");
        User result2 = userRepository.findByEmail("TEST@EXAMPLE.COM");
        User result3 = userRepository.findByEmail("Test@Example.Com");

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result2).isNull();
        assertThat(result3).isNull();
    }
}