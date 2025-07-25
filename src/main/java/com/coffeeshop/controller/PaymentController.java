package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.request.PaymentRequestDTO;
import com.coffeeshop.dto.admin.response.PaymentResponseDTO;
import com.coffeeshop.entity.Payment;
import com.coffeeshop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO paymentRequestDTO) {
        Payment payment = paymentService.createPayment(paymentRequestDTO);
        return new ResponseEntity<>(paymentToDTO(payment), HttpStatus.CREATED);
    }

    /**
     * API lấy danh sách tất cả các thanh toán trong hệ thống.
     *
     * @return Danh sách các PaymentResponseDTO.
     */
    @GetMapping
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentService.getAllPayments().stream()
                .map(this::paymentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * API lấy thông tin chi tiết của một thanh toán dựa vào ID.
     *
     * @param id ID của thanh toán cần tìm.
     * @return ResponseEntity chứa PaymentResponseDTO nếu tìm thấy, ngược lại trả về 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Integer id) {
        return paymentService.getPaymentById(id)
                .map(payment -> ResponseEntity.ok(paymentToDTO(payment)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * API lấy lịch sử thanh toán của một khách hàng cụ thể.
     *
     * @param customerId ID của khách hàng.
     * @return Danh sách các PaymentResponseDTO của khách hàng đó.
     */
    @GetMapping("/by-customer/{customerId}")
    public List<PaymentResponseDTO> getPaymentsByCustomer(@PathVariable Integer customerId) {
        return paymentService.getPaymentsByCustomerId(customerId).stream()
                .map(this::paymentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * API lấy danh sách các thanh toán của một đơn hàng cụ thể.
     *
     * @param orderId ID của đơn hàng.
     * @return Danh sách các PaymentResponseDTO thuộc đơn hàng đó.
     */
    @GetMapping("/by-order/{orderId}")
    public List<PaymentResponseDTO> getPaymentsByOrder(@PathVariable Integer orderId) {
        return paymentService.getPaymentsByOrderId(orderId).stream()
                .map(this::paymentToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Phương thức nội bộ để chuyển đổi từ entity Payment sang PaymentResponseDTO.
     * Việc này giúp che giấu các chi tiết không cần thiết của entity và cấu trúc lại dữ liệu
     * cho phù hợp với phía client.
     *
     * @param payment Entity Payment cần chuyển đổi.
     * @return PaymentResponseDTO đã được mapping thông tin.
     */
    private PaymentResponseDTO paymentToDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());

        if (payment.getOrder() != null && payment.getOrder().getOrderNumber() != null) {
            try {
                // An toàn hơn khi parse, chỉ lấy các ký tự số từ orderNumber
                String numericOrderNumber = payment.getOrder().getOrderNumber().replaceAll("[^0-9]", "");
                if (!numericOrderNumber.isEmpty()) {
                    dto.setOrderNumber(Long.parseLong(numericOrderNumber));
                }
            } catch (NumberFormatException e) {
                // Nếu có lỗi xảy ra, ghi log và bỏ qua, orderNumber sẽ là null
                System.err.println("Could not parse order number: " + payment.getOrder().getOrderNumber());
                dto.setOrderNumber(null);
            }
        }

        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setStatus(payment.getStatus().name());
        dto.setCreatedAt(payment.getCreatedAt());

        if (payment.getProcessedBy() != null) {
            PaymentResponseDTO.UserInfo userInfo = new PaymentResponseDTO.UserInfo();
            userInfo.setId(payment.getProcessedBy().getId());
            userInfo.setUsername(payment.getProcessedBy().getUsername());
            userInfo.setFullName(payment.getProcessedBy().getFullName());
            dto.setProcessedBy(userInfo);
        }

        return dto;
    }
}