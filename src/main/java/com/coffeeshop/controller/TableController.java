package com.coffeeshop.controller;

import com.coffeeshop.dto.TableDTO;
import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {
    @Autowired
    private TableService tableService;

    @GetMapping
    public List<TableDTO> getAllTables() {
        return tableService.getAllTables().stream().map(TableDTO::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public TableDTO getTableById(@PathVariable Integer id) {
        return tableService.getTableById(id).map(TableDTO::fromEntity)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bàn!"));
    }

    @PostMapping
    public TableDTO createTable(@RequestBody TableEntity table) {
        return TableDTO.fromEntity(tableService.saveTable(table));
    }

    @PutMapping("/{id}")
    public TableDTO updateTable(@PathVariable Integer id, @RequestBody TableEntity table) {
        table.setId(id);
        return TableDTO.fromEntity(tableService.saveTable(table));
    }

    @DeleteMapping("/{id}")
    public void deleteTable(@PathVariable Integer id) {
        tableService.deleteTable(id);
    }
}