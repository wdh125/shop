package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Reservation;
import com.coffeeshop.repository.ReservationRepository;
import com.coffeeshop.enums.ReservationStatus;

@Service
public class ReservationService {
	@Autowired
	private ReservationRepository reservationRepository;

	public List<Reservation> getAllReservations() {
		return reservationRepository.findAll();
	}

	public Optional<Reservation> getReservationById(Integer id) {
		return reservationRepository.findById(id);
	}

	public Reservation saveReservation(Reservation reservation) {
		if (reservation.getId() == null) {
			reservation.setCreatedAt(java.time.LocalDateTime.now());
		}
		reservation.setUpdatedAt(java.time.LocalDateTime.now());
		return reservationRepository.save(reservation);
	}

	public void deleteReservation(Integer id) {
		reservationRepository.deleteById(id);
	}

	public List<Reservation> getReservationsByUser(Integer userId) {
		return reservationRepository.findByCustomer_Id(userId);
	}

	public Reservation cancelReservation(Integer reservationId, Integer userId) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new RuntimeException("Reservation not found"));
		if (!reservation.getCustomer().getId().equals(userId)) {
			throw new RuntimeException("Not allowed to cancel this reservation");
		}
		// Không cho hủy nếu còn dưới 30 phút so với thời gian đặt bàn
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(reservation.getReservationDatetime().minusMinutes(30))) {
			throw new IllegalArgumentException("Cannot cancel reservation within 30 minutes before reservation time.");
		}
		reservation.setStatus(ReservationStatus.CANCELLED);
		reservation.setUpdatedAt(java.time.LocalDateTime.now());
		return reservationRepository.save(reservation);
	}

	public Reservation updateReservationStatus(Integer reservationId, ReservationStatus status) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new RuntimeException("Reservation not found"));
		reservation.setStatus(status);
		reservation.setUpdatedAt(java.time.LocalDateTime.now());
		return reservationRepository.save(reservation);
	}
}