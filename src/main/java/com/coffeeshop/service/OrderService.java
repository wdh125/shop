package com.coffeeshop.service;

import com.coffeeshop.dto.admin.request.OrderRequestDTO;
import com.coffeeshop.entity.*;
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
     * Tạo một đơn hàng mới cùng với các món trong đơn.
     * Đây là một giao dịch, đảm bảo tính toàn vẹn dữ liệu.
     *
     * @param orderRequestDTO DTO từ client chứa thông tin tạo đơn hàng.
     * @return Entity Order sau khi đã được lưu thành công.
     */
    @Transactional
    public Order createOrderWithItems(OrderRequestDTO orderRequestDTO) {
        User user;
        TableEntity table;
        Reservation reservation = null;

        // Step 1: Xác định thông tin User và Table
        // Ưu tiên lấy thông tin từ Reservation nếu có
        if (orderRequestDTO.getReservationId() != null) {
            reservation = reservationRepository.findById(orderRequestDTO.getReservationId())
                    .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + orderRequestDTO.getReservationId()));
            // Bổ sung kiểm tra trạng thái
            if (reservation.getStatus() == com.coffeeshop.enums.ReservationStatus.CANCELLED) {
                throw new IllegalArgumentException("Reservation is cancelled, cannot create order");
            }
            // Bổ sung kiểm tra reservation đã có order chưa
            Order existingOrder = orderRepository.findByReservation_Id(reservation.getId());
            if (existingOrder != null) {
                throw new IllegalArgumentException("This reservation already has an order");
            }
            user = reservation.getCustomer();
            table = reservation.getTable();
        } else if (orderRequestDTO.getUserId() != null && orderRequestDTO.getTableId() != null) {
            // Trường hợp tạo đơn hàng trực tiếp
            user = userRepository.findById(orderRequestDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + orderRequestDTO.getUserId()));
            table = tableRepository.findById(orderRequestDTO.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found with id: " + orderRequestDTO.getTableId()));
        } else {
            throw new IllegalArgumentException("Either reservationId or both userId and tableId must be provided.");
        }

        // Step 2: Tạo đối tượng Order ban đầu
        Order order = new Order();
        order.setCustomer(user);
        order.setTable(table);
        if (reservation != null) {
            order.setReservation(reservation); // Liên kết Order với Reservation
        }
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setNotes(orderRequestDTO.getNote());
        order.setQrCodePayment("ORDER-QR-" + System.currentTimeMillis());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Step 3: Tạo danh sách các OrderItem từ DTO
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderRequestDTO.OrderItemDTO itemDTO : orderRequestDTO.getItems()) {
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

        // Step 4: Tính toán thuế và tổng tiền cuối cùng
        BigDecimal taxRate = settingService.getTaxRate();
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        
        // Step 5: Lưu Order và sau đó là các OrderItem
        Order savedOrder = orderRepository.save(order);
        
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        orderItemRepository.saveAll(orderItems);

        return savedOrder;
    }

    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
		return orderRepository.save(order);
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
}