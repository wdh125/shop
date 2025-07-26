package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Category;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.dto.admin.request.AdminCategoryRequestDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryResponseDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryStatisticsDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerCategoryResponseDTO;
import java.time.LocalDateTime;

@Service
public class CategoryService {
	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductService productService;

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	public Optional<Category> getCategoryById(Integer id) {
		return categoryRepository.findById(id);
	}

	public Category saveCategory(AdminCategoryRequestDTO dto) {
		Category category = dto.getId() != null ? getCategoryById(dto.getId()).orElse(new Category()) : new Category();
		category.setName(dto.getName());
		category.setDescription(dto.getDescription());
		category.setIsActive(dto.getIsActive());
		category.setDisplayOrder(dto.getDisplayOrder());
		category.setUpdatedAt(LocalDateTime.now());
		if (category.getId() == null) category.setCreatedAt(LocalDateTime.now());
		return categoryRepository.save(category);
	}

	public void deleteCategory(Integer id) {
		categoryRepository.deleteById(id);
	}

	public List<Category> reorderCategories(java.util.List<Map<String, Integer>> reorderList) {
		for (Map<String, Integer> item : reorderList) {
			Integer id = item.get("id");
			Integer displayOrder = item.get("displayOrder");
			Category category = getCategoryById(id).orElseThrow(() -> new IllegalArgumentException("ID không hợp lệ trong danh sách reorder!"));
			category.setDisplayOrder(displayOrder);
			category.setUpdatedAt(LocalDateTime.now());
			categoryRepository.save(category);
		}
		return getAllCategories();
	}

	public Category toggleActive(Integer id) {
		Category category = getCategoryById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
		category.setIsActive(category.getIsActive() == null ? true : !category.getIsActive());
		category.setUpdatedAt(LocalDateTime.now());
		return categoryRepository.save(category);
	}

	public AdminCategoryStatisticsDTO getStatistics() {
		var categories = getAllCategories();
		var products = productService.getAllProducts();
		int totalCategories = categories.size();
		int totalProducts = products.size();
		int avgProductsPerCategory = totalCategories == 0 ? 0 : (int) Math.round((double) totalProducts / totalCategories);
		return new AdminCategoryStatisticsDTO(totalCategories, totalProducts, avgProductsPerCategory);
	}

	// New methods for DTO mapping and business logic
	public List<AdminCategoryResponseDTO> getAllAdminCategoryDTOs() {
		return getAllCategories().stream()
			.map(c -> {
				List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
					.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
					.map(AdminProductResponseDTO::fromEntity)
					.toList();
				return AdminCategoryResponseDTO.fromEntity(c, products);
			})
			.toList();
	}

	public List<CustomerCategoryResponseDTO> getAllActiveCustomerCategoryDTOs() {
		return getAllCategories().stream()
			.filter(c -> Boolean.TRUE.equals(c.getIsActive()))
			.map(c -> CustomerCategoryResponseDTO.fromEntity(c, (int) productService.getAllProducts().stream()
				.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()) && Boolean.TRUE.equals(p.getIsAvailable()))
				.count()))
			.toList();
	}

	public AdminCategoryResponseDTO getAdminCategoryDTOById(Integer id) {
		Category category = getCategoryById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
		
		List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(category.getId()))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
		
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	public List<AdminProductResponseDTO> getAdminProductsByCategory(Integer id) {
		// Validate category exists
		if (!getCategoryById(id).isPresent()) {
			throw new IllegalArgumentException("Không tìm thấy danh mục!");
		}
		
		return productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
	}

	public AdminCategoryResponseDTO createCategory(AdminCategoryRequestDTO dto) {
		Category category = saveCategory(dto);
		List<AdminProductResponseDTO> products = List.of(); // New category has no products yet
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	public AdminCategoryResponseDTO updateCategory(Integer id, AdminCategoryRequestDTO dto) {
		dto.setId(id);
		Category category = saveCategory(dto);
		
		List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
		
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	public AdminCategoryResponseDTO toggleActiveAndReturnDTO(Integer id) {
		Category category = toggleActive(id);
		
		List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(category.getId()))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
		
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	public List<AdminCategoryResponseDTO> reorderCategoriesAndReturnDTOs(List<Map<String, Integer>> reorderList) {
		List<Category> categories = reorderCategories(reorderList);
		
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
}