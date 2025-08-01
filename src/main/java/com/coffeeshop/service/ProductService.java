package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import com.coffeeshop.entity.Product;
import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import com.coffeeshop.controller.ProductController.ReorderRequest;

/**
 * Product service interface for managing coffee shop products
 */
public interface ProductService {

    /**
     * Get all products
     */
    List<Product> getAllProducts();

    /**
     * Get product by ID
     */
    Optional<Product> getProductById(Integer id);

    /**
     * Save or update product
     */
    Product saveProduct(Product product);

    /**
     * Save product from DTO
     */
    Product saveProductFromDTO(AdminProductRequestDTO dto, Integer id);

    /**
     * Delete product by ID
     */
    void deleteProduct(Integer id);

    /**
     * Reorder products
     */
    List<Product> reorderProducts(Map<Integer, Integer> idToOrder);

    /**
     * Get filtered admin products
     */
    List<AdminProductResponseDTO> getFilteredAdminProducts(Integer categoryId, Boolean isAvailable, String search, String sort);

    /**
     * Get filtered customer products
     */
    List<CustomerProductResponseDTO> getFilteredCustomerProducts(Integer categoryId, String search, String sort);

    /**
     * Get customer products by category
     */
    List<CustomerProductResponseDTO> getCustomerProductsByCategory(Integer categoryId);

    /**
     * Get admin product by ID
     */
    AdminProductResponseDTO getAdminProductById(Integer id);

    /**
     * Create new product
     */
    AdminProductResponseDTO createProduct(AdminProductRequestDTO request);

    /**
     * Update existing product
     */
    AdminProductResponseDTO updateProduct(Integer id, AdminProductRequestDTO request);

    /**
     * Toggle product availability
     */
    AdminProductResponseDTO toggleProductAvailable(Integer id);

    /**
     * Reorder products with request list
     */
    List<AdminProductResponseDTO> reorderProducts(List<ReorderRequest> reorderList);
}