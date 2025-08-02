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

	public List<Payment> getAllPayments() {
		return paymentRepository.findAll();
	}

	public Optional<Payment> getPaymentById(Integer id) {
		return paymentRepository.findById(id);
	}

    /**
     * Tạo một thanh toán mới và cập nhật trạng thái đơn hàng.
     * Đây là một giao dịch (transaction), nếu có lỗi xảy ra, mọi thay đổi sẽ được rollback.
     *
     * @param paymentRequestDTO DTO chứa thông tin cần thiết để tạo payment.
     * @return Entity Payment sau khi được lưu.
     * @throws RuntimeException      nếu Order hoặc User không tìm thấy.
     * @throws IllegalStateException nếu logic nghiệp vụ bị vi phạm (VD: đơn đã hủy, đã thanh toán).
     */
    @Transactional
    public Payment createPayment(AdminPaymentRequestDTO paymentRequestDTO) {
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
            // Cập nhật trạng thái thanh toán của Order
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            // Create notification for successful payment
            notificationService.createPaymentNotification(
                order.getCustomer(),
                savedPayment,
                NotificationType.PAYMENT_RECEIVED,
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng " + order.getOrderNumber() + 
                " đã được xử lý thành công với số tiền " + savedPayment.getAmount() + "đ"
            );

            // Kích hoạt tác vụ bất đồng bộ để theo dõi và cập nhật trạng thái chuẩn bị/sẵn sàng
            orderSchedulingService.scheduleOrderStatusUpdate(order.getId());
        } else if (savedPayment.getStatus() == PaymentProcessStatus.FAILED) {
            // Create notification for failed payment
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

    // Đã chuyển thành createPaymentForCustomerEntity phía dưới

    public Payment updatePaymentStatusByAdmin(Integer id, AdminPaymentStatusUpdateDTO request) {
        // Lấy user hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new RuntimeException("Chỉ admin mới được xác nhận thanh toán!");
        }
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new RuntimeException("Chỉ xác nhận thanh toán tiền mặt!");
        }
        // Chỉ cho phép cập nhật nếu payment đang ở trạng thái FAILED (tương đương PENDING logic cũ)
        if (payment.getStatus() != PaymentProcessStatus.FAILED) {
            throw new RuntimeException("Chỉ cập nhật thanh toán ở trạng thái FAILED!");
        }
        // Cập nhật trạng thái
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
        // Nếu COMPLETED thì cập nhật trạng thái order
        if (status == PaymentProcessStatus.COMPLETED) {
            Order order = payment.getOrder();
            order.setStatus(order.getStatus().nextAfterPayment());
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
        }
        return payment;
    }

    /**
     * Lấy danh sách thanh toán của một khách hàng.
     * @param customerId ID của khách hàng.
     * @return List các payment.
     */
    public List<Payment> getPaymentsByCustomerId(Integer customerId) {
        userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        return orderRepository.findByCustomer_Id(customerId).stream()
                .flatMap(order -> paymentRepository.findByOrder_Id(order.getId()).stream())
                .collect(Collectors.toList());
    }

    public List<Payment> getPaymentsByOrderId(Integer orderId) {
        return paymentRepository.findByOrder_Id(orderId);
    }

    // --- DTO Service Methods ---
    public List<AdminPaymentResponseDTO> getAllAdminPaymentDTOs() {
        return getAllPayments().stream().map(this::toAdminPaymentResponseDTO).toList();
    }

    public AdminPaymentResponseDTO getAdminPaymentDTOById(Integer id) {
        Payment payment = getPaymentById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy payment!"));
        return toAdminPaymentResponseDTO(payment);
    }

    public List<CustomerPaymentResponseDTO> getCustomerPaymentDTOsByCustomerId(Integer customerId) {
        return getPaymentsByCustomerId(customerId).stream().map(this::toCustomerPaymentResponseDTO).toList();
    }

    public List<CustomerPaymentResponseDTO> getCustomerPaymentDTOsByOrderId(Integer orderId) {
        List<Payment> payments = getPaymentsByOrderId(orderId);
        return payments.stream()
                .map(this::toCustomerPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerPaymentResponseDTO> getCustomerPaymentDTOsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        // Lấy tất cả payments và filter theo customer
        List<Payment> allPayments = paymentRepository.findAll();
        List<Payment> userPayments = allPayments.stream()
                .filter(payment -> payment.getOrder().getCustomer().getId().equals(user.getId()))
                .collect(Collectors.toList());
        
        return userPayments.stream()
                .map(this::toCustomerPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    public AdminPaymentResponseDTO updatePaymentStatusByAdminAndReturnDTO(Integer id, AdminPaymentStatusUpdateDTO request) {
        Payment payment = updatePaymentStatusByAdmin(id, request);
        return toAdminPaymentResponseDTO(payment);
    }

    public CustomerPaymentResponseDTO createPaymentForCustomer(CustomerPaymentRequestDTO request, String username) {
        Payment payment = createPaymentForCustomerEntity(request, username);
        return toCustomerPaymentResponseDTO(payment);
    }

    // --- Helper mapping methods ---
    private AdminPaymentResponseDTO toAdminPaymentResponseDTO(Payment payment) {
        AdminPaymentResponseDTO dto = new AdminPaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        AdminPaymentResponseDTO.CustomerInfo customer = new AdminPaymentResponseDTO.CustomerInfo();
        if (payment.getOrder() != null && payment.getOrder().getCustomer() != null) {
            customer.setId(payment.getOrder().getCustomer().getId());
            customer.setUsername(payment.getOrder().getCustomer().getUsername());
            customer.setFullName(payment.getOrder().getCustomer().getFullName());
            customer.setPhone(payment.getOrder().getCustomer().getPhone());
        }
        dto.setCustomer(customer);
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

    // --- Helper: giữ lại logic cũ cho entity ---
    public Payment createPaymentForCustomerEntity(CustomerPaymentRequestDTO request, String username) {
        // Validate user exists and get user info
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        
        // Kiểm tra order tồn tại và thuộc về user
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order không tồn tại"));
        
        // Security validation: ensure user can only create payments for their own orders
        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn chỉ có thể thanh toán đơn hàng của chính mình!");
        }
        
        // Kiểm tra trạng thái đơn
        if (!order.getStatus().isAllowPayment()) {
            throw new RuntimeException("Đơn hàng không ở trạng thái cho phép thanh toán!");
        }
        // Kiểm tra số tiền hợp lệ
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new RuntimeException("Số tiền không hợp lệ!");
        }
        // Kiểm tra phương thức thanh toán hợp lệ
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (Exception e) {
            throw new RuntimeException("Phương thức thanh toán không hợp lệ!");
        }
        // Tạo payment
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(java.math.BigDecimal.valueOf(request.getAmount()));
        payment.setPaymentMethod(method);
        payment.setStatus(method == PaymentMethod.CASH ? PaymentProcessStatus.FAILED : PaymentProcessStatus.COMPLETED);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setProcessedBy(user);
        paymentRepository.save(payment);
        // Nếu là CARD/QR_CODE thì cập nhật order đã thanh toán
        if (method != PaymentMethod.CASH) {
            order.setStatus(order.getStatus().nextAfterPayment());
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
        }
        return payment;
    }

    // --- Helper Methods ---

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
        // Không set updatedAt vì payment không sửa
        return payment;
    }
}