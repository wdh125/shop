package com.coffeeshop.service;

import com.coffeeshop.entity.User;
import com.coffeeshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
        // Có thể custom authorities/roles ở đây nếu cần
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Đã mã hóa
                .authorities(user.getRole().name()) // "ROLE_ADMIN" hoặc "ROLE_CUSTOMER"
                .accountLocked(!user.getIsActive())
                .disabled(!user.getIsActive())
                .build();
    }
} 