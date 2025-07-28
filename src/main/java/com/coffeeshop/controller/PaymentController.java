package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.response.AdminPaymentResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerPaymentResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;
import com.coffeeshop.dto.admin.request.AdminPaymentStatusUpdateDTO;
import com.coffeeshop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

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
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CustomerPaymentResponseDTO createPayment(@Valid @RequestBody CustomerPaymentRequestDTO request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.createPaymentForCustomer(request);
    }

    /**
     * API lấy danh sách tất cả các thanh toán trong hệ thống.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminPaymentResponseDTO> getAllPayments() {
        return paymentService.getAllAdminPaymentDTOs();
    }

    /**
     * API lấy thông tin chi tiết của một thanh toán dựa vào ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminPaymentResponseDTO getPaymentById(@PathVariable Integer id) {
        return paymentService.getAdminPaymentDTOById(id);
    }

    /**
     * API lấy lịch sử thanh toán của khách hàng hiện tại.
     */
    @GetMapping("/my-payments")
    @PreAuthorize("isAuthenticated()")
    public List<CustomerPaymentResponseDTO> getMyPayments(@AuthenticationPrincipal UserDetails userDetails) {
        return paymentService.getCustomerPaymentDTOsByUsername(userDetails.getUsername());
    }

    /**
     * API lấy lịch sử thanh toán của một khách hàng cụ thể.
     */
    @GetMapping("/by-customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomerPaymentResponseDTO> getPaymentsByCustomer(@PathVariable Integer customerId) {
        return paymentService.getCustomerPaymentDTOsByCustomerId(customerId);
    }

    /**
     * API lấy danh sách các thanh toán của một đơn hàng cụ thể.
     */
    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomerPaymentResponseDTO> getPaymentsByOrder(@PathVariable Integer orderId) {
        return paymentService.getCustomerPaymentDTOsByOrderId(orderId);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminPaymentResponseDTO updatePaymentStatus(@PathVariable Integer id, @Valid @RequestBody AdminPaymentStatusUpdateDTO request) {
        return paymentService.updatePaymentStatusByAdminAndReturnDTO(id, request);
    }
}