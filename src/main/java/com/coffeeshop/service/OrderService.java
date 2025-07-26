package com.coffeeshop.service;

import com.coffeeshop.dto.admin.request.AdminOrderRequestDTO;
import com.coffeeshop.dto.admin.response.AdminOrderResponseDTO;
import com.coffeeshop.dto.customer.request.CustomerOrderRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerOrderResponseDTO;
import com.coffeeshop.dto.shared.OrderItemDTO;
import com.coffeeshop.entity.*;
import com.coffeeshop.enums.OrderItemStatus;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentMethod;
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
    public Order createOrderWithItems(AdminOrderRequestDTO orderRequestDTO) {
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

    // --- DTO methods for controller ---
    public CustomerOrderResponseDTO createOrderWithItems(CustomerOrderRequestDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AdminOrderRequestDTO adminRequest = new AdminOrderRequestDTO();
        adminRequest.setTableId(dto.getTableId());
        adminRequest.setUserId(user.getId());
        adminRequest.setReservationId(dto.getReservationId());
        adminRequest.setNote(dto.getNote());
        adminRequest.setItems(dto.getItems());
        Order order = createOrderWithItems(adminRequest);
        return toCustomerOrderResponseDTO(order);
    }

    public AdminOrderResponseDTO createOrderWithItemsAdmin(AdminOrderRequestDTO dto) {
        Order order = createOrderWithItems(dto);
        return toAdminOrderResponseDTO(order);
    }

    public List<AdminOrderResponseDTO> getAllAdminOrderDTOs() {
        return getAllOrders().stream()
                .map(this::toAdminOrderResponseDTO)
                .collect(Collectors.toList());
    }

    public AdminOrderResponseDTO getAdminOrderDTOById(Integer id) {
        Order order = getOrderById(id);
        return toAdminOrderResponseDTO(order);
    }

    public AdminOrderResponseDTO updateOrderStatusAndReturnDTO(Integer id, OrderStatus status) {
        Order updatedOrder = updateOrderStatus(id, status);
        return toAdminOrderResponseDTO(updatedOrder);
    }

    public List<AdminOrderResponseDTO> filterOrders(OrderStatus status, Integer tableId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders;
        if (status != null && tableId != null) {
            TableEntity table = tableRepository.findById(tableId)
                    .orElseThrow(() -> new RuntimeException("Table not found"));
            orders = filterOrdersByStatusAndTable(status, table);
        } else if (status != null && startDate != null && endDate != null) {
            orders = filterOrdersByStatusAndDateRange(status, startDate, endDate);
        } else if (status != null) {
            orders = filterOrdersByStatus(status);
        } else if (tableId != null) {
            orders = filterOrdersByTable(tableId);
        } else if (startDate != null && endDate != null) {
            orders = filterOrdersByDateRange(startDate, endDate);
        } else {
            orders = getAllOrders();
        }
        return orders.stream().map(this::toAdminOrderResponseDTO).collect(Collectors.toList());
    }

    public AdminOrderResponseDTO updatePaymentMethodAndReturnDTO(Integer id, PaymentMethod method) {
        Order order = updatePaymentMethod(id, method);
        return toAdminOrderResponseDTO(order);
    }

    // --- Mapping methods ---
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
        List<AdminOrderResponseDTO.OrderItemInfo> items = getOrderItemsByOrderId(order.getId()).stream().map(item -> {
            AdminOrderResponseDTO.OrderItemInfo oi = new AdminOrderResponseDTO.OrderItemInfo();
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
}