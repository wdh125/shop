package com.coffeeshop.service;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.TableStatus;
import com.coffeeshop.repository.TableRepository;
import com.coffeeshop.service.impl.TableServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TableService
 * Tests table management including CRUD operations and business logic
 */
@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private TableRepository tableRepository;
    
    @InjectMocks
    private TableServiceImpl tableService;
    
    private TableEntity testTable;
    private TableEntity anotherTable;
    
    @BeforeEach
    void setUp() {
        testTable = new TableEntity();
        testTable.setId(1);
        testTable.setTableNumber("T01");
        testTable.setLocation("Main Floor");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setCreatedAt(LocalDateTime.now().minusDays(1));
        testTable.setUpdatedAt(LocalDateTime.now().minusHours(1));
        
        anotherTable = new TableEntity();
        anotherTable.setId(2);
        anotherTable.setTableNumber("T02");
        anotherTable.setLocation("Balcony");
        anotherTable.setCapacity(2);
        anotherTable.setStatus(TableStatus.OCCUPIED);
        anotherTable.setCreatedAt(LocalDateTime.now().minusDays(2));
        anotherTable.setUpdatedAt(LocalDateTime.now().minusHours(2));
    }
    
    @Test
    @DisplayName("Get all tables should return all tables")
    void getAllTables_ShouldReturnAllTables() {
        // Arrange
        List<TableEntity> expectedTables = Arrays.asList(testTable, anotherTable);
        when(tableRepository.findAll()).thenReturn(expectedTables);
        
        // Act
        List<TableEntity> result = tableService.getAllTables();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testTable, anotherTable);
        
        verify(tableRepository).findAll();
    }
    
    @Test
    @DisplayName("Get all tables when empty should return empty list")
    void getAllTables_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(tableRepository.findAll()).thenReturn(Arrays.asList());
        
        // Act
        List<TableEntity> result = tableService.getAllTables();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        
        verify(tableRepository).findAll();
    }
    
    @Test
    @DisplayName("Get table by ID should return table when exists")
    void getTableById_WhenTableExists_ShouldReturnTable() {
        // Arrange
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        
        // Act
        Optional<TableEntity> result = tableService.getTableById(1);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testTable);
        assertThat(result.get().getTableNumber()).isEqualTo("T01");
        
        verify(tableRepository).findById(1);
    }
    
    @Test
    @DisplayName("Get table by ID should return empty when not exists")
    void getTableById_WhenTableNotExists_ShouldReturnEmpty() {
        // Arrange
        when(tableRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act
        Optional<TableEntity> result = tableService.getTableById(999);
        
        // Assert
        assertThat(result).isEmpty();
        
        verify(tableRepository).findById(999);
    }
    
    @Test
    @DisplayName("Save new table should set created and updated timestamps")
    void saveTable_WhenNewTable_ShouldSetTimestamps() {
        // Arrange
        TableEntity newTable = new TableEntity();
        newTable.setTableNumber("T03");
        newTable.setLocation("Garden");
        newTable.setCapacity(6);
        newTable.setStatus(TableStatus.AVAILABLE);
        
        TableEntity savedTable = new TableEntity();
        savedTable.setId(3);
        savedTable.setTableNumber("T03");
        savedTable.setLocation("Garden");
        savedTable.setCapacity(6);
        savedTable.setStatus(TableStatus.AVAILABLE);
        savedTable.setCreatedAt(LocalDateTime.now());
        savedTable.setUpdatedAt(LocalDateTime.now());
        
        when(tableRepository.save(any(TableEntity.class))).thenReturn(savedTable);
        
        // Act
        TableEntity result = tableService.saveTable(newTable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3);
        assertThat(result.getTableNumber()).isEqualTo("T03");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        
        verify(tableRepository).save(argThat(table -> 
            table.getCreatedAt() != null && table.getUpdatedAt() != null));
    }
    
    @Test
    @DisplayName("Save existing table should preserve created timestamp and update updated timestamp")
    void saveTable_WhenExistingTable_ShouldPreserveCreatedTimestamp() {
        // Arrange
        TableEntity existingTable = new TableEntity();
        existingTable.setId(1);
        existingTable.setTableNumber("T01-Updated");
        existingTable.setLocation("Main Floor - Updated");
        existingTable.setCapacity(6);
        existingTable.setStatus(TableStatus.RESERVED);
        existingTable.setCreatedAt(testTable.getCreatedAt()); // Keep original
        
        when(tableRepository.save(any(TableEntity.class))).thenReturn(existingTable);
        
        // Act
        TableEntity result = tableService.saveTable(existingTable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isEqualTo(testTable.getCreatedAt());
        assertThat(result.getUpdatedAt()).isNotNull();
        
        verify(tableRepository).save(argThat(table -> 
            table.getUpdatedAt() != null));
    }
    
    @Test
    @DisplayName("Save existing table without created timestamp should fetch from repository")
    void saveTable_WhenExistingTableWithoutCreatedAt_ShouldFetchFromRepository() {
        // Arrange
        TableEntity existingTable = new TableEntity();
        existingTable.setId(1);
        existingTable.setTableNumber("T01-Updated");
        existingTable.setCreatedAt(null); // Missing created timestamp
        
        when(tableRepository.findById(1)).thenReturn(Optional.of(testTable));
        when(tableRepository.save(any(TableEntity.class))).thenReturn(existingTable);
        
        // Act
        TableEntity result = tableService.saveTable(existingTable);
        
        // Assert
        verify(tableRepository).findById(1);
        verify(tableRepository).save(argThat(table -> 
            table.getCreatedAt() != null && table.getUpdatedAt() != null));
    }
    
    @Test
    @DisplayName("Delete table should call repository delete by ID")
    void deleteTable_ShouldCallRepositoryDelete() {
        // Arrange
        Integer tableId = 1;
        doNothing().when(tableRepository).deleteById(tableId);
        
        // Act
        tableService.deleteTable(tableId);
        
        // Assert
        verify(tableRepository).deleteById(tableId);
    }
    
    @Test
    @DisplayName("Save table with edge case parameters should handle properly")
    void saveTable_WithEdgeCaseParameters_ShouldHandleProperly() {
        // Arrange
        TableEntity edgeTable = new TableEntity();
        edgeTable.setTableNumber("VIP-01");
        edgeTable.setLocation("Private Room");
        edgeTable.setCapacity(1);
        edgeTable.setStatus(TableStatus.RESERVED);
        
        when(tableRepository.save(any(TableEntity.class))).thenReturn(edgeTable);
        
        // Act
        TableEntity result = tableService.saveTable(edgeTable);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCapacity()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(TableStatus.RESERVED);
        
        verify(tableRepository).save(any(TableEntity.class));
    }
}