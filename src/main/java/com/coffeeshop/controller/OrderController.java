package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.response.AdminOrderResponseDTO;
import com.coffeeshop.dto.admin.request.AdminOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public CustomerOrderResponseDTO createOrderWithItems(@Valid @RequestBody CustomerOrderRequestDTO orderRequestDTO,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        return orderService.createOrderWithItems(orderRequestDTO, userDetails.getUsername());
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO createOrderWithItemsAdmin(@Valid @RequestBody AdminOrderRequestDTO orderRequestDTO) {
        return orderService.createOrderWithItemsAdmin(orderRequestDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminOrderResponseDTO> getAllOrders() {
        return orderService.getAllAdminOrderDTOs();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO getOrderById(@PathVariable Integer id) {
        return orderService.getAdminOrderDTOById(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO updateOrderStatus(@PathVariable Integer id, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatusAndReturnDTO(id, status);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminOrderResponseDTO> filterOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Integer tableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return orderService.filterOrders(status, tableId, startDate, endDate);
    }

    @PutMapping("/{id}/payment-method")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO updatePaymentMethod(@PathVariable Integer id, @RequestBody UpdatePaymentMethodRequest request) {
        return orderService.updatePaymentMethodAndReturnDTO(id, request.getPaymentMethod());
    }

    public static class UpdatePaymentMethodRequest {
        private PaymentMethod paymentMethod;
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}