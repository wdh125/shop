package com.coffeeshop.dto.customer.response;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.TableStatus;

public class CustomerTableResponseDTO {
    private Integer id;
    private String tableNumber;
    private Integer capacity;
    private String location;
    private String status; // FE nhận String, backend dùng Enum
    private Boolean isActive;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // Factory method
    public static CustomerTableResponseDTO fromEntity(TableEntity entity) {
        CustomerTableResponseDTO dto = new CustomerTableResponseDTO();
        dto.setId(entity.getId());
        dto.setTableNumber(entity.getTableNumber());
        dto.setCapacity(entity.getCapacity());
        dto.setLocation(entity.getLocation());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setIsActive(entity.getIsActive());
        return dto;
    }
} 