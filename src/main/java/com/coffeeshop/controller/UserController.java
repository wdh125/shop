package com.coffeeshop.controller;

import com.coffeeshop.dto.UserDTO;
import com.coffeeshop.entity.User;
import com.coffeeshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(UserDTO::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Integer id) {
        return userService.getUserById(id).map(UserDTO::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
    }

    @GetMapping("/me")
    public UserDTO getCurrentUser(@RequestParam Integer userId) {
        return userService.getUserById(userId).map(UserDTO::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
    }

    @PostMapping
    public UserDTO createUser(@RequestBody User user) {
        return UserDTO.fromEntity(userService.saveUser(user));
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        return UserDTO.fromEntity(userService.saveUser(user));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }
}