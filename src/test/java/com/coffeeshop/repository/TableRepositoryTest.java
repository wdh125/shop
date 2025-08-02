package com.coffeeshop.repository;

import com.coffeeshop.entity.TableEntity;
import com.coffeeshop.enums.TableStatus;
import com.coffeeshop.test.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive unit tests for TableRepository
 */
@DataJpaTest
@ActiveProfiles("test")
class TableRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TableRepository tableRepository;

    private TableEntity testTable;

    @BeforeEach
    void setUp() {
        testTable = TestDataFactory.createTestTable("T01", 4, TableStatus.AVAILABLE);
        entityManager.persistAndFlush(testTable);
    }

    @Test
    @DisplayName("Save table - Success")
    void saveTable_Success() {
        // Given
        TableEntity newTable = TestDataFactory.createTestTable("T02", 2, TableStatus.OCCUPIED);

        // When
        TableEntity savedTable = tableRepository.save(newTable);

        // Then
        assertThat(savedTable.getId()).isNotNull();
        assertThat(savedTable.getTableNumber()).isEqualTo("T02");
        assertThat(savedTable.getCapacity()).isEqualTo(2);
        assertThat(savedTable.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        assertThat(savedTable.getIsActive()).isTrue();
        
        // Verify it was persisted
        Optional<TableEntity> retrievedTable = tableRepository.findById(savedTable.getId());
        assertThat(retrievedTable).isPresent();
        assertThat(retrievedTable.get().getTableNumber()).isEqualTo("T02");
    }

    @Test
    @DisplayName("Find by ID - Success")
    void findById_Success() {
        // When
        Optional<TableEntity> result = tableRepository.findById(testTable.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTableNumber()).isEqualTo("T01");
        assertThat(result.get().getCapacity()).isEqualTo(4);
        assertThat(result.get().getStatus()).isEqualTo(TableStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Find by ID - Not found")
    void findById_NotFound() {
        // When
        Optional<TableEntity> result = tableRepository.findById(99999);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Find all tables - Success")
    void findAllTables_Success() {
        // Given
        TableEntity table2 = TestDataFactory.createTestTable("T02", 6, TableStatus.OCCUPIED);
        TableEntity table3 = TestDataFactory.createTestTable("T03", 2, TableStatus.RESERVED);
        entityManager.persistAndFlush(table2);
        entityManager.persistAndFlush(table3);

        // When
        List<TableEntity> allTables = tableRepository.findAll();

        // Then
        assertThat(allTables).hasSize(3);
        assertThat(allTables).extracting(TableEntity::getTableNumber)
                .containsExactlyInAnyOrder("T01", "T02", "T03");
        assertThat(allTables).extracting(TableEntity::getStatus)
                .containsExactlyInAnyOrder(TableStatus.AVAILABLE, TableStatus.OCCUPIED, TableStatus.RESERVED);
    }

    @Test
    @DisplayName("Update table - Success")
    void updateTable_Success() {
        // Given
        testTable.setTableNumber("T01-UPD");
        testTable.setCapacity(8);
        testTable.setStatus(TableStatus.OCCUPIED);
        testTable.setLocation("Updated Location");

        // When
        TableEntity updatedTable = tableRepository.save(testTable);
        entityManager.flush();

        // Then
        assertThat(updatedTable.getTableNumber()).isEqualTo("T01-UPD");
        assertThat(updatedTable.getCapacity()).isEqualTo(8);
        assertThat(updatedTable.getStatus()).isEqualTo(TableStatus.OCCUPIED);
        assertThat(updatedTable.getLocation()).isEqualTo("Updated Location");
        
        // Verify persistence
        Optional<TableEntity> retrievedTable = tableRepository.findById(testTable.getId());
        assertThat(retrievedTable).isPresent();
        assertThat(retrievedTable.get().getTableNumber()).isEqualTo("T01-UPD");
        assertThat(retrievedTable.get().getCapacity()).isEqualTo(8);
        assertThat(retrievedTable.get().getStatus()).isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    @DisplayName("Delete table - Success")
    void deleteTable_Success() {
        // Given
        Integer tableId = testTable.getId();

        // When
        tableRepository.delete(testTable);
        entityManager.flush();

        // Then
        Optional<TableEntity> result = tableRepository.findById(tableId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Count tables - Success")
    void countTables_Success() {
        // Given
        TableEntity table2 = TestDataFactory.createTestTable("T02", 6, TableStatus.AVAILABLE);
        entityManager.persistAndFlush(table2);

        // When
        long count = tableRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Exists by ID - Success")
    void existsById_Success() {
        // When
        boolean exists = tableRepository.existsById(testTable.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Exists by ID - Not found")
    void existsById_NotFound() {
        // When
        boolean exists = tableRepository.existsById(99999);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Delete by ID - Success")
    void deleteById_Success() {
        // Given
        Integer tableId = testTable.getId();

        // When
        tableRepository.deleteById(tableId);
        entityManager.flush();

        // Then
        Optional<TableEntity> result = tableRepository.findById(tableId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Table entity constraints - Success")
    void tableEntityConstraints_Success() {
        // When
        TableEntity result = tableRepository.findById(testTable.getId()).orElse(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTableNumber()).isNotNull();
        assertThat(result.getCapacity()).isNotNull();
        assertThat(result.getStatus()).isNotNull();
        assertThat(result.getIsActive()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Table status enum values - Success")
    void tableStatusEnumValues_Success() {
        // Given
        TableEntity availableTable = TestDataFactory.createTestTable("T04", 4, TableStatus.AVAILABLE);
        TableEntity occupiedTable = TestDataFactory.createTestTable("T05", 4, TableStatus.OCCUPIED);
        TableEntity reservedTable = TestDataFactory.createTestTable("T06", 4, TableStatus.RESERVED);

        // When
        tableRepository.save(availableTable);
        tableRepository.save(occupiedTable);
        tableRepository.save(reservedTable);
        entityManager.flush();

        // Then
        List<TableEntity> allTables = tableRepository.findAll();
        assertThat(allTables).extracting(TableEntity::getStatus)
                .contains(TableStatus.AVAILABLE, TableStatus.OCCUPIED, TableStatus.RESERVED);
    }
}