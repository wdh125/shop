package com.coffeeshop.dto.admin.response;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.TableStatus;
import java.time.LocalDateTime;

public class AdminTableResponseDTO {
    private Integer id;
    private String tableNumber;
    private Integer capacity;
    private String location;
    private String status; // FE nhận String, backend dùng Enum
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Factory method
    public static AdminTableResponseDTO fromEntity(TableEntity entity) {
        AdminTableResponseDTO dto = new AdminTableResponseDTO();
        dto.setId(entity.getId());
        dto.setTableNumber(entity.getTableNumber());
        dto.setCapacity(entity.getCapacity());
        dto.setLocation(entity.getLocation());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
} 