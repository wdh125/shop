package com.coffeeshop.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.coffeeshop.enums.OrderStatus;
import com.coffeeshop.enums.PaymentMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "table_id", nullable = false)
	private TableEntity table;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservation_id")
	private Reservation reservation;

	@Column(nullable = false, length = 20, unique = true)
	private String orderNumber;

	@Column(name = "subtotal", nullable = false)
	private java.math.BigDecimal subtotal;

	@Column(name = "tax_amount")
	private java.math.BigDecimal taxAmount;

	@Column(name = "total_amount", nullable = false)
	private java.math.BigDecimal totalAmount;

	@Column(length = 255, unique = true)
	private String qrCodePayment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status = OrderStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private com.coffeeshop.enums.PaymentStatus paymentStatus = com.coffeeshop.enums.PaymentStatus.UNPAID;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private PaymentMethod paymentMethod;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@OneToMany(mappedBy = "order")
	private List<OrderItem> items;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getCustomer() {
		return customer;
	}

	public void setCustomer(User customer) {
		this.customer = customer;
	}

	public TableEntity getTable() {
		return table;
	}

	public void setTable(TableEntity table) {
		this.table = table;
	}

	public Reservation getReservation() {
		return reservation;
	}

	public void setReservation(Reservation reservation) {
		this.reservation = reservation;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public java.math.BigDecimal getSubtotal() { return subtotal; }
	public void setSubtotal(java.math.BigDecimal subtotal) { this.subtotal = subtotal; }
	public java.math.BigDecimal getTaxAmount() { return taxAmount; }
	public void setTaxAmount(java.math.BigDecimal taxAmount) { this.taxAmount = taxAmount; }
	public java.math.BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }

	public String getQrCodePayment() {
		return qrCodePayment;
	}

	public void setQrCodePayment(String qrCodePayment) {
		this.qrCodePayment = qrCodePayment;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public com.coffeeshop.enums.PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(com.coffeeshop.enums.PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<OrderItem> getItems() { return items; }

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}