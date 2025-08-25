package com.coffeeshop.config;

import com.coffeeshop.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Cấu hình bảo mật cho ứng dụng
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Cấu hình SecurityFilterChain chính
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Tắt CSRF cho API endpoints
                .ignoringRequestMatchers("/api/**")
            )
            // Bật CORS để cho phép frontend gọi API
            .cors(Customizer.withDefaults())

            // Cấu hình session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )

            // Cấu hình authorization cho các endpoint
            .authorizeHttpRequests(auth -> auth
                // Public pages - không cần đăng nhập
                .requestMatchers(
                    "/", "/home", "/menu", "/login", "/register", "/forgot-password",
                    "/css/**", "/js/**", "/images/**", "/img/**", "/static/**",
                    "/favicon.ico", "/error"
                ).permitAll()
                // Public static uploads
                .requestMatchers("/uploads/**").permitAll()
                // API endpoints cho authentication (public) và preflight OPTIONS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // API endpoints cho authentication
                .requestMatchers(
                    "/api/auth/login", 
                    "/api/auth/register", 
                    "/api/auth/refresh",
                    "/api/auth/logout"
                ).permitAll()
                // Public API: Cho phép GET products và categories không cần đăng nhập
                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                // Các request khác tới products/categories phải đăng nhập
                .requestMatchers("/api/products/**", "/api/categories/**").authenticated()
                // Admin pages
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // User pages - cần đăng nhập
                .requestMatchers("/profile/**", "/orders/**", "/cart/**", "/reservation/**").authenticated()
                // API endpoints - cần đăng nhập (trừ public APIs ở trên)
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )

            // Cấu hình form login cho web pages
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )

            // Cấu hình logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // Thêm JWT filter chỉ cho API requests
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình CORS cho phép frontend dev server gọi API
    @Bean
    org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("http://localhost:5173");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addExposedHeader("Authorization");

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Bean encoder mật khẩu
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}