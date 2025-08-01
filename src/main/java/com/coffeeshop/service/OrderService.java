package com.coffeeshop.service;

import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.coffeeshop.entity.*;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.enums.OrderItemStatus;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service chứa các logic nghiệp vụ liên quan đến Order.
 */
@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private TableRepository tableRepository;
    @Autowired private ReservationRepository reservationRepository; // Thêm ReservationRepository
    @Autowired private SettingService settingService;
    @Autowired private NotificationService notificationService;

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order findOrderByReservationId(Integer reservationId) {
        return orderRepository.findByReservation_Id(reservationId);
    }

    /**
     * Tạo một đơn hàng mới cùng với các món trong đơn cho customer.
     */
    @Transactional
    public Order createOrderWithItems(CustomerOrderRequestDTO orderRequestDTO, User user) {
        TableEntity table;
        Reservation reservation = null;

        if (orderRequestDTO.getReservationId() != null) {
            reservation = reservationRepository.findById(orderRequestDTO.getReservationId())
                    .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + orderRequestDTO.getReservationId()));
            if (reservation.getStatus() == com.coffeeshop.enums.ReservationStatus.CANCELLED) {
                throw new IllegalArgumentException("Reservation is cancelled, cannot create order");
            }
            Order existingOrder = orderRepository.findByReservation_Id(reservation.getId());
            if (existingOrder != null) {
                throw new IllegalArgumentException("This reservation already has an order");
            }
            table = reservation.getTable();
        } else if (orderRequestDTO.getTableId() != null) {
            table = tableRepository.findById(orderRequestDTO.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found with id: " + orderRequestDTO.getTableId()));
        } else {
            throw new IllegalArgumentException("TableId or reservationId must be provided.");
        }

        Order order = new Order();
        order.setCustomer(user);
        order.setTable(table);
        if (reservation != null) {
            order.setReservation(reservation);
        }
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setNotes(orderRequestDTO.getNote());
        order.setQrCodePayment("ORDER-QR-" + System.currentTimeMillis());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemDTO itemDTO : orderRequestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemDTO.getProductId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setStatus(OrderItemStatus.ORDERED);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            orderItem.setTotalPrice(itemTotal);
            subtotal = subtotal.add(itemTotal);
            orderItems.add(orderItem);
        }

        BigDecimal taxRate = settingService.getTaxRate();
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        orderItemRepository.saveAll(orderItems);

        // Create notification for order creation
        notificationService.createOrderNotification(
            user, 
            savedOrder, 
            NotificationType.ORDER_CREATED,
            "Đơn hàng mới được tạo",
            "Đơn hàng " + savedOrder.getOrderNumber() + " đã được tạo thành công với tổng tiền " + 
            savedOrder.getTotalAmount() + "đ tại bàn " + savedOrder.getTable().getTableNumber()
        );

        return savedOrder;
    }

    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Create notification for status change
        String statusMessage = getStatusChangeMessage(oldStatus, status);
        if (statusMessage != null) {
            NotificationType notificationType = getNotificationTypeForStatus(status);
            notificationService.createOrderNotification(
                order.getCustomer(),
                savedOrder,
                notificationType,
                "Trạng thái đơn hàng thay đổi",
                "Đơn hàng " + savedOrder.getOrderNumber() + " " + statusMessage
            );
        }

        return savedOrder;
    }

    public Order updatePaymentMethod(Integer orderId, com.coffeeshop.enums.PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setPaymentMethod(paymentMethod);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }

    //<editor-fold desc="Filter Methods">
    public List<Order> filterOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> filterOrdersByTable(Integer tableId) {
        TableEntity table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + tableId));
        return orderRepository.findByTable(table);
    }

    public List<Order> filterOrdersByStatusAndTable(OrderStatus status, TableEntity table) {
        return orderRepository.findByStatusAndTable(status, table);
    }

    public List<Order> filterOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end);
    }

    public List<Order> filterOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByStatusAndCreatedAtBetween(status, start, end);
    }
    //</editor-fold>

    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        return orderItemRepository.findByOrder_Id(orderId);
	}

    public Order saveOrder(Order order) {
        if (order.getId() == null) {
            order.setCreatedAt(java.time.LocalDateTime.now());
        }
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return orderRepository.save(order);
    }
    public OrderItem saveOrderItem(OrderItem item) {
        if (item.getId() == null) {
            item.setCreatedAt(java.time.LocalDateTime.now());
        }
        item.setUpdatedAt(java.time.LocalDateTime.now());
        return orderItemRepository.save(item);
    }

    // --- DTO methods for controller ---
    public CustomerOrderResponseDTO createOrderWithItems(CustomerOrderRequestDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = createOrderWithItems(dto, user);
        return toCustomerOrderResponseDTO(order);
    }

    public List<CustomerOrderResponseDTO> getCustomerOrdersByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::toCustomerOrderResponseDTO)
                .collect(Collectors.toList());
    }

    // --- Mapping methods ---
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
        List<CustomerOrderResponseDTO.OrderItemInfo> items = getOrderItemsByOrderId(order.getId()).stream().map(item -> {
            CustomerOrderResponseDTO.OrderItemInfo oi = new CustomerOrderResponseDTO.OrderItemInfo();
            oi.setId(item.getId());
            oi.setProductName(item.getProduct().getName());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null);
            oi.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null);
            return oi;
        }).collect(Collectors.toList());
        dto.setItems(items);
        return dto;
    }

    // Helper methods for notification
    private String getStatusChangeMessage(OrderStatus oldStatus, OrderStatus newStatus) {
        switch (newStatus) {
            case PREPARING:
                return "đang được chuẩn bị";
            case SERVED:
                return "đã được phục vụ";
            case COMPLETED:
                return "đã hoàn thành";
            case CANCELLED:
                return "đã bị hủy";
            case PAID:
                return "đã được thanh toán";
            default:
                return null;
        }
    }

    private NotificationType getNotificationTypeForStatus(OrderStatus status) {
        switch (status) {
            case CANCELLED:
                return NotificationType.ORDER_CANCELLED;
            case COMPLETED:
                return NotificationType.ORDER_COMPLETED;
            default:
                return NotificationType.ORDER_STATUS_CHANGED;
        }
    }
}