package com.coffeeshop.dto.customer.response;

import java.time.LocalDateTime;

public class TableReservationStatusDTO {
    private Integer tableId;
    private String tableNumber;
    private LocalDateTime reservationDatetime;
    private String status;

    // Constructor mặc định
    public TableReservationStatusDTO() {}

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public LocalDateTime getReservationDatetime() { return reservationDatetime; }
    public void setReservationDatetime(LocalDateTime reservationDatetime) { this.reservationDatetime = reservationDatetime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 