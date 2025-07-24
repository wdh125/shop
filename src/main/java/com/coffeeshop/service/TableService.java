package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.repository.TableRepository;

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
			table.setCreatedAt(java.time.LocalDateTime.now());
		} else if (table.getCreatedAt() == null) {
			table.setCreatedAt(tableRepository.findById(table.getId())
				.map(TableEntity::getCreatedAt)
				.orElse(java.time.LocalDateTime.now()));
		}
		table.setUpdatedAt(java.time.LocalDateTime.now());
		return tableRepository.save(table);
	}

	public void deleteTable(Integer id) {
		tableRepository.deleteById(id);
	}
}