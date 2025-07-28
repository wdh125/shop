package com.coffeeshop.dto.customer.request;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

public class ReservationRequestDTO {
    @NotNull(message = "ID bàn không được để trống")
    private Integer tableId;
    
    @NotNull(message = "Thời gian đặt bàn không được để trống")
    @Future(message = "Thời gian đặt bàn phải trong tương lai")
    private LocalDateTime reservationDatetime;
    
    @NotNull(message = "Số lượng người không được để trống")
    @Min(value = 1, message = "Số lượng người phải ít nhất là 1")
    @Max(value = 20, message = "Số lượng người không được vượt quá 20")
    private Integer partySize;
    
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;
    
    // Default constructor for Jackson deserialization
    public ReservationRequestDTO() {}
    
    // Full constructor
    public ReservationRequestDTO(Integer tableId, LocalDateTime reservationDatetime, Integer partySize, String notes) {
        this.tableId = tableId;
        this.reservationDatetime = reservationDatetime;
        this.partySize = partySize;
        this.notes = notes;
    }
    
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    
    public LocalDateTime getReservationDatetime() { return reservationDatetime; }
    public void setReservationDatetime(LocalDateTime reservationDatetime) { this.reservationDatetime = reservationDatetime; }
    
    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
} 