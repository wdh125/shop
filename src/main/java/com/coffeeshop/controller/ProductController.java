package com.coffeeshop.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coffeeshop.entity.Product;
import com.coffeeshop.service.ProductService;
import com.coffeeshop.dto.admin.response.ProductDTO;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	@Autowired
	private ProductService productService;

	@GetMapping
	public List<ProductDTO> getAllProducts(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) Boolean isAvailable,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "") String sort
	) {
		List<Product> products = productService.getAllProducts();
		// Filter theo categoryId
		if (categoryId != null) {
			products = products.stream().filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId)).toList();
		}
		// Filter theo isAvailable
		if (isAvailable != null) {
			products = products.stream().filter(p -> isAvailable.equals(p.getIsAvailable())).toList();
		}
		// Search theo tên
		if (search != null && !search.isBlank()) {
			String lower = search.toLowerCase();
			products = products.stream().filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lower)).toList();
		}
		// Sort theo giá
		if (sort.equalsIgnoreCase("price,asc")) {
			products = products.stream().sorted((a, b) -> a.getPrice().compareTo(b.getPrice())).toList();
		} else if (sort.equalsIgnoreCase("price,desc")) {
			products = products.stream().sorted((a, b) -> b.getPrice().compareTo(a.getPrice())).toList();
		}
		return products.stream().map(ProductDTO::fromEntity).toList();
	}

	@GetMapping("/available")
	public List<ProductDTO> getAvailableProducts() {
		return productService.getAllProducts().stream()
			.filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
			.map(ProductDTO::fromEntity)
			.toList();
	}

	@GetMapping("/{id}")
	public ProductDTO getProductById(@PathVariable Integer id) {
		Product product = productService.getProductById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));
		return ProductDTO.fromEntity(product);
	}

	@PostMapping
	public ProductDTO createProduct(@RequestBody Product product) {
		return ProductDTO.fromEntity(productService.saveProduct(product));
	}

	@PutMapping("/{id}")
	public ProductDTO updateProduct(@PathVariable Integer id, @RequestBody Product product) {
		product.setId(id);
		return ProductDTO.fromEntity(productService.saveProduct(product));
	}

	@DeleteMapping("/{id}")
	public void deleteProduct(@PathVariable Integer id) {
		productService.deleteProduct(id);
	}

	@PatchMapping("/{id}/toggle-available")
	public ProductDTO toggleAvailable(@PathVariable Integer id) {
		Product product = productService.getProductById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));
		product.setIsAvailable(product.getIsAvailable() == null ? true : !product.getIsAvailable());
		return ProductDTO.fromEntity(productService.saveProduct(product));
	}

	@PatchMapping("/reorder")
	public List<ProductDTO> reorderProducts(@RequestBody List<ReorderRequest> reorderList) {
		Map<Integer, Integer> idToOrder = reorderList.stream()
			.collect(java.util.stream.Collectors.toMap(ReorderRequest::getId, ReorderRequest::getDisplayOrder));
		List<Integer> allIds = productService.getAllProducts().stream().map(Product::getId).toList();
		for (Integer id : idToOrder.keySet()) {
			if (!allIds.contains(id)) throw new IllegalArgumentException("ID không hợp lệ trong danh sách reorder!");
		}
		return productService.reorderProducts(idToOrder).stream().map(ProductDTO::fromEntity).toList();
	}
	public static class ReorderRequest {
		private Integer id;
		private Integer displayOrder;
		public Integer getId() { return id; }
		public void setId(Integer id) { this.id = id; }
		public Integer getDisplayOrder() { return displayOrder; }
		public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
	}
}