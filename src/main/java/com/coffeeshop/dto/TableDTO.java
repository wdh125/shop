package com.coffeeshop.dto;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.TableStatus;

public class TableDTO {
    private Integer id;
    private String tableNumber;
    private Integer capacity;
    private String location;
    private TableStatus status;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public static TableDTO fromEntity(TableEntity t) {
        TableDTO dto = new TableDTO();
        dto.id = t.getId();
        dto.tableNumber = t.getTableNumber();
        dto.capacity = t.getCapacity();
        dto.location = t.getLocation();
        dto.status = t.getStatus();
        dto.createdAt = t.getCreatedAt();
        dto.updatedAt = t.getUpdatedAt();
        return dto;
    }
    public Integer getId() { return id; }
    public String getTableNumber() { return tableNumber; }
    public Integer getCapacity() { return capacity; }
    public String getLocation() { return location; }
    public TableStatus getStatus() { return status; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
} 