package com.coffeeshop.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.coffeeshop.entity.Product;
import com.coffeeshop.entity.Category;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import com.coffeeshop.controller.ProductController.ReorderRequest;
import com.coffeeshop.service.ProductService;
import java.time.LocalDateTime;

// Service implementation xử lý logic nghiệp vụ sản phẩm
@Service
public class ProductServiceImpl implements ProductService {
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Override
	public Optional<Product> getProductById(Integer id) {
		return productRepository.findById(id);
	}

	@Override
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
		
		// Validate thời gian chế biến > 0 nếu có
		if (product.getPreparationTime() != null && product.getPreparationTime() <= 0) {
			throw new IllegalArgumentException("Thời gian chế biến phải lớn hơn 0!");
		}
		if (product.getId() == null) {
			product.setCreatedAt(LocalDateTime.now());
		} else if (product.getCreatedAt() == null) {
			product.setCreatedAt(productRepository.findById(product.getId())
				.map(Product::getCreatedAt)
				.orElse(LocalDateTime.now()));
		}
		product.setUpdatedAt(LocalDateTime.now());
		return productRepository.save(product);
	}

	@Override
	public Product saveProductFromDTO(AdminProductRequestDTO dto, Integer id) {
		Product product = id != null ? getProductById(id).orElse(new Product()) : new Product();
		
		// Cập nhật các trường bắt buộc từ DTO
		product.setName(dto.getName());
		product.setPrice(dto.getPrice());
		
		// Xử lý isAvailable: nếu là update và DTO null thì giữ nguyên giá trị cũ
		if (dto.getIsAvailable() != null) {
			product.setIsAvailable(dto.getIsAvailable());
		} else if (id == null) {
			// Nếu là tạo mới và không có giá trị thì set default
			product.setIsAvailable(true);
		}
		
		// Các trường có thể null - cập nhật từ DTO, nếu DTO null thì giữ nguyên giá trị cũ
		if (dto.getDescription() != null) {
			product.setDescription(dto.getDescription());
		}
		// Nếu update sản phẩm và có image mới thì cập nhật, không thì giữ nguyên
		if (dto.getImageUrl() != null) {
			product.setImageUrl(dto.getImageUrl());
		}
		if (dto.getPreparationTime() != null) {
			product.setPreparationTime(dto.getPreparationTime());
		}
		if (dto.getDisplayOrder() != null) {
			product.setDisplayOrder(dto.getDisplayOrder());
		}
		
		// Bắt buộc phải có category
		if (dto.getCategoryId() == null) {
			throw new IllegalArgumentException("Category ID không được để trống! Mọi sản phẩm đều phải thuộc về một danh mục.");
		}
		
		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + dto.getCategoryId()));
		product.setCategory(category);
		
		return saveProduct(product);
	}

	@Override
	public void deleteProduct(Integer id) {
		Optional<Product> productOpt = productRepository.findById(id);
		if (productOpt.isPresent()) {
			Product product = productOpt.get();
			product.setIsAvailable(false);
			product.setUpdatedAt(LocalDateTime.now());
			productRepository.save(product);
		}
	}

	@Override
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

	@Override
	public List<AdminProductResponseDTO> getFilteredAdminProducts(Integer categoryId, Boolean isAvailable, String search, String sort) {
		List<Product> products = getAllProducts();
		
		// Filter theo categoryId
		if (categoryId != null) {
			products = products.stream()
				.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
				.toList();
		}
		
		// Filter theo isAvailable
		if (isAvailable != null) {
			products = products.stream()
				.filter(p -> isAvailable.equals(p.getIsAvailable()))
				.toList();
		}
		
		// Search theo tên
		if (search != null && !search.isBlank()) {
			String lower = search.toLowerCase();
			products = products.stream()
				.filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lower))
				.toList();
		}
		
		// Sort theo giá
		if (sort.equalsIgnoreCase("price,asc")) {
			products = products.stream()
				.sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
				.toList();
		} else if (sort.equalsIgnoreCase("price,desc")) {
			products = products.stream()
				.sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
				.toList();
		}
		
		return products.stream()
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
	}

	@Override
	public List<CustomerProductResponseDTO> getFilteredCustomerProducts(Integer categoryId, String search, String sort) {
		List<Product> products = getAllProducts().stream()
			.filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
			.toList();
			
		if (categoryId != null) {
			products = products.stream()
				.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
				.toList();
		}
		
		if (search != null && !search.isBlank()) {
			String lower = search.toLowerCase();
			products = products.stream()
				.filter(p -> p.getName() != null && p.getName().toLowerCase().contains(lower))
				.toList();
		}
		
		if (sort.equalsIgnoreCase("price,asc")) {
			products = products.stream()
				.sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
				.toList();
		} else if (sort.equalsIgnoreCase("price,desc")) {
			products = products.stream()
				.sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
				.toList();
		}
		
		return products.stream()
			.map(CustomerProductResponseDTO::fromEntity)
			.toList();
	}

	@Override
	public List<CustomerProductResponseDTO> getCustomerProductsByCategory(Integer categoryId) {
		return getAllProducts().stream()
			.filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
			.filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
			.map(CustomerProductResponseDTO::fromEntity)
			.toList();
	}

	@Override
	public AdminProductResponseDTO getAdminProductById(Integer id) {
		return getProductById(id)
			.map(AdminProductResponseDTO::fromEntity)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));
	}

	@Override
	public CustomerProductResponseDTO getCustomerProductById(Integer id) {
		return getProductById(id)
			.filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
			.map(CustomerProductResponseDTO::fromEntity)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm hoặc sản phẩm không khả dụng!"));
	}

	@Override
	public AdminProductResponseDTO createProduct(AdminProductRequestDTO request) {
		Product product = saveProductFromDTO(request, null);
		return AdminProductResponseDTO.fromEntity(product);
	}

	@Override
	public AdminProductResponseDTO updateProduct(Integer id, AdminProductRequestDTO request) {
		Product product = saveProductFromDTO(request, id);
		return AdminProductResponseDTO.fromEntity(product);
	}

	@Override
	public AdminProductResponseDTO toggleProductAvailable(Integer id) {
		Product product = getProductById(id)
			.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));
		product.setIsAvailable(product.getIsAvailable() == null ? true : !product.getIsAvailable());
		return AdminProductResponseDTO.fromEntity(saveProduct(product));
	}

	@Override
	public List<AdminProductResponseDTO> reorderProducts(List<ReorderRequest> reorderList) {
		Map<Integer, Integer> idToOrder = reorderList.stream()
			.collect(Collectors.toMap(ReorderRequest::getId, ReorderRequest::getDisplayOrder));
		
		List<Integer> allIds = getAllProducts().stream().map(Product::getId).toList();
		for (Integer id : idToOrder.keySet()) {
			if (!allIds.contains(id)) {
				throw new IllegalArgumentException("ID không hợp lệ trong danh sách reorder!");
			}
		}
		
		return reorderProducts(idToOrder).stream()
			.map(AdminProductResponseDTO::fromEntity)
			.toList();
	}
}