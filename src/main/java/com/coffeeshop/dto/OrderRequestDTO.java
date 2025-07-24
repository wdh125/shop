package com.coffeeshop.dto;

import java.util.List;

public class OrderRequestDTO {
	private Integer tableId; // Tùy chọn nếu có reservationId
	private Integer userId; // Tùy chọn nếu có reservationId
	private Integer reservationId; // Tùy chọn, dùng cho việc đặt món trước
	private String note;
	private List<OrderItemDTO> items;

	// Getters and Setters
	public Integer getTableId() { return tableId; }
	public void setTableId(Integer tableId) { this.tableId = tableId; }
	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public Integer getReservationId() { return reservationId; }
	public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }
	public List<OrderItemDTO> getItems() { return items; }
	public void setItems(List<OrderItemDTO> items) { this.items = items; }

	public static class OrderItemDTO {
		private Integer productId;
		private Integer quantity;

		// getter, setter
		public Integer getProductId() {
			return productId;
		}

		public void setProductId(Integer productId) {
			this.productId = productId;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}
	}

}