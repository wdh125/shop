package com.coffeeshop.controller;

import com.coffeeshop.dto.*;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.OrderItem;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.service.OrderService;
import com.coffeeshop.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * API tạo một đơn hàng mới cùng với các món trong đơn hàng.
     *
     * @param orderRequestDTO DTO chứa thông tin về bàn, khách hàng và danh sách các món.
     * @return ResponseEntity chứa OrderResponseDTO với thông tin chi tiết của đơn hàng đã tạo.
     */
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrderWithItems(@RequestBody OrderRequestDTO orderRequestDTO) {
        Order order = orderService.createOrderWithItems(orderRequestDTO);
        return new ResponseEntity<>(orderToDTO(order), HttpStatus.CREATED);
    }

    /**
     * API lấy danh sách tất cả các đơn hàng.
     *
     * @return Danh sách các OrderResponseDTO.
     */
    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(this::orderToDTO)
                .collect(Collectors.toList());
    }

    /**
     * API lấy thông tin chi tiết của một đơn hàng dựa vào ID.
     *
     * @param id ID của đơn hàng cần tìm.
     * @return ResponseEntity chứa OrderResponseDTO nếu tìm thấy, ngược lại trả về 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Integer id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(orderToDTO(order));
    }

    /**
     * API cập nhật trạng thái của một đơn hàng.
     *
     * @param id     ID của đơn hàng cần cập nhật.
     * @param status Trạng thái mới của đơn hàng.
     * @return OrderResponseDTO với thông tin đã được cập nhật.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Integer id, @RequestParam OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(orderToDTO(updatedOrder));
    }

    /**
     * API để lọc các đơn hàng theo nhiều tiêu chí.
     *
     * @param status    (tùy chọn) Lọc theo trạng thái đơn hàng.
     * @param tableId   (tùy chọn) Lọc theo ID của bàn.
     * @param startDate (tùy chọn) Lọc theo ngày bắt đầu (format: yyyy-MM-dd'T'HH:mm:ss).
     * @param endDate   (tùy chọn) Lọc theo ngày kết thúc (format: yyyy-MM-dd'T'HH:mm:ss).
     * @return Danh sách các OrderResponseDTO phù hợp với tiêu chí lọc.
     */
    @GetMapping("/filter")
    public List<OrderResponseDTO> filterOrders(
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

        return orders.stream().map(this::orderToDTO).collect(Collectors.toList());
    }

    @PutMapping("/{id}/payment-method")
    public ResponseEntity<OrderResponseDTO> updatePaymentMethod(@PathVariable Integer id, @RequestBody UpdatePaymentMethodRequest request) {
        Order order = orderService.updatePaymentMethod(id, request.getPaymentMethod());
        return ResponseEntity.ok(orderToDTO(order));
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
    private OrderResponseDTO orderToDTO(Order order) {
        // Chuyển đổi thông tin cơ bản của Order
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setQrCodePayment(order.getQrCodePayment());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Mapping reservationId
        dto.setReservationId(order.getReservation() != null ? order.getReservation().getId() : null);

        // Chuyển đổi thông tin Customer
        if (order.getCustomer() != null) {
            OrderResponseDTO.CustomerInfo customerInfo = new OrderResponseDTO.CustomerInfo();
            customerInfo.setId(order.getCustomer().getId());
            customerInfo.setUsername(order.getCustomer().getUsername());
            customerInfo.setFullName(order.getCustomer().getFullName());
            customerInfo.setPhone(order.getCustomer().getPhone());
            dto.setCustomer(customerInfo);
        }

        // Chuyển đổi thông tin Table
        if (order.getTable() != null) {
            OrderResponseDTO.TableInfo tableInfo = new OrderResponseDTO.TableInfo();
            tableInfo.setId(order.getTable().getId());
            tableInfo.setTableNumber(order.getTable().getTableNumber());
            dto.setTable(tableInfo);
        }

        // Chuyển đổi danh sách OrderItem
        List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(order.getId());
        dto.setItems(orderItems.stream().map(this::orderItemToDTO).collect(Collectors.toList()));

        return dto;
    }

    /**
     * Phương thức nội bộ để chuyển đổi từ entity OrderItem sang OrderItemResponseDTO.
     *
     * @param orderItem Entity OrderItem cần chuyển đổi.
     * @return OrderItemResponseDTO đã được mapping thông tin.
     */
    private OrderItemResponseDTO orderItemToDTO(OrderItem orderItem) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setId(orderItem.getId());
        if (orderItem.getProduct() != null) {
            dto.setProductName(orderItem.getProduct().getName());
        }
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setTotalPrice(orderItem.getTotalPrice());
        return dto;
    }
}