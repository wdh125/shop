package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Reservation;
import com.coffeeshop.entity.User;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.entity.Order;
import com.coffeeshop.repository.ReservationRepository;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.enums.ReservationStatus;
import com.coffeeshop.dto.admin.response.AdminReservationResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerReservationResponseDTO;
import com.coffeeshop.dto.customer.response.ReservationDetailDTO;
import com.coffeeshop.dto.customer.response.TableReservationStatusDTO;
import com.coffeeshop.dto.customer.request.ReservationRequestDTO;
import com.coffeeshop.scheduler.SchedulerConfig;
import com.coffeeshop.enums.TableStatus;

@Service
public class ReservationService {
	@Autowired
	private ReservationRepository reservationRepository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TableService tableService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SchedulerConfig schedulerConfig;

	@Autowired
	private NotificationService notificationService;

	// Danh sách ngày nghỉ (có thể lấy từ config hoặc DB)
	private static final Set<DayOfWeek> HOLIDAYS = Set.of(DayOfWeek.SUNDAY);

	// ===== Các method cũ (giữ nguyên) =====
	public List<Reservation> getAllReservations() {
		return reservationRepository.findAll();
	}

	public Optional<Reservation> getReservationById(Integer id) {
		return reservationRepository.findById(id);
	}

	public Reservation saveReservation(Reservation reservation) {
		boolean isNew = reservation.getId() == null;
		
		if (isNew) {
			reservation.setCreatedAt(java.time.LocalDateTime.now());
		}
		reservation.setUpdatedAt(java.time.LocalDateTime.now());
		Reservation savedReservation = reservationRepository.save(reservation);

		// Create notification for new reservation
		if (isNew) {
			notificationService.createReservationNotification(
				reservation.getCustomer(),
				savedReservation,
				NotificationType.RESERVATION_CONFIRMED,
				"Đặt bàn mới được tạo",
				"Đặt bàn của bạn tại bàn " + reservation.getTable().getTableNumber() + 
				" vào lúc " + reservation.getReservationDatetime() + " đã được tạo thành công"
			);
		}

		return savedReservation;
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
		Reservation savedReservation = reservationRepository.save(reservation);

		// Create notification for reservation cancellation
		notificationService.createReservationNotification(
			reservation.getCustomer(),
			savedReservation,
			NotificationType.RESERVATION_CANCELLED,
			"Đặt bàn đã bị hủy",
			"Đặt bàn của bạn tại bàn " + reservation.getTable().getTableNumber() + 
			" vào lúc " + reservation.getReservationDatetime() + " đã được hủy thành công"
		);

		return savedReservation;
	}

