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
		return products.stream().map(AdminProductResponseDTO::fromEntity).toList();
	}

	@GetMapping("/available")
	public List<CustomerProductResponseDTO> getAvailableProducts(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "") String sort
	) {
		List<Product> products = productService.getAllProducts().stream()
			.filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
			.toList();
		if (categoryId != null) {
			products = products.stream().filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId)).toList();
		}
		if (search != null && !search.isBlank()) {
			String lower = search.toLowerCase();
			products = products.stream().filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lower)).toList();
		}
		if (sort.equalsIgnoreCase("price,asc")) {
			products = products.stream().sorted((a, b) -> a.getPrice().compareTo(b.getPrice())).toList();
		} else if (sort.equalsIgnoreCase("price,desc")) {
			products = products.stream().sorted((a, b) -> b.getPrice().compareTo(a.getPrice())).toList();
		}
		return products.stream().map(CustomerProductResponseDTO::fromEntity).toList();
	}

	@GetMapping("/category/{categoryId}")
	public List<CustomerProductResponseDTO> getProductsByCategory(@PathVariable Integer categoryId) {
		List<Product> products = productService.getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
			.filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
			.toList();
		return products.stream().map(CustomerProductResponseDTO::fromEntity).toList();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO getProductById(@PathVariable Integer id) {
		Product product = productService.getProductById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));
		return AdminProductResponseDTO.fromEntity(product);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO createProduct(@RequestBody AdminProductRequestDTO request) {
		Product product = productService.saveProductFromDTO(request, null);
		return AdminProductResponseDTO.fromEntity(product);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO updateProduct(@PathVariable Integer id, @RequestBody AdminProductRequestDTO request) {
		Product product = productService.saveProductFromDTO(request, id);
		return AdminProductResponseDTO.fromEntity(product);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteProduct(@PathVariable Integer id) {
		productService.deleteProduct(id);
	}

	@PatchMapping("/{id}/toggle-available")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminProductResponseDTO toggleAvailable(@PathVariable Integer id) {
		Product product = productService.getProductById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));
		product.setIsAvailable(product.getIsAvailable() == null ? true : !product.getIsAvailable());
		return AdminProductResponseDTO.fromEntity(productService.saveProduct(product));
	}

	@PatchMapping("/reorder")
	@PreAuthorize("hasRole('ADMIN')")
	public List<AdminProductResponseDTO> reorderProducts(@RequestBody List<ReorderRequest> reorderList) {
		Map<Integer, Integer> idToOrder = reorderList.stream()
			.collect(java.util.stream.Collectors.toMap(ReorderRequest::getId, ReorderRequest::getDisplayOrder));
		List<Integer> allIds = productService.getAllProducts().stream().map(Product::getId).toList();
		for (Integer id : idToOrder.keySet()) {
			if (!allIds.contains(id)) throw new IllegalArgumentException("ID không hợp lệ trong danh sách reorder!");
		}
		return productService.reorderProducts(idToOrder).stream().map(AdminProductResponseDTO::fromEntity).toList();
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