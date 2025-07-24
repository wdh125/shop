package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Category;
import com.coffeeshop.repository.CategoryRepository;

@Service
public class CategoryService {
	@Autowired
	private CategoryRepository categoryRepository;

	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	public Optional<Category> getCategoryById(Integer id) {
		return categoryRepository.findById(id);
	}

	public Category saveCategory(Category category) {
		// Validate trùng tên (không phân biệt hoa thường)
		categoryRepository.findByName(category.getName())
			.filter(existing -> category.getId() == null || !existing.getId().equals(category.getId()))
			.ifPresent(existing -> {
				throw new IllegalArgumentException("Tên danh mục đã tồn tại!");
			});
		if (category.getId() == null) {
			category.setCreatedAt(java.time.LocalDateTime.now());
		} else if (category.getCreatedAt() == null) {
			category.setCreatedAt(categoryRepository.findById(category.getId())
				.map(Category::getCreatedAt)
				.orElse(java.time.LocalDateTime.now()));
		}
		category.setUpdatedAt(java.time.LocalDateTime.now());
		return categoryRepository.save(category);
	}

	public void deleteCategory(Integer id) {
		categoryRepository.deleteById(id);
	}

	public List<Category> reorderCategories(Map<Integer, Integer> idToOrder) {
		List<Category> all = categoryRepository.findAll();
		for (Category cat : all) {
			if (idToOrder.containsKey(cat.getId())) {
				cat.setDisplayOrder(idToOrder.get(cat.getId()));
				categoryRepository.save(cat);
			}
		}
		return categoryRepository.findAll();
	}
}