package com.coffeeshop.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Category;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.dto.admin.request.AdminCategoryRequestDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryResponseDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryStatisticsDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerCategoryResponseDTO;
import com.coffeeshop.service.CategoryService;
import com.coffeeshop.service.ProductService;

@Service
public class CategoryServiceImpl implements CategoryService {
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProductService productService;

	@Override
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	public Optional<Category> getCategoryById(Integer id) {
		return categoryRepository.findById(id);
	}

	@Override
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

	@Override
	public void deleteCategory(Integer id) {
		categoryRepository.deleteById(id);
	}

	@Override
	public List<Category> reorderCategories(List<Map<String, Integer>> reorderList) {
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

	@Override
	public Category toggleActive(Integer id) {
		Category category = getCategoryById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
		category.setIsActive(category.getIsActive() == null ? true : !category.getIsActive());
		category.setUpdatedAt(LocalDateTime.now());
		return categoryRepository.save(category);
	}

	@Override
	public AdminCategoryStatisticsDTO getStatistics() {
		var categories = getAllCategories();
		var products = productService.getAllProducts();
		int totalCategories = categories.size();
		int totalProducts = products.size();
		int avgProductsPerCategory = totalCategories == 0 ? 0 : (int) Math.round((double) totalProducts / totalCategories);
		return new AdminCategoryStatisticsDTO(totalCategories, totalProducts, avgProductsPerCategory);
	}

	@Override
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

	@Override
	public List<CustomerCategoryResponseDTO> getAllActiveCustomerCategoryDTOs() {
		return getAllCategories().stream()
			.filter(c -> Boolean.TRUE.equals(c.getIsActive()))
			.map(c -> CustomerCategoryResponseDTO.fromEntity(c, (int) productService.getAllProducts().stream()
				.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()) && Boolean.TRUE.equals(p.getIsAvailable()))
				.count()))
			.toList();
	}

	@Override
	public AdminCategoryResponseDTO getAdminCategoryDTOById(Integer id) {
		Category category = getCategoryById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
		
		List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(category.getId()))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
		
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	@Override
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

	@Override
	public AdminCategoryResponseDTO createCategory(AdminCategoryRequestDTO dto) {
		Category category = saveCategory(dto);
		List<AdminProductResponseDTO> products = List.of(); // New category has no products yet
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	@Override
	public AdminCategoryResponseDTO updateCategory(Integer id, AdminCategoryRequestDTO dto) {
		dto.setId(id);
		Category category = saveCategory(dto);
		
		List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
		
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	@Override
	public AdminCategoryResponseDTO toggleActiveAndReturnDTO(Integer id) {
		Category category = toggleActive(id);
		
		List<AdminProductResponseDTO> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(category.getId()))
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
		
		return AdminCategoryResponseDTO.fromEntity(category, products);
	}

	@Override
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