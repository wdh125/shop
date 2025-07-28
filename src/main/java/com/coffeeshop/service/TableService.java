package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.repository.TableRepository;
import com.coffeeshop.dto.admin.response.AdminTableResponseDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerTableResponseDTO;
import java.time.LocalDateTime;

@Service
public class TableService {
	@Autowired
	private TableRepository tableRepository;

	public List<TableEntity> getAllTables() {
		return tableRepository.findAll();
	}

	public Optional<TableEntity> getTableById(Integer id) {
		return tableRepository.findById(id);
	}

	public TableEntity saveTable(TableEntity table) {
		if (table.getId() == null) {
			table.setCreatedAt(LocalDateTime.now());
		} else if (table.getCreatedAt() == null) {
			table.setCreatedAt(tableRepository.findById(table.getId())
				.map(TableEntity::getCreatedAt)
				.orElse(LocalDateTime.now()));
		}
		table.setUpdatedAt(LocalDateTime.now());
		return tableRepository.save(table);
	}

	public void deleteTable(Integer id) {
		tableRepository.deleteById(id);
	}

	// New methods for DTO mapping and business logic
	public List<AdminTableResponseDTO> getAllAdminTableDTOs() {
		return getAllTables().stream()
			.map(this::toAdminTableResponseDTO)
			.collect(Collectors.toList());
	}

	public List<CustomerTableResponseDTO> getAvailableCustomerTableDTOs() {
		return getAllTables().stream()
			.filter(t -> Boolean.TRUE.equals(t.getIsActive()))
			.map(this::toCustomerTableResponseDTO)
			.collect(Collectors.toList());
	}

	public AdminTableResponseDTO getAdminTableDTOById(Integer id) {
		TableEntity table = getTableById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn!"));
		return toAdminTableResponseDTO(table);
	}

	public AdminTableResponseDTO createTable(AdminTableRequestDTO request) {
		TableEntity table = new TableEntity();
		table.setTableNumber(request.getTableNumber());
		table.setCapacity(request.getCapacity());
		table.setLocation(request.getLocation());
		table.setStatus(request.getStatus());
		table.setIsActive(request.getIsActive());
		return toAdminTableResponseDTO(saveTable(table));
	}

	public AdminTableResponseDTO updateTable(Integer id, AdminTableRequestDTO request) {
		TableEntity table = getTableById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn!"));
		table.setTableNumber(request.getTableNumber());
		table.setCapacity(request.getCapacity());
		table.setLocation(request.getLocation());
		table.setStatus(request.getStatus());
		table.setIsActive(request.getIsActive());
		return toAdminTableResponseDTO(saveTable(table));
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