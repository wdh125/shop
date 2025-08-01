package com.coffeeshop.controller;

import com.coffeeshop.dto.auth.AuthRequestDTO;
import com.coffeeshop.dto.auth.AuthResponseDTO;
import com.coffeeshop.dto.auth.RefreshTokenRequestDTO;
import com.coffeeshop.dto.auth.RefreshTokenResponseDTO;
import com.coffeeshop.dto.auth.RegisterRequestDTO;
import com.coffeeshop.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        RefreshTokenResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        String message = authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO request) {
        String message = authService.register(request);
        return ResponseEntity.ok(message);
    }
} 