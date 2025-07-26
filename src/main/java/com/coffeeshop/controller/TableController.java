package com.coffeeshop.controller;

import com.coffeeshop.service.TableService;
import com.coffeeshop.dto.admin.response.AdminTableResponseDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerTableResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {
    @Autowired
    private TableService tableService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminTableResponseDTO> getAllTables() {
        return tableService.getAllAdminTableDTOs();
    }

    @GetMapping("/available")
    public List<CustomerTableResponseDTO> getAvailableTables() {
        return tableService.getAvailableCustomerTableDTOs();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminTableResponseDTO getTableById(@PathVariable Integer id) {
        return tableService.getAdminTableDTOById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminTableResponseDTO createTable(@Valid @RequestBody AdminTableRequestDTO request) {
        return tableService.createTable(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminTableResponseDTO updateTable(@PathVariable Integer id, @Valid @RequestBody AdminTableRequestDTO request) {
        return tableService.updateTable(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTable(@PathVariable Integer id) {
        tableService.deleteTable(id);
    }
}