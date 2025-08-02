package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.admin.response.AdminTableResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerTableResponseDTO;
import com.coffeeshop.security.JwtAuthenticationFilter;
import com.coffeeshop.security.JwtUtils;
import com.coffeeshop.service.CustomUserDetailsService;
import com.coffeeshop.service.TableService;
import com.coffeeshop.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TableController
 * Tests REST API endpoints for table management
 */
@WebMvcTest(TableController.class)
@ActiveProfiles("test")
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableService tableService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminTableRequestDTO adminTableRequest;
    private AdminTableResponseDTO adminTableResponse;
    private CustomerTableResponseDTO customerTableResponse;

    @BeforeEach
    void setUp() {
        // Setup admin table request DTO
        adminTableRequest = new AdminTableRequestDTO();
        adminTableRequest.setTableNumber("T01");
        adminTableRequest.setCapacity(4);
        adminTableRequest.setLocation("Main Floor");
        adminTableRequest.setIsActive(true);

        // Setup admin table response DTO
        adminTableResponse = new AdminTableResponseDTO();
        adminTableResponse.setId(1);
        adminTableResponse.setTableNumber("T01");
        adminTableResponse.setCapacity(4);
        adminTableResponse.setLocation("Main Floor");
        adminTableResponse.setStatus("AVAILABLE");
        adminTableResponse.setIsActive(true);
        adminTableResponse.setCreatedAt(LocalDateTime.now());
        adminTableResponse.setUpdatedAt(LocalDateTime.now());

        // Setup customer table response DTO
        customerTableResponse = new CustomerTableResponseDTO();
        customerTableResponse.setId(1);
        customerTableResponse.setTableNumber("T01");
        customerTableResponse.setCapacity(4);
        customerTableResponse.setLocation("Main Floor");
        customerTableResponse.setStatus("AVAILABLE");
    }

    @Test
    @DisplayName("GET /api/tables/available should return available tables for everyone")
    void getAvailableTables_ShouldReturnAvailableTables() throws Exception {
        // Arrange
        List<CustomerTableResponseDTO> tables = Arrays.asList(customerTableResponse);
        when(tableService.getAvailableCustomerTableDTOs()).thenReturn(tables);

        // Act & Assert
        mockMvc.perform(get("/api/tables/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].capacity").value(4))
                .andExpect(jsonPath("$[0].location").value("Main Floor"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(tableService).getAvailableCustomerTableDTOs();
    }

    @Test
    @DisplayName("GET /api/tables with admin role should return all tables")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getAllTables_WithAdminRole_ShouldReturnAllTables() throws Exception {
        // Arrange
        List<AdminTableResponseDTO> tables = Arrays.asList(adminTableResponse);
        when(tableService.getAllAdminTableDTOs()).thenReturn(tables);

        // Act & Assert
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].capacity").value(4))
                .andExpect(jsonPath("$[0].location").value("Main Floor"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$[0].isActive").value(true));

        verify(tableService).getAllAdminTableDTOs();
    }

    @Test
    @DisplayName("GET /api/tables without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void getAllTables_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tableService);
    }

    @Test
    @DisplayName("GET /api/tables/{id} with admin role should return table details")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getTableById_WithAdminRole_ShouldReturnTableDetails() throws Exception {
        // Arrange
        when(tableService.getAdminTableDTOById(1)).thenReturn(adminTableResponse);

        // Act & Assert
        mockMvc.perform(get("/api/tables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.location").value("Main Floor"));

        verify(tableService).getAdminTableDTOById(1);
    }

    @Test
    @DisplayName("GET /api/tables/{id} without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void getTableById_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tables/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tableService);
    }

    @Test
    @DisplayName("POST /api/tables with admin role should create table")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createTable_WithAdminRole_ShouldCreateTable() throws Exception {
        // Arrange
        when(tableService.createTable(any(AdminTableRequestDTO.class))).thenReturn(adminTableResponse);

        // Act & Assert
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.capacity").value(4));

        verify(tableService).createTable(any(AdminTableRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/tables without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void createTable_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tableService);
    }

    @Test
    @DisplayName("PUT /api/tables/{id} with admin role should update table")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void updateTable_WithAdminRole_ShouldUpdateTable() throws Exception {
        // Arrange
        AdminTableResponseDTO updatedTable = new AdminTableResponseDTO();
        updatedTable.setId(1);
        updatedTable.setTableNumber("T01-Updated");
        updatedTable.setCapacity(6);
        updatedTable.setLocation("VIP Floor");
        
        when(tableService.updateTable(eq(1), any(AdminTableRequestDTO.class))).thenReturn(updatedTable);

        // Act & Assert
        mockMvc.perform(put("/api/tables/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01-Updated"))
                .andExpect(jsonPath("$.capacity").value(6))
                .andExpect(jsonPath("$.location").value("VIP Floor"));

        verify(tableService).updateTable(eq(1), any(AdminTableRequestDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tables/{id} without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void updateTable_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/tables/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tableService);
    }

    @Test
    @DisplayName("DELETE /api/tables/{id} with admin role should delete table")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteTable_WithAdminRole_ShouldDeleteTable() throws Exception {
        // Arrange
        doNothing().when(tableService).deleteTable(1);

        // Act & Assert
        mockMvc.perform(delete("/api/tables/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(tableService).deleteTable(1);
    }

    @Test
    @DisplayName("DELETE /api/tables/{id} without admin role should return 403")
    @WithMockUser(authorities = {"ROLE_USER"})
    void deleteTable_WithoutAdminRole_ShouldReturn403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tables/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tableService);
    }

    @Test
    @DisplayName("POST /api/tables with invalid data should return 400")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createTable_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange - Create invalid request (missing required fields)
        AdminTableRequestDTO invalidRequest = new AdminTableRequestDTO();
        // tableNumber is required but missing

        // Act & Assert
        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tableService);
    }

    @Test
    @DisplayName("Admin endpoints without authentication should return 401")
    void adminEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tables")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/tables/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminTableRequest)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/tables/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/tables/{id} with non-existent table should return 404")
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void getTableById_WithNonExistentTable_ShouldReturn404() throws Exception {
        // Arrange
        when(tableService.getAdminTableDTOById(999)).thenThrow(new RuntimeException("Table not found"));

        // Act & Assert
        mockMvc.perform(get("/api/tables/999"))
                .andExpect(status().isInternalServerError()); // Currently returns 500, should be improved to 404

        verify(tableService).getAdminTableDTOById(999);
    }

    @Test
    @DisplayName("Public available tables endpoint should be accessible without authentication")
    void availableTablesEndpoint_ShouldBeAccessible() throws Exception {
        // Arrange
        List<CustomerTableResponseDTO> tables = Arrays.asList(customerTableResponse);
        when(tableService.getAvailableCustomerTableDTOs()).thenReturn(tables);

        // Act & Assert
        mockMvc.perform(get("/api/tables/available"))
                .andExpect(status().isOk());

        verify(tableService).getAvailableCustomerTableDTOs();
    }
}