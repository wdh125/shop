package com.coffeeshop.service;

import com.coffeeshop.dto.admin.request.AdminPaymentRequestDTO;
import com.coffeeshop.dto.customer.request.CustomerPaymentRequestDTO;
import com.coffeeshop.dto.admin.request.AdminPaymentStatusUpdateDTO;
import com.coffeeshop.dto.admin.response.AdminPaymentResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerPaymentResponseDTO;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Payment;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentProcessStatus;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.enums.UserRole;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.PaymentRepository;
import com.coffeeshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service chứa các logic nghiệp vụ liên quan đến Payment.
 */
@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderSchedulingService orderSchedulingService;

    @Autowired
    private NotificationService notificationService;

    // --------------------- ENTITY METHODS (PRIVATE/INTERNAL) ---------------------

    private List<Payment> getAllPaymentsEntity() {
        return paymentRepository.findAll();
    }

    private Optional<Payment> getPaymentByIdEntity(Integer id) {
        return paymentRepository.findById(id);
    }

    private List<Payment> getPaymentsByCustomerIdEntity(Integer customerId) {
        userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        return orderRepository.findByCustomer_Id(customerId).stream()
                .flatMap(order -> paymentRepository.findByOrder_Id(order.getId()).stream())
                .collect(Collectors.toList());
    }

    private List<Payment> getPaymentsByOrderIdEntity(Integer orderId) {
        return paymentRepository.findByOrder_Id(orderId);
    }

    // --------------------- DTO METHODS (PUBLIC) ---------------------

    /**
     * Lấy danh sách tất cả payment cho admin.
     */
    public List<AdminPaymentResponseDTO> getAllAdminPaymentDTOs() {
        return getAllPaymentsEntity().stream().map(this::toAdminPaymentResponseDTO).toList();
    }

    /**
     * Lấy payment chi tiết cho admin.
     */
    public AdminPaymentResponseDTO getAdminPaymentDTOById(Integer id) {
        Payment payment = getPaymentByIdEntity(id).orElseThrow(() -> new RuntimeException("Không tìm thấy payment!"));
        return toAdminPaymentResponseDTO(payment);
    }

    /**
     * Cập nhật trạng thái thanh toán (ADMIN) và trả về DTO.
     */
    public AdminPaymentResponseDTO updatePaymentStatusByAdminAndReturnDTO(Integer id, AdminPaymentStatusUpdateDTO request) {
        Payment payment = updatePaymentStatusByAdminEntity(id, request);
        return toAdminPaymentResponseDTO(payment);
    }

    /**
     * Tạo payment mới (ADMIN).
     */
    @Transactional
    public AdminPaymentResponseDTO createPayment(AdminPaymentRequestDTO paymentRequestDTO) {
        Payment payment = createPaymentEntity(paymentRequestDTO);
        return toAdminPaymentResponseDTO(payment);
    }

    /**
     * Lấy danh sách payment của một customer.
     */
    public List<CustomerPaymentResponseDTO> getCustomerPaymentDTOsByCustomerId(Integer customerId) {
        return getPaymentsByCustomerIdEntity(customerId).stream().map(this::toCustomerPaymentResponseDTO).toList();
    }

    /**
     * Lấy danh sách payment của một order (customer).
     */
    public List<CustomerPaymentResponseDTO> getCustomerPaymentDTOsByOrderId(Integer orderId) {
        List<Payment> payments = getPaymentsByOrderIdEntity(orderId);
        return payments.stream()
                .map(this::toCustomerPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách payment của user qua username (customer).
     */
    public List<CustomerPaymentResponseDTO> getCustomerPaymentDTOsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        List<Payment> allPayments = paymentRepository.findAll();
        List<Payment> userPayments = allPayments.stream()
                .filter(payment -> payment.getOrder().getCustomer().getId().equals(user.getId()))
                .collect(Collectors.toList());

        return userPayments.stream()
                .map(this::toCustomerPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết payment theo ID cho đúng customer (qua ownership của order).
     */
    public CustomerPaymentResponseDTO getCustomerPaymentDTOByIdAndUsername(Integer paymentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));
        if (payment.getOrder() == null || payment.getOrder().getCustomer() == null ||
            !payment.getOrder().getCustomer().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xem thanh toán này");
        }
        return toCustomerPaymentResponseDTO(payment);
    }

    /**
     * Tạo payment cho customer.
     */
    public CustomerPaymentResponseDTO createPaymentForCustomer(CustomerPaymentRequestDTO request, String username) {
        Payment payment = createPaymentForCustomerEntity(request, username);
        return toCustomerPaymentResponseDTO(payment);
    }

    // --------------------- ENTITY INTERNAL METHODS ---------------------

    /**
     * Tạo một payment mới (ADMIN) - trả về entity.
     */
    @Transactional
    private Payment createPaymentEntity(AdminPaymentRequestDTO paymentRequestDTO) {
        // Step 1: Lấy order và kiểm tra các điều kiện cơ bản.
        Order order = orderRepository.findById(paymentRequestDTO.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + paymentRequestDTO.getOrderId()));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order has been cancelled. Cannot create payment.");
        }

        // Step 2: Đảm bảo một đơn hàng chỉ có một thanh toán thành công (completed).
        boolean hasCompletedPayment = paymentRepository.findByOrder_Id(order.getId()).stream()
                .anyMatch(p -> p.getStatus() == PaymentProcessStatus.COMPLETED);
        if (hasCompletedPayment) {
            throw new IllegalStateException("Order already has a completed payment. Cannot create more payments.");
        }

        // Step 3: Lấy thông tin người thực hiện thanh toán.
        User processedByUser = userRepository.findById(paymentRequestDTO.getPaidByUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + paymentRequestDTO.getPaidByUserId()));

        // Step 4: Kiểm tra quyền của người thực hiện thanh toán dựa trên phương thức thanh toán.
        validatePaymentPermissions(paymentRequestDTO, processedByUser, order);

        // Step 5: Tạo đối tượng Payment và lưu vào database.
        Payment payment = buildPayment(paymentRequestDTO, order, processedByUser);
        Payment savedPayment = paymentRepository.save(payment);

        // Step 6: Nếu thanh toán thành công, cập nhật trạng thái của Order và LÊN LỊCH.
        if (savedPayment.getStatus() == PaymentProcessStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            notificationService.createPaymentNotification(
                order.getCustomer(),
                savedPayment,
                NotificationType.PAYMENT_RECEIVED,
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng " + order.getOrderNumber() +
                " đã được xử lý thành công với số tiền " + savedPayment.getAmount() + "đ"
            );

            orderSchedulingService.scheduleOrderStatusUpdate(order.getId());
        } else if (savedPayment.getStatus() == PaymentProcessStatus.FAILED) {
            notificationService.createPaymentNotification(
                order.getCustomer(),
                savedPayment,
                NotificationType.PAYMENT_FAILED,
                "Thanh toán thất bại",
                "Thanh toán cho đơn hàng " + order.getOrderNumber() + " đã thất bại. Vui lòng thử lại."
            );
        }

        return savedPayment;
    }

    /**
     * Admin cập nhật trạng thái payment và trả về entity.
     */
    public Payment updatePaymentStatusByAdmin(Integer id, AdminPaymentStatusUpdateDTO request) {
        return updatePaymentStatusByAdminEntity(id, request);
    }

    /**
     * Admin cập nhật trạng thái payment và trả về entity.
     */
    private Payment updatePaymentStatusByAdminEntity(Integer id, AdminPaymentStatusUpdateDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new RuntimeException("Chỉ admin mới được xác nhận thanh toán!");
        }
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // Chỉ cho phép admin xác nhận thanh toán CASH
        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new RuntimeException("Chỉ xác nhận thanh toán tiền mặt! Thanh toán online tự động hoàn thành.");
        }
        
        // Kiểm tra trạng thái hiện tại - chỉ cho phép cập nhật từ PENDING
        if (payment.getStatus() != PaymentProcessStatus.PENDING) {
            throw new RuntimeException("Chỉ cập nhật thanh toán ở trạng thái PENDING!");
        }
        
        PaymentProcessStatus status;
        try {
            status = PaymentProcessStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new RuntimeException("Trạng thái thanh toán không hợp lệ!");
        }
        if (status != PaymentProcessStatus.COMPLETED && status != PaymentProcessStatus.FAILED) {
            throw new RuntimeException("Chỉ cho phép xác nhận COMPLETED hoặc FAILED!");
        }
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        
        if (status == PaymentProcessStatus.COMPLETED) {
            Order order = payment.getOrder();
            order.setStatus(order.getStatus().nextAfterPayment());
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            orderSchedulingService.scheduleOrderStatusUpdate(order.getId());

            if (order.getReservation() != null) {
                System.out.println("Order " + order.getId() + " linked to reservation " + order.getReservation().getId() + " - will be processed by scheduler");
            }
        }
        return payment;
    }

    /**
     * Tạo payment cho customer entity (nội bộ).
     */
    public Payment createPaymentForCustomerEntity(CustomerPaymentRequestDTO request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn chỉ có thể thanh toán đơn hàng của chính mình!");
        }

        if (!order.getStatus().isAllowPayment()) {
            throw new RuntimeException("Đơn hàng không ở trạng thái cho phép thanh toán!");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Số tiền không hợp lệ!");
        }
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (Exception e) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ!");
        }
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(java.math.BigDecimal.valueOf(request.getAmount()));
        payment.setPaymentMethod(method);
        payment.setStatus(method == PaymentMethod.CASH ? PaymentProcessStatus.PENDING : PaymentProcessStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setProcessedBy(user);
        paymentRepository.save(payment);
        if (method != PaymentMethod.CASH) {
            order.setStatus(order.getStatus().nextAfterPayment());
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            orderSchedulingService.scheduleOrderStatusUpdate(order.getId());

            if (order.getReservation() != null) {
                System.out.println("Order " + order.getId() + " linked to reservation " + order.getReservation().getId() + " - will be processed by scheduler");
            }
        }
        return payment;
    }

    // --------------------- DTO MAPPING METHODS ---------------------

    private AdminPaymentResponseDTO toAdminPaymentResponseDTO(Payment payment) {
        AdminPaymentResponseDTO dto = new AdminPaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);

        // Customer info
        AdminPaymentResponseDTO.CustomerInfo customer = new AdminPaymentResponseDTO.CustomerInfo();
        if (payment.getOrder() != null && payment.getOrder().getCustomer() != null) {
            customer.setId(payment.getOrder().getCustomer().getId());
            customer.setUsername(payment.getOrder().getCustomer().getUsername());
            customer.setFullName(payment.getOrder().getCustomer().getFullName());
            customer.setPhone(payment.getOrder().getCustomer().getPhone());
        }
        dto.setCustomer(customer);

        // Order info
        if (payment.getOrder() != null) {
            AdminPaymentResponseDTO.OrderInfo orderInfo = new AdminPaymentResponseDTO.OrderInfo();
            orderInfo.setId(payment.getOrder().getId());
            orderInfo.setOrderNumber(payment.getOrder().getOrderNumber());
            orderInfo.setStatus(payment.getOrder().getStatus() != null ? payment.getOrder().getStatus().name() : null);
            orderInfo.setPaymentStatus(payment.getOrder().getPaymentStatus() != null ? payment.getOrder().getPaymentStatus().name() : null);
            orderInfo.setTotalAmount(payment.getOrder().getTotalAmount() != null ? payment.getOrder().getTotalAmount().doubleValue() : null);
            orderInfo.setSubtotal(payment.getOrder().getSubtotal() != null ? payment.getOrder().getSubtotal().doubleValue() : null);
            orderInfo.setTaxAmount(payment.getOrder().getTaxAmount() != null ? payment.getOrder().getTaxAmount().doubleValue() : null);

            if (payment.getOrder().getOrderItems() != null && !payment.getOrder().getOrderItems().isEmpty()) {
                List<AdminPaymentResponseDTO.OrderItemInfo> orderItems = payment.getOrder().getOrderItems().stream()
                    .map(item -> {
                        AdminPaymentResponseDTO.OrderItemInfo itemInfo = new AdminPaymentResponseDTO.OrderItemInfo();
                        itemInfo.setId(item.getId());
                        itemInfo.setProductName(item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm");
                        itemInfo.setQuantity(item.getQuantity());
                        itemInfo.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null);
                        itemInfo.setTotalPrice(item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null);
                        return itemInfo;
                    })
                    .collect(Collectors.toList());
                orderInfo.setOrderItems(orderItems);
            }

            dto.setOrder(orderInfo);
        }

        dto.setAmount(payment.getAmount() != null ? payment.getAmount().doubleValue() : null);
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    private CustomerPaymentResponseDTO toCustomerPaymentResponseDTO(Payment payment) {
        CustomerPaymentResponseDTO dto = new CustomerPaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        dto.setAmount(payment.getAmount() != null ? payment.getAmount().doubleValue() : null);
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }

    // --------------------- HELPER METHODS ---------------------

    private void validatePaymentPermissions(AdminPaymentRequestDTO dto, User user, Order order) {
        PaymentMethod method = dto.getPaymentMethod();
        UserRole allowedRole = method.getAllowedRole();
        if (user.getRole() != allowedRole) {
            throw new IllegalStateException("Only " + allowedRole + " can process " + method + " payments.");
        }
        if (allowedRole == UserRole.ROLE_CUSTOMER && !order.getCustomer().getId().equals(user.getId())) {
            throw new IllegalStateException("Only the customer who created the order can pay by " + method + ".");
        }
    }

    private Payment buildPayment(AdminPaymentRequestDTO dto, Order order, User user) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount()); // Lấy tổng tiền từ order
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setProcessedBy(user);
        payment.setStatus(dto.getStatus() != null ? dto.getStatus() : PaymentProcessStatus.COMPLETED);
        payment.setCreatedAt(java.time.LocalDateTime.now());
        return payment;
    }
}