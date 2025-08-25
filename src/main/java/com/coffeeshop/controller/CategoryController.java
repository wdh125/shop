package com.coffeeshop.controller;

import com.coffeeshop.service.CategoryService;
import com.coffeeshop.service.FileStorageService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
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
    private FileStorageService fileStorageService;

    // ---------- PUBLIC ENDPOINTS FOR CUSTOMER ----------
    // Get all active categories (for customer)
    @GetMapping("/active")
    public List<CustomerCategoryResponseDTO> getActiveCategories() {
        return categoryService.getAllActiveCustomerCategoryDTOs();
    }

    // Get menu categories (for customer)
    @GetMapping("/menu")
    public List<CustomerCategoryResponseDTO> getMenu() {
        return categoryService.getAllActiveCustomerCategoryDTOs();
    }

    // Get single active category detail (for customer)
    @GetMapping("/active/{id}")
    public CustomerCategoryResponseDTO getActiveCategoryById(@PathVariable Integer id) {
        return categoryService.getActiveCustomerCategoryDTOById(id);
    }

    // ---------- ADMIN ENDPOINTS ----------
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<AdminCategoryResponseDTO> getAllCategories() {
        return categoryService.getAllAdminCategoryDTOs();
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO getCategoryById(@PathVariable Integer id) {
        return categoryService.getAdminCategoryDTOById(id);
    }

    @GetMapping("/{id}/products")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<AdminProductResponseDTO> getProductsByCategory(@PathVariable Integer id) {
        return categoryService.getAdminProductsByCategory(id);
    }

    @GetMapping("/statistics")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryStatisticsDTO getStatistics() {
        return categoryService.getStatistics();
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO createCategory(
        @Valid @ModelAttribute AdminCategoryRequestDTO dto,
        @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        System.out.println("Creating category: " + dto.getName() + ", Active: " + dto.getIsActive());
        
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = fileStorageService.saveFile(image);
                dto.setImageUrl(imageUrl); 
            } catch (IOException e) {
                throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage());
            }
        }
        return categoryService.createCategory(dto);
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO updateCategory(
        @PathVariable Integer id,
        @Valid @ModelAttribute AdminCategoryRequestDTO dto,
        @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = fileStorageService.saveFile(image);
                dto.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage());
            }
        }
        return categoryService.updateCategory(id, dto);
    }

    @PatchMapping("/{id}/toggle-active")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public AdminCategoryResponseDTO toggleActive(@PathVariable Integer id) {
        return categoryService.toggleActiveAndReturnDTO(id);
    }

    @PatchMapping("/reorder")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public List<AdminCategoryResponseDTO> reorderCategories(@RequestBody List<Map<String, Integer>> reorderList) {
        return categoryService.reorderCategoriesAndReturnDTOs(reorderList);
    }

    // DTO for reorder request (if needed)
    public static class ReorderRequest {
        private Integer id;
        private Integer displayOrder;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }
}