package com.coffeeshop.controller;

import com.coffeeshop.dto.admin.response.AdminOrderResponseDTO;
import com.coffeeshop.dto.admin.request.AdminOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.service.TableService;
import com.coffeeshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller quản lý các API liên quan đến Đơn hàng (Order).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TableService tableService;

    @Autowired
    private UserService userService;

    /**
     * API tạo một đơn hàng mới cùng với các món trong đơn hàng (Customer).
     *
     * @param orderRequestDTO DTO chứa thông tin về bàn và danh sách các món.
     * @return CustomerOrderResponseDTO với thông tin chi tiết của đơn hàng đã tạo.
     */
    @PostMapping
    public CustomerOrderResponseDTO createOrderWithItems(@RequestBody CustomerOrderRequestDTO orderRequestDTO, 
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        // Lấy user từ authentication context
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Convert CustomerOrderRequestDTO to AdminOrderRequestDTO
        AdminOrderRequestDTO adminRequest = new AdminOrderRequestDTO();
        adminRequest.setTableId(orderRequestDTO.getTableId());
        adminRequest.setUserId(user.getId()); // Set userId từ authentication context
        adminRequest.setReservationId(orderRequestDTO.getReservationId());
        adminRequest.setNote(orderRequestDTO.getNote());
        adminRequest.setItems(orderRequestDTO.getItems());
        
        Order order = orderService.createOrderWithItems(adminRequest);
        return toCustomerOrderResponseDTO(order);
    }

    /**
     * API tạo một đơn hàng mới cùng với các món trong đơn hàng (Admin).
     *
     * @param orderRequestDTO DTO chứa thông tin về bàn, khách hàng và danh sách các món.
     * @return AdminOrderResponseDTO với thông tin chi tiết của đơn hàng đã tạo.
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO createOrderWithItemsAdmin(@RequestBody AdminOrderRequestDTO orderRequestDTO) {
        Order order = orderService.createOrderWithItems(orderRequestDTO);
        return toAdminOrderResponseDTO(order);
    }

    /**
     * API lấy danh sách tất cả các đơn hàng (Admin).
     *
     * @return Danh sách các AdminOrderResponseDTO.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminOrderResponseDTO> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(this::toAdminOrderResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * API lấy thông tin chi tiết của một đơn hàng dựa vào ID (Admin).
     *
     * @param id ID của đơn hàng cần tìm.
     * @return AdminOrderResponseDTO nếu tìm thấy, ngược lại trả về 404 Not Found.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO getOrderById(@PathVariable Integer id) {
        Order order = orderService.getOrderById(id);
        return toAdminOrderResponseDTO(order);
    }

    /**
     * API cập nhật trạng thái của một đơn hàng (Admin).
     *
     * @param id     ID của đơn hàng cần cập nhật.
     * @param status Trạng thái mới của đơn hàng.
     * @return AdminOrderResponseDTO với thông tin đã được cập nhật.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO updateOrderStatus(@PathVariable Integer id, @RequestParam OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return toAdminOrderResponseDTO(updatedOrder);
    }

    /**
     * API để lọc các đơn hàng theo nhiều tiêu chí (Admin).
     *
     * @param status    (tùy chọn) Lọc theo trạng thái đơn hàng.
     * @param tableId   (tùy chọn) Lọc theo ID của bàn.
     * @param startDate (tùy chọn) Lọc theo ngày bắt đầu (format: yyyy-MM-dd'T'HH:mm:ss).
     * @param endDate   (tùy chọn) Lọc theo ngày kết thúc (format: yyyy-MM-dd'T'HH:mm:ss).
     * @return Danh sách các AdminOrderResponseDTO phù hợp với tiêu chí lọc.
     */
    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminOrderResponseDTO> filterOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Integer tableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Order> orders;

        if (status != null && tableId != null) {
            TableEntity table = tableService.getTableById(tableId)
                    .orElseThrow(() -> new RuntimeException("Table not found"));
            orders = orderService.filterOrdersByStatusAndTable(status, table);
        } else if (status != null && startDate != null && endDate != null) {
            orders = orderService.filterOrdersByStatusAndDateRange(status, startDate, endDate);
        } else if (status != null) {
            orders = orderService.filterOrdersByStatus(status);
        } else if (tableId != null) {
            orders = orderService.filterOrdersByTable(tableId);
        } else if (startDate != null && endDate != null) {
            orders = orderService.filterOrdersByDateRange(startDate, endDate);
        } else {
            orders = orderService.getAllOrders();
        }

        return orders.stream()
                .map(this::toAdminOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/payment-method")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminOrderResponseDTO updatePaymentMethod(@PathVariable Integer id, @RequestBody UpdatePaymentMethodRequest request) {
        Order order = orderService.updatePaymentMethod(id, request.getPaymentMethod());
        return toAdminOrderResponseDTO(order);
    }

    public static class UpdatePaymentMethodRequest {
        private com.coffeeshop.enums.PaymentMethod paymentMethod;
        public com.coffeeshop.enums.PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(com.coffeeshop.enums.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    /**
     * Phương thức nội bộ để chuyển đổi từ entity Order sang OrderResponseDTO.
     *
     * @param order Entity Order cần chuyển đổi.
     * @return OrderResponseDTO đã được mapping thông tin.
     */
    private AdminOrderResponseDTO toAdminOrderResponseDTO(Order order) {
        AdminOrderResponseDTO dto = new AdminOrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        // Customer info
        AdminOrderResponseDTO.CustomerInfo customer = new AdminOrderResponseDTO.CustomerInfo();
        customer.setId(order.getCustomer().getId());
        customer.setUsername(order.getCustomer().getUsername());
        customer.setFullName(order.getCustomer().getFullName());
        customer.setPhone(order.getCustomer().getPhone());
        dto.setCustomer(customer);
        // Table info
        AdminOrderResponseDTO.TableInfo table = new AdminOrderResponseDTO.TableInfo();
        table.setId(order.getTable().getId());
        table.setTableNumber(order.getTable().getTableNumber());
        table.setLocation(order.getTable().getLocation());
        dto.setTable(table);
        dto.setReservationId(order.getReservation() != null ? order.getReservation().getId() : null);
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        dto.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        // Items
        List<AdminOrderResponseDTO.OrderItemInfo> items = orderService.getOrderItemsByOrderId(order.getId()).stream().map(item -> {
            AdminOrderResponseDTO.OrderItemInfo oi = new AdminOrderResponseDTO.OrderItemInfo();
            oi.setId(item.getId());
            oi.setProductName(item.getProduct().getName());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null);
            oi.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null);
            return oi;
        }).toList();
        dto.setItems(items);
        return dto;
    }
    private CustomerOrderResponseDTO toCustomerOrderResponseDTO(Order order) {
        CustomerOrderResponseDTO dto = new CustomerOrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        // Table info
        CustomerOrderResponseDTO.TableInfo table = new CustomerOrderResponseDTO.TableInfo();
        table.setId(order.getTable().getId());
        table.setTableNumber(order.getTable().getTableNumber());
        table.setLocation(order.getTable().getLocation());
        dto.setTable(table);
        dto.setReservationId(order.getReservation() != null ? order.getReservation().getId() : null);
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        // Items
        List<CustomerOrderResponseDTO.OrderItemInfo> items = orderService.getOrderItemsByOrderId(order.getId()).stream().map(item -> {
            CustomerOrderResponseDTO.OrderItemInfo oi = new CustomerOrderResponseDTO.OrderItemInfo();
            oi.setId(item.getId());
            oi.setProductName(item.getProduct().getName());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null);
            oi.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null);
            return oi;
        }).toList();
        dto.setItems(items);
        return dto;
    }
}