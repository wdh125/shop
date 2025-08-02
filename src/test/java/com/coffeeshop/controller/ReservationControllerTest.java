package com.coffeeshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
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

import com.coffeeshop.dto.admin.response.AdminReservationResponseDTO;
import com.coffeeshop.dto.customer.request.ReservationRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerReservationResponseDTO;
import com.coffeeshop.dto.customer.response.ReservationDetailDTO;
import com.coffeeshop.dto.customer.response.TableReservationStatusDTO;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.ReservationStatus;
import com.coffeeshop.enums.TableStatus;
import com.coffeeshop.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private TableEntity availableTable;
    private TableReservationStatusDTO bookedTable;
    private ReservationRequestDTO reservationRequest;
    private CustomerReservationResponseDTO customerReservationResponse;
    private AdminReservationResponseDTO adminReservationResponse;
    private ReservationDetailDTO reservationDetail;

    @BeforeEach
    void setUp() {
        availableTable = new TableEntity();
        availableTable.setId(1);
        availableTable.setTableNumber("T001");
        availableTable.setCapacity(4);
        availableTable.setStatus(TableStatus.AVAILABLE);

        bookedTable = new TableReservationStatusDTO();
        bookedTable.setId(2);
        bookedTable.setTableNumber("T002");
        bookedTable.setCapacity(6);
        bookedTable.setStatus(TableStatus.RESERVED);

        reservationRequest = new ReservationRequestDTO();
        reservationRequest.setTableId(1);
        reservationRequest.setReservationTime(LocalDateTime.now().plusHours(2));
        reservationRequest.setGuestCount(4);
        reservationRequest.setSpecialRequests("Window seat preferred");

        customerReservationResponse = new CustomerReservationResponseDTO();
        customerReservationResponse.setId(1);
        customerReservationResponse.setTableNumber("T001");
        customerReservationResponse.setReservationTime(LocalDateTime.now().plusHours(2));
        customerReservationResponse.setGuestCount(4);
        customerReservationResponse.setStatus(ReservationStatus.CONFIRMED);

        adminReservationResponse = new AdminReservationResponseDTO();
        adminReservationResponse.setId(1);
        adminReservationResponse.setTableNumber("T001");
        adminReservationResponse.setCustomerName("Test User");
        adminReservationResponse.setStatus(ReservationStatus.CONFIRMED);

        reservationDetail = new ReservationDetailDTO();
        reservationDetail.setId(1);
        reservationDetail.setTableNumber("T001");
        reservationDetail.setStatus(ReservationStatus.CONFIRMED);
    }

    @Test
    void testGetAvailableTables_Success() throws Exception {
        List<TableEntity> tables = Arrays.asList(availableTable);
        when(reservationService.getAvailableTables()).thenReturn(tables);

        mockMvc.perform(get("/api/reservations/available-tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T001"))
                .andExpect(jsonPath("$[0].capacity").value(4));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBookedTables_Success() throws Exception {
        List<TableReservationStatusDTO> tables = Arrays.asList(bookedTable);
        when(reservationService.getBookedTableStatusDTOs()).thenReturn(tables);

        mockMvc.perform(get("/api/reservations/booked-tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].tableNumber").value("T002"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetBookedTables_Forbidden() throws Exception {
        mockMvc.perform(get("/api/reservations/booked-tables"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReservations_Success() throws Exception {
        List<AdminReservationResponseDTO> reservations = Arrays.asList(adminReservationResponse);
        when(reservationService.getAllAdminReservationDTOs()).thenReturn(reservations);

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tableNumber").value("T001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetReservationById_Success() throws Exception {
        when(reservationService.getAdminReservationDTOById(1)).thenReturn(adminReservationResponse);

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T001"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReservation_Success() throws Exception {
        when(reservationService.createReservation(any(ReservationRequestDTO.class), eq("testuser")))
                .thenReturn(customerReservationResponse);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tableNumber").value("T001"))
                .andExpect(jsonPath("$.guestCount").value(4));
    }

    @Test
    void testCreateReservation_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReservation_InvalidData() throws Exception {
        ReservationRequestDTO invalidRequest = new ReservationRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReservation_PastTime() throws Exception {
        ReservationRequestDTO pastTimeRequest = new ReservationRequestDTO();
        pastTimeRequest.setTableId(1);
        pastTimeRequest.setReservationTime(LocalDateTime.now().minusHours(1)); // Past time
        pastTimeRequest.setGuestCount(4);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pastTimeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetReservationsByCurrentUser_Success() throws Exception {
        List<CustomerReservationResponseDTO> reservations = Arrays.asList(customerReservationResponse);
        when(reservationService.getReservationsByUser("testuser")).thenReturn(reservations);

        mockMvc.perform(get("/api/reservations/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser") 
    void testCancelReservation_Success() throws Exception {
        CustomerReservationResponseDTO cancelledReservation = new CustomerReservationResponseDTO();
        cancelledReservation.setId(1);
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);
        
        when(reservationService.cancelReservation(1, "testuser")).thenReturn(cancelledReservation);

        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void testCancelReservation_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateReservationStatus_Success() throws Exception {
        when(reservationService.updateReservationStatusAndReturnDTO(1, ReservationStatus.COMPLETED))
                .thenReturn(reservationDetail);

        mockMvc.perform(put("/api/reservations/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testUpdateReservationStatus_Forbidden() throws Exception {
        mockMvc.perform(put("/api/reservations/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateReservation_Success() throws Exception {
        when(reservationService.updateReservation(eq(1), any(ReservationRequestDTO.class)))
                .thenReturn(reservationDetail);

        mockMvc.perform(put("/api/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteReservation_Success() throws Exception {
        doNothing().when(reservationService).deleteReservation(1);

        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testDeleteReservation_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReservation_TableNotAvailable() throws Exception {
        when(reservationService.createReservation(any(ReservationRequestDTO.class), eq("testuser")))
                .thenThrow(new RuntimeException("Table is not available"));

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReservation_InvalidGuestCount() throws Exception {
        ReservationRequestDTO invalidRequest = new ReservationRequestDTO();
        invalidRequest.setTableId(1);
        invalidRequest.setReservationTime(LocalDateTime.now().plusHours(2));
        invalidRequest.setGuestCount(0); // Invalid guest count

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}