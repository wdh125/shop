package com.coffeeshop.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Product;
import com.coffeeshop.repository.ProductRepository;

@Service
public class ProductService {
	@Autowired
	private ProductRepository productRepository;

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