package com.coffeeshop.controller;

import com.coffeeshop.service.CategoryService;
import com.coffeeshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
    @Autowired
    private ProductService productService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminCategoryResponseDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
            .map(c -> {
                List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
                    .map(AdminProductResponseDTO::fromEntity)
                    .toList();
                return AdminCategoryResponseDTO.fromEntity(c, products);
            })
            .toList();
    }

    @GetMapping("/active")
    public List<CustomerCategoryResponseDTO> getActiveCategories() {
        return categoryService.getAllCategories().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .map(c -> CustomerCategoryResponseDTO.fromEntity(c, (int) productService.getAllProducts().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()) && Boolean.TRUE.equals(p.getIsAvailable()))
                .count()))
            .toList();
    }

    @GetMapping("/menu")
    public List<CustomerCategoryResponseDTO> getMenu() {
        return categoryService.getAllCategories().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .map(c -> CustomerCategoryResponseDTO.fromEntity(c, (int) productService.getAllProducts().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()) && Boolean.TRUE.equals(p.getIsAvailable()))
                .count()))
            .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO getCategoryById(@PathVariable Integer id) {
        var c = categoryService.getCategoryById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
        List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
            .map(AdminProductResponseDTO::fromEntity)
            .toList();
        return AdminCategoryResponseDTO.fromEntity(c, products);
    }

    @GetMapping("/{id}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminProductResponseDTO> getProductsByCategory(@PathVariable Integer id) {
        return productService.getAllProducts().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
            .map(AdminProductResponseDTO::fromEntity)
            .toList();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryStatisticsDTO getStatistics() {
        return categoryService.getStatistics();
    }
    public static class StatisticsDTO {
        private final int totalCategories;
        private final int totalProducts;
        private final int avgProductsPerCategory;
        public StatisticsDTO(int totalCategories, int totalProducts, int avgProductsPerCategory) {
            this.totalCategories = totalCategories;
            this.totalProducts = totalProducts;
            this.avgProductsPerCategory = avgProductsPerCategory;
        }
        public int getTotalCategories() { return totalCategories; }
        public int getTotalProducts() { return totalProducts; }
        public int getAvgProductsPerCategory() { return avgProductsPerCategory; }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO createCategory(@RequestBody AdminCategoryRequestDTO dto) {
        var category = categoryService.saveCategory(dto);
        List<AdminProductResponseDTO> products = List.of();
        return AdminCategoryResponseDTO.fromEntity(category, products);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO updateCategory(@PathVariable Integer id, @RequestBody AdminCategoryRequestDTO dto) {
        dto.setId(id);
        var category = categoryService.saveCategory(dto);
        List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
            .map(AdminProductResponseDTO::fromEntity)
            .toList();
        return AdminCategoryResponseDTO.fromEntity(category, products);
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO toggleActive(@PathVariable Integer id) {
        var category = categoryService.toggleActive(id);
        List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
            .map(AdminProductResponseDTO::fromEntity)
            .toList();
        return AdminCategoryResponseDTO.fromEntity(category, products);
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminCategoryResponseDTO> reorderCategories(@RequestBody List<Map<String, Integer>> reorderList) {
        var categories = categoryService.reorderCategories(reorderList);
        return categories.stream()
            .map(c -> {
                List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
                    .map(AdminProductResponseDTO::fromEntity)
                    .toList();
                return AdminCategoryResponseDTO.fromEntity(c, products);
            })
            .toList();
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