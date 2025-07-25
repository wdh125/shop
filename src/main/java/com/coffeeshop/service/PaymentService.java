package com.coffeeshop.service;

import com.coffeeshop.dto.admin.request.PaymentRequestDTO;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Payment;
import com.coffeeshop.entity.User;
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
    public Payment createPayment(PaymentRequestDTO paymentRequestDTO) {
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

            // Kích hoạt tác vụ bất đồng bộ để theo dõi và cập nhật trạng thái chuẩn bị/sẵn sàng
            orderSchedulingService.scheduleOrderStatusUpdate(order.getId());
        }

        return savedPayment;
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

    // --- Helper Methods ---

    private void validatePaymentPermissions(PaymentRequestDTO dto, User user, Order order) {
        PaymentMethod method = dto.getPaymentMethod();
        UserRole allowedRole = method.getAllowedRole();
        if (user.getRole() != allowedRole) {
            throw new IllegalStateException("Only " + allowedRole + " can process " + method + " payments.");
        }
        if (allowedRole == UserRole.ROLE_CUSTOMER && !order.getCustomer().getId().equals(user.getId())) {
            throw new IllegalStateException("Only the customer who created the order can pay by " + method + ".");
        }
    }

    private Payment buildPayment(PaymentRequestDTO dto, Order order, User user) {
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