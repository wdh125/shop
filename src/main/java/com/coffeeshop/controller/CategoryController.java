package com.coffeeshop.controller;

import com.coffeeshop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import com.coffeeshop.dto.admin.response.AdminCategoryResponseDTO;
import com.coffeeshop.dto.admin.request.AdminCategoryRequestDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryStatisticsDTO;
import com.coffeeshop.dto.customer.response.CustomerCategoryResponseDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminCategoryResponseDTO> getAllCategories() {
        return categoryService.getAllAdminCategoryDTOs();
    }

    @GetMapping("/active")
    public List<CustomerCategoryResponseDTO> getActiveCategories() {
        return categoryService.getAllActiveCustomerCategoryDTOs();
    }

    @GetMapping("/menu")
    public List<CustomerCategoryResponseDTO> getMenu() {
        return categoryService.getAllActiveCustomerCategoryDTOs();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO getCategoryById(@PathVariable Integer id) {
        return categoryService.getAdminCategoryDTOById(id);
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProductResponseDTO> getProductsByCategory(@PathVariable Integer id) {
        return categoryService.getAdminProductsByCategory(id);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryStatisticsDTO getStatistics() {
        return categoryService.getStatistics();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO createCategory(@Valid @RequestBody AdminCategoryRequestDTO dto) {
        return categoryService.createCategory(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO updateCategory(@PathVariable Integer id, @Valid @RequestBody AdminCategoryRequestDTO dto) {
        return categoryService.updateCategory(id, dto);
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO toggleActive(@PathVariable Integer id) {
        return categoryService.toggleActiveAndReturnDTO(id);
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminCategoryResponseDTO> reorderCategories(@RequestBody List<Map<String, Integer>> reorderList) {
        return categoryService.reorderCategoriesAndReturnDTOs(reorderList);
    }

    public static class ReorderRequest {
        private Integer id;
        private Integer displayOrder;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
}