package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.response.AdminReservationResponseDTO;
import com.coffeeshop.dto.customer.request.ReservationRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerReservationResponseDTO;
import com.coffeeshop.dto.customer.response.ReservationDetailDTO;
import com.coffeeshop.dto.customer.response.TableReservationStatusDTO;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.ReservationStatus;
import com.coffeeshop.enums.TableStatus;
import com.coffeeshop.service.ReservationService;
import com.coffeeshop.test.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for ReservationController
 */
@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private TableEntity availableTable;
    private ReservationRequestDTO reservationRequest;
    private CustomerReservationResponseDTO customerReservationResponse;
    private AdminReservationResponseDTO adminReservationResponse;
    private ReservationDetailDTO reservationDetail;
    private TableReservationStatusDTO tableReservationStatus;

    @BeforeEach
    void setUp() {
        // Setup available table
        availableTable = TestDataFactory.createTestTable("T01", 4, TableStatus.AVAILABLE);
        availableTable.setId(1);

        // Setup reservation request
        reservationRequest = new ReservationRequestDTO();
        reservationRequest.setTableId(1);
        reservationRequest.setReservationTime(LocalDateTime.now().plusHours(2));
        reservationRequest.setPartySize(4);
        reservationRequest.setSpecialRequests("Test reservation");

        // Setup customer reservation response
        customerReservationResponse = new CustomerReservationResponseDTO();
        customerReservationResponse.setId(1);
        customerReservationResponse.setTableNumber("T01");
        customerReservationResponse.setReservationTime(LocalDateTime.now().plusHours(2));
        customerReservationResponse.setPartySize(4);
        customerReservationResponse.setStatus(ReservationStatus.CONFIRMED);
        customerReservationResponse.setSpecialRequests("Test reservation");
        customerReservationResponse.setCreatedAt(LocalDateTime.now());

        // Setup admin reservation response
        adminReservationResponse = new AdminReservationResponseDTO();
        adminReservationResponse.setId(1);
        adminReservationResponse.setCustomerId(1);
        adminReservationResponse.setCustomerName("Test Customer");
        adminReservationResponse.setTableId(1);
        adminReservationResponse.setTableNumber("T01");
        adminReservationResponse.setReservationTime(LocalDateTime.now().plusHours(2));
        adminReservationResponse.setPartySize(4);
        adminReservationResponse.setStatus(ReservationStatus.CONFIRMED);
        adminReservationResponse.setSpecialRequests("Test reservation");
        adminReservationResponse.setCreatedAt(LocalDateTime.now());
        adminReservationResponse.setUpdatedAt(LocalDateTime.now());

        // Setup reservation detail
        reservationDetail = new ReservationDetailDTO();
        reservationDetail.setId(1);
        reservationDetail.setTableNumber("T01");
        reservationDetail.setReservationTime(LocalDateTime.now().plusHours(2));
        reservationDetail.setPartySize(4);
        reservationDetail.setStatus(ReservationStatus.CONFIRMED);
        reservationDetail.setSpecialRequests("Test reservation");

        // Setup table reservation status
        tableReservationStatus = new TableReservationStatusDTO();
        tableReservationStatus.setTableId(1);
        tableReservationStatus.setTableNumber("T01");
        tableReservationStatus.setCapacity(4);
        tableReservationStatus.setCurrentStatus(TableStatus.OCCUPIED);
        tableReservationStatus.setReservationId(1);
        tableReservationStatus.setCustomerName("Test Customer");
        tableReservationStatus.setReservationTime(LocalDateTime.now().plusHours(2));
    }

    @Test
    @DisplayName("Get available tables - Public access")
    void getAvailableTables_PublicAccess() throws Exception {
        // Given
        List<TableEntity> tables = Arrays.asList(availableTable);
        when(reservationService.getAvailableTables()).thenReturn(tables);

        // When & Then
        mockMvc.perform(get("/api/reservations/available-tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].capacity").value(4))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Get booked tables - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getBookedTables_AdminAccess() throws Exception {
        // Given
        List<TableReservationStatusDTO> bookedTables = Arrays.asList(tableReservationStatus);
        when(reservationService.getBookedTableStatusDTOs()).thenReturn(bookedTables);

        // When & Then
        mockMvc.perform(get("/api/reservations/booked-tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tableId").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].currentStatus").value("OCCUPIED"))
                .andExpect(jsonPath("$[0].customerName").value("Test Customer"));
    }

    @Test
    @DisplayName("Get booked tables - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getBookedTables_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reservations/booked-tables"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get all reservations - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllReservations_AdminAccess() throws Exception {
        // Given
        List<AdminReservationResponseDTO> reservations = Arrays.asList(adminReservationResponse);
        when(reservationService.getAllAdminReservationDTOs()).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Test Customer"))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"));
    }

    @Test
    @DisplayName("Get reservation by ID - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getReservationById_AdminAccess() throws Exception {
        // Given
        when(reservationService.getAdminReservationDTOById(1)).thenReturn(adminReservationResponse);

        // When & Then
        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test Customer"))
                .andExpect(jsonPath("$.tableNumber").value("T01"));
    }

    @Test
    @DisplayName("Create reservation - Authenticated user")  
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void createReservation_AuthenticatedUser() throws Exception {
        // Given
        when(reservationService.createReservation(any(ReservationRequestDTO.class), eq("customer")))
                .thenReturn(customerReservationResponse);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.partySize").value(4))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Create reservation - Unauthorized access")
    void createReservation_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/reservations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Create reservation - Invalid request")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void createReservation_InvalidRequest() throws Exception {
        // Given - invalid request (missing required fields)
        ReservationRequestDTO invalidRequest = new ReservationRequestDTO();

        // When & Then
        mockMvc.perform(post("/api/reservations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get reservations by current user - Authenticated user")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void getReservationsByCurrentUser_AuthenticatedUser() throws Exception {
        // Given
        List<CustomerReservationResponseDTO> reservations = Arrays.asList(customerReservationResponse);
        when(reservationService.getReservationsByUser("customer")).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T01"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Cancel reservation - Authenticated user")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void cancelReservation_AuthenticatedUser() throws Exception {
        // Given
        CustomerReservationResponseDTO cancelledReservation = new CustomerReservationResponseDTO();
        cancelledReservation.setId(1);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.cancelReservation(1, "customer")).thenReturn(cancelledReservation);

        // When & Then
        mockMvc.perform(put("/api/reservations/1/cancel")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Update reservation status - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateReservationStatus_AdminAccess() throws Exception {
        // Given
        ReservationDetailDTO updatedReservation = new ReservationDetailDTO();
        updatedReservation.setId(1);
        updatedReservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.updateReservationStatusAndReturnDTO(1, ReservationStatus.CONFIRMED))
                .thenReturn(updatedReservation);

        // When & Then
        mockMvc.perform(put("/api/reservations/1/status")
                .with(csrf())
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Update reservation - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateReservation_AdminAccess() throws Exception {
        // Given
        when(reservationService.updateReservation(eq(1), any(ReservationRequestDTO.class)))
                .thenReturn(reservationDetail);

        // When & Then
        mockMvc.perform(put("/api/reservations/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T01"))
                .andExpect(jsonPath("$.partySize").value(4));
    }

    @Test
    @DisplayName("Delete reservation - Admin access")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteReservation_AdminAccess() throws Exception {
        // Given
        doNothing().when(reservationService).deleteReservation(1);

        // When & Then
        mockMvc.perform(delete("/api/reservations/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete reservation - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void deleteReservation_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/reservations/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update reservation status - Customer access denied")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    void updateReservationStatus_CustomerAccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/reservations/1/status")
                .with(csrf())
                .param("status", "CONFIRMED"))
                .andExpect(status().isForbidden());
    }
}