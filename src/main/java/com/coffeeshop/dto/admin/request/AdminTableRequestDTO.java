package com.coffeeshop.dto.admin.request;

import com.coffeeshop.enums.TableStatus;

public class AdminTableRequestDTO {
    private String tableNumber;
    private Integer capacity;
    private String location;
    private TableStatus status;
    private Boolean isActive;
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
} 