package com.coffeeshop.entity;

import java.time.LocalDateTime;

import com.coffeeshop.enums.PaymentMethod;
import com.coffeeshop.enums.PaymentProcessStatus;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 20)
	private PaymentMethod paymentMethod;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private java.math.BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private PaymentProcessStatus status = PaymentProcessStatus.FAILED;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "processed_by")
	private User processedBy;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private java.time.LocalDateTime updatedAt;
	public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public java.math.BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(java.math.BigDecimal amount) {
		this.amount = amount;
	}

	public PaymentProcessStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentProcessStatus status) {
		this.status = status;
	}

	public User getProcessedBy() {
		return processedBy;
	}

	public void setProcessedBy(User processedBy) {
		this.processedBy = processedBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}