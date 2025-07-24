package com.coffeeshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coffeeshop.entity.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByCustomer_Id(Integer customerId);
    List<Reservation> findByStatus(com.coffeeshop.enums.ReservationStatus status);
}