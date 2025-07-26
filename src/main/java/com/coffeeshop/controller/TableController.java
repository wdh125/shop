package com.coffeeshop.controller;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.service.TableService;
import com.coffeeshop.dto.admin.response.AdminTableResponseDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerTableResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {
    @Autowired
    private TableService tableService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminTableResponseDTO> getAllTables() {
        return tableService.getAllTables().stream().map(this::toAdminTableResponseDTO).toList();
    }

    @GetMapping("/available")
    public List<CustomerTableResponseDTO> getAvailableTables() {
        return tableService.getAllTables().stream()
            .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
            .map(this::toCustomerTableResponseDTO)
            .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminTableResponseDTO getTableById(@PathVariable Integer id) {
        return tableService.getTableById(id).map(this::toAdminTableResponseDTO)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn!"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminTableResponseDTO createTable(@RequestBody AdminTableRequestDTO request) {
        TableEntity table = new TableEntity();
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setLocation(request.getLocation());
        table.setStatus(request.getStatus());
        table.setIsActive(request.getIsActive());
        return toAdminTableResponseDTO(tableService.saveTable(table));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminTableResponseDTO updateTable(@PathVariable Integer id, @RequestBody AdminTableRequestDTO request) {
        TableEntity table = new TableEntity();
        table.setId(id);
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setLocation(request.getLocation());
        table.setStatus(request.getStatus());
        table.setIsActive(request.getIsActive());
        return toAdminTableResponseDTO(tableService.saveTable(table));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTable(@PathVariable Integer id) {
        tableService.deleteTable(id);
    }

    private AdminTableResponseDTO toAdminTableResponseDTO(TableEntity table) {
        AdminTableResponseDTO dto = new AdminTableResponseDTO();
        dto.setId(table.getId());
        dto.setTableNumber(table.getTableNumber());
        dto.setCapacity(table.getCapacity());
        dto.setLocation(table.getLocation());
        dto.setStatus(table.getStatus() != null ? table.getStatus().name() : null);
        dto.setIsActive(table.getIsActive());
        dto.setCreatedAt(table.getCreatedAt());
        dto.setUpdatedAt(table.getUpdatedAt());
        return dto;
    }
    
    private CustomerTableResponseDTO toCustomerTableResponseDTO(TableEntity table) {
        CustomerTableResponseDTO dto = new CustomerTableResponseDTO();
        dto.setId(table.getId());
        dto.setTableNumber(table.getTableNumber());
        dto.setCapacity(table.getCapacity());
        dto.setLocation(table.getLocation());
        dto.setStatus(table.getStatus() != null ? table.getStatus().name() : null);
        dto.setIsActive(table.getIsActive());
        return dto;
    }
}