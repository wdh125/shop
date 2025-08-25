package com.coffeeshop.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import com.coffeeshop.service.FileStorageService;
import com.coffeeshop.service.ProductService;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import org.springframework.security.access.prepost.PreAuthorize;

// Controller xử lý các API quản lý sản phẩm
@RestController
@RequestMapping("/api/products")
public class ProductController {
	@Autowired
	private ProductService productService;
	@Autowired
	private FileStorageService fileStorageService;

	// Lấy danh sách sản phẩm cho admin (có filter và search)
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

	// Lấy danh sách sản phẩm có sẵn cho customer
	@GetMapping("/available")
	public List<CustomerProductResponseDTO> getAvailableProducts(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "") String sort
	) {
		return productService.getFilteredCustomerProducts(categoryId, search, sort);
	}

	// Lấy sản phẩm theo danh mục
	@GetMapping("/category/{categoryId}")
	public List<CustomerProductResponseDTO> getProductsByCategory(@PathVariable Integer categoryId) {
		return productService.getCustomerProductsByCategory(categoryId);
	}

	// Lấy chi tiết sản phẩm cho admin
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO getProductById(@PathVariable Integer id) {
		return productService.getAdminProductById(id);
	}

	// Public endpoint để xem chi tiết sản phẩm - không cần đăng nhập
	@GetMapping("/public/{id}")
	public CustomerProductResponseDTO getPublicProductById(@PathVariable Integer id) {
		return productService.getCustomerProductById(id);
	}

	// Tạo sản phẩm mới (admin only)
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO createProduct(
		@Valid @ModelAttribute AdminProductRequestDTO request,
		@RequestParam(value = "image", required = false) MultipartFile image
	) {
		if (image != null && !image.isEmpty()) {
			try {
				String imageUrl = fileStorageService.saveFile(image);
				request.setImageUrl(imageUrl);
			} catch (IOException e) {
				throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage());
			}
		}
		return productService.createProduct(request);
	}

	// Cập nhật sản phẩm (admin only)
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO updateProduct(
		@PathVariable Integer id,
		@Valid @ModelAttribute AdminProductRequestDTO request,
		@RequestParam(value = "image", required = false) MultipartFile image
	) {
		if (image != null && !image.isEmpty()) {
			try {
				String imageUrl = fileStorageService.saveFile(image);
				request.setImageUrl(imageUrl);
			} catch (IOException e) {
				throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage());
			}
		}
		return productService.updateProduct(id, request);
	}

	// Xóa sản phẩm (admin only)
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