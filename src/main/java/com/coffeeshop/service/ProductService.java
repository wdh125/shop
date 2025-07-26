package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Product;
import com.coffeeshop.entity.Category;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;

@Service
public class ProductService {
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	public Optional<Product> getProductById(Integer id) {
		return productRepository.findById(id);
	}

	public Product saveProduct(Product product) {
		// Validate trùng tên (không phân biệt hoa thường)
		productRepository.findByName(product.getName())
			.filter(existing -> product.getId() == null || !existing.getId().equals(product.getId()))
			.ifPresent(existing -> {
				throw new IllegalArgumentException("Tên sản phẩm đã tồn tại!");
			});
		// Validate giá > 0
		if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
			throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0!");
		}
		if (product.getId() == null) {
			product.setCreatedAt(java.time.LocalDateTime.now());
		} else if (product.getCreatedAt() == null) {
			product.setCreatedAt(productRepository.findById(product.getId())
				.map(Product::getCreatedAt)
				.orElse(java.time.LocalDateTime.now()));
		}
		product.setUpdatedAt(java.time.LocalDateTime.now());
		return productRepository.save(product);
	}

	public Product saveProductFromDTO(AdminProductRequestDTO dto, Integer id) {
		Product product = id != null ? getProductById(id).orElse(new Product()) : new Product();
		product.setName(dto.getName());
		product.setDescription(dto.getDescription());
		product.setPrice(dto.getPrice());
		product.setImageUrl(dto.getImageUrl());
		product.setIsAvailable(dto.getIsAvailable());
		product.setPreparationTime(dto.getPreparationTime());
		product.setDisplayOrder(dto.getDisplayOrder());
		
		// Set category if categoryId is provided
		if (dto.getCategoryId() != null) {
			Category category = categoryRepository.findById(dto.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + dto.getCategoryId()));
			product.setCategory(category);
		}
		
		return saveProduct(product);
	}

	public void deleteProduct(Integer id) {
		productRepository.deleteById(id);
	}

	public List<Product> reorderProducts(Map<Integer, Integer> idToOrder) {
		List<Product> all = productRepository.findAll();
		for (Product p : all) {
			if (idToOrder.containsKey(p.getId())) {
				p.setDisplayOrder(idToOrder.get(p.getId()));
				productRepository.save(p);
			}
		}
		return productRepository.findAll();
	}
}