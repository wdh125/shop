package com.coffeeshop.dto.customer.request;

import java.time.LocalDateTime;

public class ReservationRequestDTO {
    private Integer customerId;
    private Integer tableId;
    private LocalDateTime reservationDatetime;
    private Integer partySize;
    private String notes;
    
    // Default constructor for Jackson deserialization
    public ReservationRequestDTO() {}
    
    // Full constructor
    public ReservationRequestDTO(Integer customerId, Integer tableId, LocalDateTime reservationDatetime, Integer partySize, String notes) {
        this.customerId = customerId;
        this.tableId = tableId;
        this.reservationDatetime = reservationDatetime;
        this.partySize = partySize;
        this.notes = notes;
    }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public LocalDateTime getReservationDatetime() { return reservationDatetime; }
    public void setReservationDatetime(LocalDateTime reservationDatetime) { this.reservationDatetime = reservationDatetime; }
    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
} 