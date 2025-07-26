package com.coffeeshop.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.coffeeshop.service.ProductService;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	@Autowired
	private ProductService productService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<AdminProductResponseDTO> getAllProducts(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) Boolean isAvailable,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "") String sort
	) {
		return productService.getFilteredAdminProducts(categoryId, isAvailable, search, sort);
	}

	@GetMapping("/available")
	public List<CustomerProductResponseDTO> getAvailableProducts(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "") String sort
	) {
		return productService.getFilteredCustomerProducts(categoryId, search, sort);
	}

	@GetMapping("/category/{categoryId}")
	public List<CustomerProductResponseDTO> getProductsByCategory(@PathVariable Integer categoryId) {
		return productService.getCustomerProductsByCategory(categoryId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO getProductById(@PathVariable Integer id) {
		return productService.getAdminProductById(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO createProduct(@Valid @RequestBody AdminProductRequestDTO request) {
		return productService.createProduct(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO updateProduct(@PathVariable Integer id, @Valid @RequestBody AdminProductRequestDTO request) {
		return productService.updateProduct(id, request);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteProduct(@PathVariable Integer id) {
		productService.deleteProduct(id);
	}

	@PatchMapping("/{id}/toggle-available")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO toggleAvailable(@PathVariable Integer id) {
		return productService.toggleProductAvailable(id);
	}

	@PatchMapping("/reorder")
	@PreAuthorize("hasRole('ADMIN')")
	public List<AdminProductResponseDTO> reorderProducts(@Valid @RequestBody List<ReorderRequest> reorderList) {
		return productService.reorderProducts(reorderList);
	}

	// Inner class for reorder request
	public static class ReorderRequest {
		private Integer id;
		private Integer displayOrder;

		// Constructor mặc định
		public ReorderRequest() {}

		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }
		public Integer getDisplayOrder() { return displayOrder; }
		public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
	}
}