	public Reservation updateReservationStatus(Integer reservationId, ReservationStatus status) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new RuntimeException("Reservation not found"));
		ReservationStatus oldStatus = reservation.getStatus();
		reservation.setStatus(status);
		reservation.setUpdatedAt(java.time.LocalDateTime.now());
		Reservation savedReservation = reservationRepository.save(reservation);

		// Create notification for status change
		if (status == ReservationStatus.CONFIRMED && oldStatus != ReservationStatus.CONFIRMED) {
			notificationService.createReservationNotification(
				reservation.getCustomer(),
				savedReservation,
				NotificationType.RESERVATION_CONFIRMED,
				"Đặt bàn đã được xác nhận",
				"Đặt bàn của bạn tại bàn " + reservation.getTable().getTableNumber() + 
				" vào lúc " + reservation.getReservationDatetime() + " đã được xác nhận"
			);
		}

		return savedReservation;
	}

	// ===== Các method mới cho DTO mapping =====
	
	public List<TableReservationStatusDTO> getBookedTableStatusDTOs() {
		LocalDateTime now = LocalDateTime.now();
		return getAllReservations().stream()
			.filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
			.filter(r -> r.getReservationDatetime().isAfter(now)) // Chỉ hiển thị reservation trong tương lai
			.map(this::toTableReservationStatusDTO)
			.toList();
	}

	// Method để lấy danh sách bàn trống (cho customer chọn)
	public List<TableEntity> getAvailableTables() {
		LocalDateTime now = LocalDateTime.now();
		List<Integer> bookedTableIds = getAllReservations().stream()
			.filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
			.filter(r -> r.getReservationDatetime().isAfter(now))
			.map(r -> r.getTable().getId())
			.toList();
		
		return tableService.getAllTables().stream()
			.filter(table -> !bookedTableIds.contains(table.getId()))
			.filter(table -> table.getStatus() == TableStatus.AVAILABLE)
			.toList();
	}

	public List<AdminReservationResponseDTO> getAllAdminReservationDTOs() {
		return getAllReservations().stream()
			.map(this::toAdminReservationResponseDTO)
			.toList();
	}

	public AdminReservationResponseDTO getAdminReservationDTOById(Integer id) {
		Reservation reservation = getReservationById(id)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));
		return toAdminReservationResponseDTO(reservation);
	}

	public CustomerReservationResponseDTO createReservation(ReservationRequestDTO request, String username) {
		// Validate ngày nghỉ
		DayOfWeek day = request.getReservationDatetime().getDayOfWeek();
		if (HOLIDAYS.contains(day)) {
			throw new IllegalArgumentException("Không thể đặt bàn vào ngày nghỉ (Chủ nhật)!");
		}
		
		// Validate giờ hoạt động với thời gian chuẩn bị và phục vụ
		LocalTime opening = LocalTime.parse(schedulerConfig.openingTime); // 08:00
		LocalTime closing = LocalTime.parse(schedulerConfig.closingTime); // 22:00
		LocalTime reservationTime = request.getReservationDatetime().toLocalTime();
		
		// Thời gian chuẩn bị: 1 tiếng trước giờ mở cửa để chuẩn bị
		LocalTime effectiveOpening = opening.plusMinutes(60); // 09:00 - bắt đầu nhận đặt bàn
		
		// Thời gian kết thúc đặt bàn: 2 tiếng trước giờ đóng cửa để đảm bảo đủ thời gian phục vụ
		// (60 phút phục vụ + 30 phút dọn dẹp + 30 phút buffer)
		LocalTime effectiveClosing = closing.minusMinutes(120); // 20:00 - kết thúc nhận đặt bàn
		
		if (reservationTime.isBefore(effectiveOpening) || reservationTime.isAfter(effectiveClosing)) {
			throw new IllegalArgumentException(
				"Chỉ được đặt bàn trong giờ làm việc: " + effectiveOpening + " - " + effectiveClosing + 
				" (Giờ mở cửa: " + opening + " - " + closing + ")"
			);
		}
		
		// Lấy user từ username
		User user = userService.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
		
		// Lấy table
		TableEntity table = tableService.getTableById(request.getTableId())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn với ID: " + request.getTableId()));
		
		// Kiểm tra số lượng người không vượt quá sức chứa của bàn
		if (request.getPartySize() > table.getCapacity()) {
			throw new IllegalArgumentException(
				"Số lượng người (" + request.getPartySize() + ") vượt quá sức chứa của bàn này (" + table.getCapacity() + " người)!"
			);
		}
		
		// Kiểm tra không cho đặt bàn nếu thời gian đặt < min-advance-minutes so với hiện tại
		if (request.getReservationDatetime().isBefore(LocalDateTime.now().plusMinutes(schedulerConfig.reservationMinAdvanceMinutes))) {
			throw new IllegalArgumentException("Bạn phải đặt bàn trước ít nhất " + schedulerConfig.reservationMinAdvanceMinutes + " phút!");
		}
		
		// Kiểm tra trùng lịch đặt bàn nâng cao với thời gian nghỉ giữa ca
		LocalDateTime newStart = request.getReservationDatetime();
		LocalDateTime newEnd = newStart.plusMinutes(schedulerConfig.reservationDurationMinutes); // 90 phút phục vụ
		
		List<Reservation> existing = getAllReservations().stream()
			.filter(r -> r.getTable().getId().equals(table.getId()))
			.filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
			.filter(r -> {
				LocalDateTime oldStart = r.getReservationDatetime();
				LocalDateTime oldEnd = oldStart.plusMinutes(schedulerConfig.reservationDurationMinutes + schedulerConfig.reservationBufferAfterMinutes);
				// Kiểm tra xung đột: ca mới bắt đầu trước khi ca cũ kết thúc hoàn toàn
				return newStart.isBefore(oldEnd) && newEnd.isAfter(oldStart);
			})
			.toList();
		if (!existing.isEmpty()) {
			throw new IllegalArgumentException(
				"Bàn này đã có người đặt trong khung giờ này! " +
				"Thời gian phục vụ: " + schedulerConfig.reservationDurationMinutes + " phút, " +
				"Thời gian nghỉ giữa ca: " + schedulerConfig.reservationBufferAfterMinutes + " phút"
			);
		}
		
		// Tạo reservation entity
		Reservation reservation = new Reservation();
		reservation.setCustomer(user);
		reservation.setTable(table);
		reservation.setReservationDatetime(request.getReservationDatetime());
		reservation.setPartySize(request.getPartySize());
		reservation.setNotes(request.getNotes());
		reservation.setStatus(ReservationStatus.PENDING);
		reservation.setCreatedAt(LocalDateTime.now());
		reservation.setUpdatedAt(LocalDateTime.now());
		
		Reservation saved = saveReservation(reservation);
		return toCustomerReservationResponseDTO(saved);
	}

	public List<CustomerReservationResponseDTO> getReservationsByUser(String username) {
		User user = userService.findByUsername(username)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
		return getReservationsByUser(user.getId()).stream()
			.map(this::toCustomerReservationResponseDTO)
			.toList();
	}

	public CustomerReservationResponseDTO cancelReservation(Integer id, String username) {
		User user = userService.findByUsername(username)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
		Reservation reservation = cancelReservation(id, user.getId());
		return toCustomerReservationResponseDTO(reservation);
	}

	public ReservationDetailDTO updateReservationStatusAndReturnDTO(Integer id, ReservationStatus status) {
		Reservation updated = updateReservationStatus(id, status);
		return toReservationDetailDTO(updated);
	}

	public ReservationDetailDTO updateReservation(Integer id, ReservationRequestDTO request) {
		Reservation existing = getReservationById(id)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));
		
		// Cập nhật thông tin
		existing.setReservationDatetime(request.getReservationDatetime());
		existing.setPartySize(request.getPartySize());
		existing.setNotes(request.getNotes());
		existing.setUpdatedAt(LocalDateTime.now());
		
		Reservation saved = saveReservation(existing);
		return toReservationDetailDTO(saved);
	}

	// ===== Helper methods cho DTO mapping =====
	
	private TableReservationStatusDTO toTableReservationStatusDTO(Reservation reservation) {
		TableReservationStatusDTO dto = new TableReservationStatusDTO();
		dto.setTableId(reservation.getTable().getId());
		dto.setTableNumber(reservation.getTable().getTableNumber());
		dto.setReservationDatetime(reservation.getReservationDatetime());
		dto.setStatus(reservation.getStatus().name());
		return dto;
	}

	private AdminReservationResponseDTO toAdminReservationResponseDTO(Reservation reservation) {
		AdminReservationResponseDTO dto = new AdminReservationResponseDTO();
		dto.setId(reservation.getId());
		
		// Customer info
		AdminReservationResponseDTO.CustomerInfo customer = new AdminReservationResponseDTO.CustomerInfo();
		customer.setId(reservation.getCustomer().getId());
		customer.setUsername(reservation.getCustomer().getUsername());
		customer.setFullName(reservation.getCustomer().getFullName());
		customer.setPhone(reservation.getCustomer().getPhone());
		dto.setCustomer(customer);
		
		// Table info
		AdminReservationResponseDTO.TableInfo table = new AdminReservationResponseDTO.TableInfo();
		table.setId(reservation.getTable().getId());
		table.setTableNumber(reservation.getTable().getTableNumber());
		table.setLocation(reservation.getTable().getLocation());
		dto.setTable(table);
		
		dto.setReservationDatetime(reservation.getReservationDatetime());
		dto.setPartySize(reservation.getPartySize());
		dto.setStatus(reservation.getStatus().name());
		dto.setNotes(reservation.getNotes());
		dto.setCreatedAt(reservation.getCreatedAt());
		dto.setUpdatedAt(reservation.getUpdatedAt());
		return dto;
	}

	private CustomerReservationResponseDTO toCustomerReservationResponseDTO(Reservation reservation) {
		CustomerReservationResponseDTO dto = new CustomerReservationResponseDTO();
		dto.setId(reservation.getId());
		
		// Table info
		CustomerReservationResponseDTO.TableInfo table = new CustomerReservationResponseDTO.TableInfo();
		table.setId(reservation.getTable().getId());
		table.setTableNumber(reservation.getTable().getTableNumber());
		table.setLocation(reservation.getTable().getLocation());
		dto.setTable(table);
		
		dto.setReservationDatetime(reservation.getReservationDatetime());
		dto.setPartySize(reservation.getPartySize());
		dto.setStatus(reservation.getStatus().name());
		dto.setNotes(reservation.getNotes());
		dto.setCreatedAt(reservation.getCreatedAt());
		dto.setUpdatedAt(reservation.getUpdatedAt());
		return dto;
	}

	private ReservationDetailDTO toReservationDetailDTO(Reservation reservation) {
		ReservationDetailDTO dto = new ReservationDetailDTO();
		dto.setId(reservation.getId());
		
		// Customer
		ReservationDetailDTO.CustomerInfo customerInfo = new ReservationDetailDTO.CustomerInfo();
		customerInfo.setId(reservation.getCustomer().getId());
		customerInfo.setUsername(reservation.getCustomer().getUsername());
		customerInfo.setFullName(reservation.getCustomer().getFullName());
		customerInfo.setPhone(reservation.getCustomer().getPhone());
		dto.setCustomer(customerInfo);
		
		// Table
		ReservationDetailDTO.TableInfo tableInfo = new ReservationDetailDTO.TableInfo();
		tableInfo.setId(reservation.getTable().getId());
		tableInfo.setTableNumber(reservation.getTable().getTableNumber());
		tableInfo.setLocation(reservation.getTable().getLocation());
		dto.setTable(tableInfo);
		
		// Thông tin khác
		dto.setReservationDatetime(reservation.getReservationDatetime());
		dto.setPartySize(reservation.getPartySize());
		dto.setStatus(reservation.getStatus().name());
		dto.setNotes(reservation.getNotes());
		dto.setCreatedAt(reservation.getCreatedAt());
		dto.setUpdatedAt(reservation.getUpdatedAt());
		
		// Mapping order nếu có
		Order order = orderService.findOrderByReservationId(reservation.getId());
		if (order != null) {
			ReservationDetailDTO.OrderSummaryDTO orderDTO = new ReservationDetailDTO.OrderSummaryDTO();
			orderDTO.setId(order.getId());
			orderDTO.setOrderNumber(order.getOrderNumber());
			orderDTO.setStatus(order.getStatus().name());
			orderDTO.setPaymentStatus(order.getPaymentStatus().name());
			orderDTO.setTotalAmount(order.getTotalAmount());
			dto.setOrder(orderDTO);
		} else {
			dto.setOrder(null);
		}
		return dto;
	}
}