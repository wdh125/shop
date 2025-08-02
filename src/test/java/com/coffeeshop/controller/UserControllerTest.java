package com.coffeeshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.coffeeshop.dto.admin.request.AdminUserRequestDTO;
import com.coffeeshop.dto.admin.response.AdminUserResponseDTO;
import com.coffeeshop.dto.shared.request.UserProfileUpdateRequestDTO;
import com.coffeeshop.dto.shared.response.UserProfileResponseDTO;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProfileResponseDTO userProfileResponseDTO;
    private AdminUserResponseDTO adminUserResponseDTO;
    private UserProfileUpdateRequestDTO profileUpdateRequest;
    private AdminUserRequestDTO adminUserRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        userProfileResponseDTO = new UserProfileResponseDTO();
        userProfileResponseDTO.setId(1);
        userProfileResponseDTO.setUsername("testuser");
        userProfileResponseDTO.setEmail("test@example.com");
        userProfileResponseDTO.setFullName("Test User");
        userProfileResponseDTO.setPhoneNumber("0901234567");

        adminUserResponseDTO = new AdminUserResponseDTO();
        adminUserResponseDTO.setId(1);
        adminUserResponseDTO.setUsername("testuser");
        adminUserResponseDTO.setEmail("test@example.com");
        adminUserResponseDTO.setFullName("Test User");
        adminUserResponseDTO.setRole(UserRole.CUSTOMER);
        adminUserResponseDTO.setActive(true);

        profileUpdateRequest = new UserProfileUpdateRequestDTO();
        profileUpdateRequest.setFullName("Updated Name");
        profileUpdateRequest.setEmail("updated@example.com");
        profileUpdateRequest.setPhoneNumber("0909876543");

        adminUserRequest = new AdminUserRequestDTO();
        adminUserRequest.setUsername("newuser");
        adminUserRequest.setEmail("new@example.com");
        adminUserRequest.setFullName("New User");
        adminUserRequest.setPassword("password123");
        adminUserRequest.setRole(UserRole.CUSTOMER);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetCurrentUserProfile_Success() throws Exception {
        when(userService.getCurrentUserProfile("testuser")).thenReturn(userProfileResponseDTO);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void testGetCurrentUserProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_Success() throws Exception {
        when(userService.updateUserProfile(eq("testuser"), any(UserProfileUpdateRequestDTO.class)))
                .thenReturn("Cập nhật profile thành công!");

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Cập nhật profile thành công!"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateProfile_InvalidData() throws Exception {
        UserProfileUpdateRequestDTO invalidRequest = new UserProfileUpdateRequestDTO();
        // Empty fields should trigger validation errors

        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateProfile_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_Success() throws Exception {
        List<AdminUserResponseDTO> users = Arrays.asList(adminUserResponseDTO);
        when(userService.getAllAdminUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllUsers_Forbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_Success() throws Exception {
        when(userService.getAdminUserById(1)).thenReturn(adminUserResponseDTO);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser_Success() throws Exception {
        when(userService.createUser(any(AdminUserRequestDTO.class))).thenReturn(adminUserResponseDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser_InvalidData() throws Exception {
        AdminUserRequestDTO invalidRequest = new AdminUserRequestDTO();
        // Empty required fields should trigger validation

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateUser_Forbidden() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_Success() throws Exception {
        when(userService.updateUser(eq(1), any(AdminUserRequestDTO.class))).thenReturn(adminUserResponseDTO);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Xóa người dùng thành công!"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testDeleteUser_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testToggleUserActive_Success() throws Exception {
        AdminUserResponseDTO toggledUser = new AdminUserResponseDTO();
        toggledUser.setId(1);
        toggledUser.setActive(false);
        when(userService.toggleUserActive(1)).thenReturn(toggledUser);

        mockMvc.perform(patch("/api/users/1/toggle-active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(false));
    }
}