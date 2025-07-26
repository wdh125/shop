package com.coffeeshop.controller;

import com.coffeeshop.entity.Reservation;
import com.coffeeshop.entity.User;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.entity.Order;
import com.coffeeshop.enums.ReservationStatus;
import com.coffeeshop.service.ReservationService;
import com.coffeeshop.service.UserService;
import com.coffeeshop.service.TableService;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.dto.customer.response.ReservationDetailDTO;
import com.coffeeshop.dto.customer.response.TableReservationStatusDTO;
import com.coffeeshop.scheduler.SchedulerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.DayOfWeek;
import java.util.Set;
import com.coffeeshop.dto.admin.response.AdminReservationResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerReservationResponseDTO;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private UserService userService;
    @Autowired
    private TableService tableService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SchedulerConfig schedulerConfig;

    // Danh sách ngày nghỉ (ví dụ: Chủ nhật)
    private static final Set<DayOfWeek> HOLIDAYS = Set.of(DayOfWeek.SUNDAY); // Có thể lấy từ config hoặc DB

    // Customer: Xem danh sách bàn đã bị đặt (cho chọn bàn)
    @GetMapping("/booked-tables")
    public List<TableReservationStatusDTO> getBookedTables() {
        return reservationService.getAllReservations().stream()
            .map(reservation -> {
                TableReservationStatusDTO dto = new TableReservationStatusDTO();
                dto.setTableId(reservation.getTable().getId());
                dto.setTableNumber(reservation.getTable().getTableNumber());
                dto.setReservationDatetime(reservation.getReservationDatetime());
                dto.setStatus(reservation.getStatus().name());
                return dto;
            })
            .collect(Collectors.toList());
    }

    // Admin: Xem tất cả reservation chi tiết
    @GetMapping
    public List<AdminReservationResponseDTO> getAllReservations() {
        return reservationService.getAllReservations().stream()
            .map(this::toAdminReservationResponseDTO)
            .collect(Collectors.toList());
    }

    // Admin: Xem chi tiết 1 reservation
    @GetMapping("/{id}")
    public AdminReservationResponseDTO getReservationById(@PathVariable Integer id) {
        Reservation reservation = reservationService.getReservationById(id)
            .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return toAdminReservationResponseDTO(reservation);
    }

    // Customer: Đặt bàn mới
    @PostMapping
    public CustomerReservationResponseDTO createReservation(@RequestBody Reservation reservation, @AuthenticationPrincipal UserDetails userDetails) {
        // Validate ngày nghỉ
        DayOfWeek day = reservation.getReservationDatetime().getDayOfWeek();
        if (HOLIDAYS.contains(day)) {
            throw new IllegalArgumentException("Không thể đặt bàn vào ngày nghỉ!");
        }
        // Validate giờ hoạt động
        LocalTime opening = LocalTime.parse(schedulerConfig.openingTime);
        LocalTime closing = LocalTime.parse(schedulerConfig.closingTime);
        LocalTime reservationTime = reservation.getReservationDatetime().toLocalTime();
        if (reservationTime.isBefore(opening) || reservationTime.isAfter(closing)) {
            throw new IllegalArgumentException("Chỉ được đặt bàn trong giờ mở cửa: " + opening + " - " + closing);
        }
        // Lấy user từ token
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        TableEntity table = tableService.getTableById(reservation.getTable().getId())
                .orElseThrow(() -> new RuntimeException("Table not found"));
        // Kiểm tra số lượng người không vượt quá sức chứa của bàn
        if (reservation.getPartySize() > table.getCapacity()) {
            throw new IllegalArgumentException("Số lượng người vượt quá sức chứa của bàn này!");
        }
        // Kiểm tra không cho đặt bàn nếu thời gian đặt < min-advance-minutes so với hiện tại
        if (reservation.getReservationDatetime().isBefore(LocalDateTime.now().plusMinutes(schedulerConfig.reservationMinAdvanceMinutes))) {
            throw new IllegalArgumentException("Bạn phải đặt bàn trước ít nhất " + schedulerConfig.reservationMinAdvanceMinutes + " phút!");
        }
        // Kiểm tra trùng lịch đặt bàn nâng cao (1 tiếng đặt + 30 phút đệm)
        LocalDateTime newStart = reservation.getReservationDatetime();
        LocalDateTime newEnd = newStart.plusMinutes(schedulerConfig.reservationDurationMinutes);
        List<Reservation> existing = reservationService.getAllReservations().stream()
            .filter(r -> r.getTable().getId().equals(table.getId()))
            .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
            .filter(r -> {
                LocalDateTime oldStart = r.getReservationDatetime();
                LocalDateTime oldEnd = oldStart.plusMinutes(schedulerConfig.reservationDurationMinutes + schedulerConfig.reservationBufferAfterMinutes);
                return newStart.isBefore(oldEnd) && newEnd.isAfter(oldStart);
            })
            .toList();
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Bàn này đã có người đặt hoặc đang nghỉ giữa ca trong khung giờ này!");
        }
        reservation.setCustomer(user);
        reservation.setTable(table);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationService.saveReservation(reservation);
        return toCustomerReservationResponseDTO(saved);
    }

    // Customer: Xem lịch sử đặt bàn của mình
    @GetMapping("/user/me")
    public List<CustomerReservationResponseDTO> getReservationsByCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        return reservationService.getReservationsByUser(user.getId()).stream()
            .map(this::toCustomerReservationResponseDTO)
            .toList();
    }

    // Customer: Huỷ đặt bàn của mình
    @PutMapping("/{id}/cancel")
    public CustomerReservationResponseDTO cancelReservation(@PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user!"));
        Reservation reservation = reservationService.cancelReservation(id, user.getId());
        return toCustomerReservationResponseDTO(reservation);
    }

    // Admin: Xác nhận/huỷ reservation
    @PutMapping("/{id}/status")
    public ReservationDetailDTO updateReservationStatus(@PathVariable Integer id, @RequestParam ReservationStatus status) {
        Reservation updated = reservationService.updateReservationStatus(id, status);
        return toReservationDetailDTO(updated);
    }

    @PutMapping("/{id}")
    public ReservationDetailDTO updateReservation(@PathVariable Integer id, @RequestBody Reservation reservation) {
        reservation.setId(id);
        Reservation saved = reservationService.saveReservation(reservation);
        return toReservationDetailDTO(saved);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Integer id) {
        reservationService.deleteReservation(id);
    }

    // Mapping entity sang DTO
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

    // Helper mapping methods
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
}