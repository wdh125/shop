package com.coffeeshop.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.repository.TableRepository;
import com.coffeeshop.dto.admin.response.AdminTableResponseDTO;
import com.coffeeshop.dto.admin.request.AdminTableRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerTableResponseDTO;
import com.coffeeshop.service.TableService;

@Service
public class TableServiceImpl implements TableService {
	
	@Autowired
	private TableRepository tableRepository;

	@Override
	public List<TableEntity> getAllTables() {
		return tableRepository.findAll();
	}

	@Override
	public Optional<TableEntity> getTableById(Integer id) {
		return tableRepository.findById(id);
	}

	@Override
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

	@Override
	public void deleteTable(Integer id) {
		tableRepository.deleteById(id);
	}

	@Override
	public List<AdminTableResponseDTO> getAllAdminTableDTOs() {
		return getAllTables().stream()
			.map(this::toAdminTableResponseDTO)
			.collect(Collectors.toList());
	}

	@Override
	public List<CustomerTableResponseDTO> getAvailableCustomerTableDTOs() {
		return getAllTables().stream()
			.filter(t -> Boolean.TRUE.equals(t.getIsActive()))
			.map(this::toCustomerTableResponseDTO)
			.collect(Collectors.toList());
	}

	@Override
	public AdminTableResponseDTO getAdminTableDTOById(Integer id) {
		TableEntity table = getTableById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn!"));
		return toAdminTableResponseDTO(table);
	}

	@Override
	public AdminTableResponseDTO createTable(AdminTableRequestDTO request) {
		TableEntity table = new TableEntity();
		table.setTableNumber(request.getTableNumber());
		table.setCapacity(request.getCapacity());
		table.setLocation(request.getLocation());
		table.setStatus(request.getStatus());
		table.setIsActive(request.getIsActive());
		return toAdminTableResponseDTO(saveTable(table));
	}

	@Override
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