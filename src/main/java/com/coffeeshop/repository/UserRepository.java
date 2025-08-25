package com.coffeeshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coffeeshop.entity.User;

import java.util.Optional;

// Repository interface cho entity User
public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByUsername(String username); // Tìm user theo username

	User findByEmail(String email); // Tìm user theo email

	boolean existsByUsername(String username); // Kiểm tra username đã tồn tại
	boolean existsByEmail(String email); // Kiểm tra email đã tồn tại
}