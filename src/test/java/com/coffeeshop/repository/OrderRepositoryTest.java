package com.coffeeshop.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentStatus;
import com.coffeeshop.enums.TableStatus;
import com.coffeeshop.enums.UserRole;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;
    private TableEntity testTable;
    private User testCustomer;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = new User();
        testCustomer.setUsername("testcustomer");
        testCustomer.setEmail("customer@example.com");
        testCustomer.setPassword("hashedpassword");
        testCustomer.setFullName("Test Customer");
        testCustomer.setPhone("0901234567");
        testCustomer.setRole(UserRole.ROLE_CUSTOMER);
        testCustomer.setIsActive(true);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());
        testCustomer = entityManager.persistAndFlush(testCustomer);

        // Create test table
        testTable = new TableEntity();
        testTable.setTableNumber("T001");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setIsActive(true);
        testTable.setLocation("Ground Floor");
        testTable.setCreatedAt(LocalDateTime.now());
        testTable.setUpdatedAt(LocalDateTime.now());
        testTable = entityManager.persistAndFlush(testTable);

        // Create test order
        testOrder = new Order();
        testOrder.setOrderNumber("ORD-123456");
        testOrder.setCustomer(testCustomer);
        testOrder.setTable(testTable);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentStatus(PaymentStatus.UNPAID);
        testOrder.setSubtotal(BigDecimal.valueOf(90000));
        testOrder.setTaxAmount(BigDecimal.valueOf(10000));
        testOrder.setTotalAmount(BigDecimal.valueOf(100000));
        testOrder.setNotes("Test order");
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindByStatus_ExistingStatus() {
        // Arrange
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderNumber()).isEqualTo("ORD-123456");
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testFindByStatus_NonExistingStatus() {
        // Arrange
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByStatus(OrderStatus.COMPLETED);

        // Assert
        assertThat(orders).isEmpty();
    }

    @Test
    void testFindByTable_ExistingTable() {
        // Arrange
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByTable(testTable);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getTable().getTableNumber()).isEqualTo("T001");
    }

    @Test
    void testFindByStatusAndTable() {
        // Arrange
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByStatusAndTable(OrderStatus.PENDING, testTable);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(orders.get(0).getTable()).isEqualTo(testTable);
    }

    @Test
    void testFindByCustomer_Id() {
        // Arrange
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByCustomer_Id(testCustomer.getId());

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCustomer().getId()).isEqualTo(testCustomer.getId());
    }

    @Test
    void testFindByCustomerOrderByCreatedAtDesc() {
        // Arrange
        Order order1 = testOrder;
        entityManager.persistAndFlush(order1);

        // Create a second order for the same customer
        Order order2 = new Order();
        order2.setOrderNumber("ORD-123457");
        order2.setCustomer(testCustomer);
        order2.setTable(testTable);
        order2.setStatus(OrderStatus.PREPARING);
        order2.setPaymentStatus(PaymentStatus.PAID);
        order2.setSubtotal(BigDecimal.valueOf(140000));
        order2.setTaxAmount(BigDecimal.valueOf(10000));
        order2.setTotalAmount(BigDecimal.valueOf(150000));
        order2.setCreatedAt(LocalDateTime.now().plusMinutes(1));
        order2.setUpdatedAt(LocalDateTime.now().plusMinutes(1));
        entityManager.persistAndFlush(order2);

        // Act
        List<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(testCustomer);

        // Assert
        assertThat(orders).hasSize(2);
        // Should be ordered by createdAt desc, so order2 should be first
        assertThat(orders.get(0).getOrderNumber()).isEqualTo("ORD-123457");
        assertThat(orders.get(1).getOrderNumber()).isEqualTo("ORD-123456");
    }

    @Test
    void testFindByPaymentStatus() {
        // Arrange
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByPaymentStatus(PaymentStatus.UNPAID);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void testFindByCreatedAtBetween() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getCreatedAt()).isBetween(start, end);
    }

    @Test
    void testFindByStatusAndCreatedAtBetween() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        entityManager.persistAndFlush(testOrder);

        // Act
        List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(OrderStatus.PENDING, start, end);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(orders.get(0).getCreatedAt()).isBetween(start, end);
    }

    @Test
    void testSave_NewOrder() {
        // Act
        Order saved = orderRepository.save(testOrder);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderNumber()).isEqualTo("ORD-123456");
        assertThat(saved.getCustomer()).isEqualTo(testCustomer);
        assertThat(saved.getTable()).isEqualTo(testTable);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void testFindById_ExistingOrder() {
        // Arrange
        Order saved = entityManager.persistAndFlush(testOrder);

        // Act
        Optional<Order> found = orderRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getOrderNumber()).isEqualTo("ORD-123456");
        assertThat(found.get().getCustomer().getUsername()).isEqualTo("testcustomer");
    }
}