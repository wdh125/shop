package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.response.AdminReservationResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerReservationResponseDTO;
import com.coffeeshop.dto.customer.response.ReservationDetailDTO;
import com.coffeeshop.dto.customer.response.TableReservationStatusDTO;
import com.coffeeshop.dto.customer.request.ReservationRequestDTO;
import com.coffeeshop.enums.ReservationStatus;
import com.coffeeshop.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    @GetMapping("/booked-tables")
    public List<TableReservationStatusDTO> getBookedTables() {
        return reservationService.getBookedTableStatusDTOs();
    }

    @GetMapping
    public List<AdminReservationResponseDTO> getAllReservations() {
        return reservationService.getAllAdminReservationDTOs();
    }

    @GetMapping("/{id}")
    public AdminReservationResponseDTO getReservationById(@PathVariable Integer id) {
        return reservationService.getAdminReservationDTOById(id);
    }

    @PostMapping
    public CustomerReservationResponseDTO createReservation(@Valid @RequestBody ReservationRequestDTO request,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return reservationService.createReservation(request, userDetails.getUsername());
    }

    @GetMapping("/user/me")
    public List<CustomerReservationResponseDTO> getReservationsByCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return reservationService.getReservationsByUser(userDetails.getUsername());
    }

    @PutMapping("/{id}/cancel")
    public CustomerReservationResponseDTO cancelReservation(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        return reservationService.cancelReservation(id, userDetails.getUsername());
    }

    @PutMapping("/{id}/status")
    public ReservationDetailDTO updateReservationStatus(@PathVariable Integer id, @RequestParam ReservationStatus status) {
        return reservationService.updateReservationStatusAndReturnDTO(id, status);
    }

    @PutMapping("/{id}")
    public ReservationDetailDTO updateReservation(@PathVariable Integer id, @Valid @RequestBody ReservationRequestDTO request) {
        return reservationService.updateReservation(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Integer id) {
        reservationService.deleteReservation(id);
    }
}