package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.dto.admin.response.AdminTableResponseDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerTableResponseDTO;

/**
 * Table service interface for managing restaurant tables
 */
public interface TableService {

    /**
     * Get all tables
     */
    List<TableEntity> getAllTables();

    /**
     * Get table by ID
     */
    Optional<TableEntity> getTableById(Integer id);

    /**
     * Save or update table
     */
    TableEntity saveTable(TableEntity table);

    /**
     * Delete table by ID
     */
    void deleteTable(Integer id);

    /**
     * Get all tables for admin view
     */
    List<AdminTableResponseDTO> getAllAdminTableDTOs();

    /**
     * Get available tables for customer view
     */
    List<CustomerTableResponseDTO> getAvailableCustomerTableDTOs();

    /**
     * Get admin table DTO by ID
     */
    AdminTableResponseDTO getAdminTableDTOById(Integer id);

    /**
     * Create new table
     */
    AdminTableResponseDTO createTable(AdminTableRequestDTO request);

    /**
     * Update existing table
     */
    AdminTableResponseDTO updateTable(Integer id, AdminTableRequestDTO request);
}