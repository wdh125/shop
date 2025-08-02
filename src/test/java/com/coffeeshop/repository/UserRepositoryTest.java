package com.coffeeshop.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.coffeeshop.entity.User;
import com.coffeeshop.enums.UserRole;

import org.springframework.test.context.ActiveProfiles;

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
        testUser.setPassword("hashedpassword");
        testUser.setFullName("Test User");
        testUser.setPhone("0901234567");
        testUser.setRole(UserRole.ROLE_CUSTOMER);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByUsername_ExistingUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByUsername_NonExistingUser() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByEmail_ExistingUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        User found = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
        assertThat(found.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByEmail_NonExistingUser() {
        // Act
        User found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isNull();
    }

    @Test
    void testExistsByUsername_ExistingUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByUsername("testuser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByUsername_NonExistingUser() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByEmail_ExistingUser() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_NonExistingUser() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void testSave_NewUser() {
        // Act
        User saved = userRepository.save(testUser);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.ROLE_CUSTOMER);
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void testFindById_ExistingUser() {
        // Arrange
        User saved = entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> found = userRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void testDelete_ExistingUser() {
        // Arrange
        User saved = entityManager.persistAndFlush(testUser);
        Integer userId = saved.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }
}