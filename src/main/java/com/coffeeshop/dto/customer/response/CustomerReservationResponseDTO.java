package com.coffeeshop.dto.customer.response;

import java.time.LocalDateTime;

public class CustomerReservationResponseDTO {
    private Integer id;
    private TableInfo table;
    private LocalDateTime reservationDatetime;
    private Integer partySize;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public TableInfo getTable() { return table; }
    public void setTable(TableInfo table) { this.table = table; }
    public LocalDateTime getReservationDatetime() { return reservationDatetime; }
    public void setReservationDatetime(LocalDateTime reservationDatetime) { this.reservationDatetime = reservationDatetime; }
    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    // Inner class getter/setter
    public static class TableInfo {
        private Integer id;
        private String tableNumber;
        private String location;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTableNumber() { return tableNumber; }
        public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
    // constructor, fromEntity nếu cần
} 