package com.coffeeshop.controller.admin;

import com.coffeeshop.dto.admin.response.AdminOrderResponseDTO;
import com.coffeeshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.coffeeshop.enums.OrderStatus;

// Controller ADMIN cho quản lý đơn hàng
@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

    @Autowired
    private OrderService orderService;

    // Lấy toàn bộ đơn hàng (dành cho ADMIN)
    @GetMapping
    public List<AdminOrderResponseDTO> getAllOrdersForAdmin() {
        return orderService.getAllAdminOrderDTOs();
    }

    // Lấy chi tiết 1 đơn hàng (ADMIN)
    @GetMapping("/{id}")
    public AdminOrderResponseDTO getOrderDetailForAdmin(@PathVariable Integer id) {
        return orderService.getAdminOrderDTOById(id);
    }

    // Cập nhật trạng thái đơn hàng (ADMIN)
    @PatchMapping("/{id}/status")
    public AdminOrderResponseDTO updateOrderStatusForAdmin(@PathVariable Integer id, @RequestParam("status") OrderStatus status) {
        // Cập nhật trạng thái, sau đó lấy lại DTO qua service để tránh gọi trực tiếp mapper nội bộ
        orderService.updateOrderStatus(id, status);
        return orderService.getAdminOrderDTOById(id);
    }
}


