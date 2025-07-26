package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.response.AdminPaymentResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerPaymentResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;
import com.coffeeshop.dto.admin.request.AdminPaymentStatusUpdateDTO;
import com.coffeeshop.entity.Payment;
import com.coffeeshop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller quản lý các API liên quan đến Thanh toán (Payment).
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * API để tạo một thanh toán mới cho một đơn hàng.
     * Dựa trên thông tin đơn hàng, phương thức thanh toán và người thực hiện.
     * Sẽ tự động cập nhật trạng thái của Order liên quan nếu thanh toán thành công.
     *
     * @param paymentRequestDTO DTO chứa orderId, paidByUserId, và paymentMethod.
     * @return ResponseEntity chứa PaymentResponseDTO với thông tin chi tiết của payment đã được tạo.
     */
    @PostMapping
    public CustomerPaymentResponseDTO createPayment(@RequestBody CustomerPaymentRequestDTO request) {
        Payment payment = paymentService.createPaymentForCustomer(request);
        return toCustomerPaymentResponseDTO(payment);
    }

    /**
     * API lấy danh sách tất cả các thanh toán trong hệ thống.
     *
     * @return Danh sách các PaymentResponseDTO.
     */
    @GetMapping
    public List<AdminPaymentResponseDTO> getAllPayments() {
        return paymentService.getAllPayments().stream()
                .map(this::toAdminPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * API lấy thông tin chi tiết của một thanh toán dựa vào ID.
     *
     * @param id ID của thanh toán cần tìm.
     * @return ResponseEntity chứa PaymentResponseDTO nếu tìm thấy, ngược lại trả về 404 Not Found.
     */
    @GetMapping("/{id}")
    public AdminPaymentResponseDTO getPaymentById(@PathVariable Integer id) {
        Payment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy payment!"));
        return toAdminPaymentResponseDTO(payment);
    }

    /**
     * API lấy lịch sử thanh toán của một khách hàng cụ thể.
     *
     * @param customerId ID của khách hàng.
     * @return Danh sách các PaymentResponseDTO của khách hàng đó.
     */
    @GetMapping("/by-customer/{customerId}")
    public List<CustomerPaymentResponseDTO> getPaymentsByCustomer(@PathVariable Integer customerId) {
        return paymentService.getPaymentsByCustomerId(customerId).stream()
                .map(this::toCustomerPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * API lấy danh sách các thanh toán của một đơn hàng cụ thể.
     *
     * @param orderId ID của đơn hàng.
     * @return Danh sách các PaymentResponseDTO thuộc đơn hàng đó.
     */
    @GetMapping("/by-order/{orderId}")
    public List<CustomerPaymentResponseDTO> getPaymentsByOrder(@PathVariable Integer orderId) {
        return paymentService.getPaymentsByOrderId(orderId).stream()
                .map(this::toCustomerPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}/status")
    public AdminPaymentResponseDTO updatePaymentStatus(@PathVariable Integer id, @RequestBody AdminPaymentStatusUpdateDTO request) {
        Payment payment = paymentService.updatePaymentStatusByAdmin(id, request);
        return toAdminPaymentResponseDTO(payment);
    }

    // Helper mapping methods
    private AdminPaymentResponseDTO toAdminPaymentResponseDTO(Payment payment) {
        AdminPaymentResponseDTO dto = new AdminPaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        AdminPaymentResponseDTO.CustomerInfo customer = new AdminPaymentResponseDTO.CustomerInfo();
        if (payment.getOrder() != null && payment.getOrder().getCustomer() != null) {
            customer.setId(payment.getOrder().getCustomer().getId());
            customer.setUsername(payment.getOrder().getCustomer().getUsername());
            customer.setFullName(payment.getOrder().getCustomer().getFullName());
            customer.setPhone(payment.getOrder().getCustomer().getPhone());
        }
        dto.setCustomer(customer);
        dto.setAmount(payment.getAmount() != null ? payment.getAmount().doubleValue() : null);
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
    private CustomerPaymentResponseDTO toCustomerPaymentResponseDTO(Payment payment) {
        CustomerPaymentResponseDTO dto = new CustomerPaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        dto.setAmount(payment.getAmount() != null ? payment.getAmount().doubleValue() : null);
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}