package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Category;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.dto.admin.request.AdminCategoryRequestDTO;
import com.coffeeshop.dto.admin.response.AdminCategoryStatisticsDTO;

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
		category.setUpdatedAt(java.time.LocalDateTime.now());
		if (category.getId() == null) category.setCreatedAt(java.time.LocalDateTime.now());
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
			category.setUpdatedAt(java.time.LocalDateTime.now());
			categoryRepository.save(category);
		}
		return getAllCategories();
	}

	public Category toggleActive(Integer id) {
		Category category = getCategoryById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
		category.setIsActive(category.getIsActive() == null ? true : !category.getIsActive());
		category.setUpdatedAt(java.time.LocalDateTime.now());
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
}