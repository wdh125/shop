package com.coffeeshop.controller;

import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public CustomerOrderResponseDTO createOrderWithItems(@Valid @RequestBody CustomerOrderRequestDTO orderRequestDTO,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        return orderService.createOrderWithItems(orderRequestDTO, userDetails.getUsername());
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public List<CustomerOrderResponseDTO> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        return orderService.getCustomerOrdersByUsername(userDetails.getUsername());
    }

    // Các API cho admin chỉ còn xem/truy vấn/trạng thái nếu cần
    // Nếu muốn xóa luôn các API cho admin thì có thể xóa các method dưới đây

    public static class UpdatePaymentMethodRequest {
        private PaymentMethod paymentMethod;
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}