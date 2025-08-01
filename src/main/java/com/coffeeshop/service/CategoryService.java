package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import com.coffeeshop.entity.Category;
import com.coffeeshop.dto.admin.request.AdminCategoryRequestDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryResponseDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryStatisticsDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerCategoryResponseDTO;

/**
 * Category service interface for managing product categories
 */
public interface CategoryService {

    /**
     * Get all categories
     */
    List<Category> getAllCategories();

    /**
     * Get category by ID
     */
    Optional<Category> getCategoryById(Integer id);

    /**
     * Save category from DTO
     */
    Category saveCategory(AdminCategoryRequestDTO dto);

    /**
     * Delete category by ID
     */
    void deleteCategory(Integer id);

    /**
     * Reorder categories
     */
    List<Category> reorderCategories(List<Map<String, Integer>> reorderList);

    /**
     * Toggle category active status
     */
    Category toggleActive(Integer id);

    /**
     * Get category statistics
     */
    AdminCategoryStatisticsDTO getStatistics();

    /**
     * Get all categories for admin view
     */
    List<AdminCategoryResponseDTO> getAllAdminCategoryDTOs();

    /**
     * Get active categories for customer view
     */
    List<CustomerCategoryResponseDTO> getAllActiveCustomerCategoryDTOs();

    /**
     * Get admin category DTO by ID
     */
    AdminCategoryResponseDTO getAdminCategoryDTOById(Integer id);

    /**
     * Get admin products by category
     */
    List<AdminProductResponseDTO> getAdminProductsByCategory(Integer id);

    /**
     * Create new category
     */
    AdminCategoryResponseDTO createCategory(AdminCategoryRequestDTO dto);

    /**
     * Update existing category
     */
    AdminCategoryResponseDTO updateCategory(Integer id, AdminCategoryRequestDTO dto);

    /**
     * Toggle active status and return DTO
     */
    AdminCategoryResponseDTO toggleActiveAndReturnDTO(Integer id);

    /**
     * Reorder categories and return DTOs
     */
    List<AdminCategoryResponseDTO> reorderCategoriesAndReturnDTOs(List<Map<String, Integer>> reorderList);
}