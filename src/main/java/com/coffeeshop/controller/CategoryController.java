package com.coffeeshop.controller;

import com.coffeeshop.entity.Category;
import com.coffeeshop.service.CategoryService;
import com.coffeeshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.coffeeshop.entity.Product;
import java.util.Map;
import com.coffeeshop.dto.CategoryDTO;
import com.coffeeshop.dto.ProductDTO;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
            .map(c -> CategoryDTO.fromEntity(c, productService.getAllProducts().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
                .map(p -> new CategoryDTO.ProductInfo(p.getId(), p.getName(), p.getPrice(), p.getIsAvailable()))
                .toList()))
            .toList();
    }

    @GetMapping("/active")
    public List<CategoryDTO> getActiveCategories() {
        return categoryService.getAllCategories().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .map(c -> CategoryDTO.fromEntity(c, productService.getAllProducts().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
                .map(p -> new CategoryDTO.ProductInfo(p.getId(), p.getName(), p.getPrice(), p.getIsAvailable()))
                .toList()))
            .toList();
    }

    @GetMapping("/menu")
    public List<MenuCategoryDTO> getMenu() {
        List<Category> categories = categoryService.getAllCategories().stream()
            .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
            .toList();
        List<Product> products = productService.getAllProducts().stream()
            .filter(p -> Boolean.TRUE.equals(p.getIsAvailable()))
            .toList();
        return categories.stream().map(cat -> {
            List<ProductDTO> catProducts = products.stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(cat.getId()))
                .map(ProductDTO::fromEntity)
                .toList();
            return new MenuCategoryDTO(cat, catProducts);
        }).toList();
    }

    @GetMapping("/{id}")
    public CategoryDTO getCategoryDetail(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
        List<CategoryDTO.ProductInfo> products = productService.getAllProducts().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
            .map(p -> new CategoryDTO.ProductInfo(p.getId(), p.getName(), p.getPrice(), p.getIsAvailable()))
            .toList();
        return CategoryDTO.fromEntity(category, products);
    }

    @GetMapping("/{id}/products")
    public List<ProductDTO> getProductsByCategory(
            @PathVariable Integer id,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false, defaultValue = "") String sort
    ) {
        List<ProductDTO> products = productService.getAllProducts().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(id))
            .filter(p -> isAvailable == null || isAvailable.equals(p.getIsAvailable()))
            .map(ProductDTO::fromEntity)
            .toList();
        if (sort.equalsIgnoreCase("price,asc")) {
            products = products.stream().sorted((a, b) -> a.getPrice().compareTo(b.getPrice())).toList();
        } else if (sort.equalsIgnoreCase("price,desc")) {
            products = products.stream().sorted((a, b) -> b.getPrice().compareTo(a.getPrice())).toList();
        }
        return products;
    }

    @GetMapping("/statistics")
    public StatisticsDTO getStatistics() {
        List<Category> categories = categoryService.getAllCategories();
        List<Product> products = productService.getAllProducts();
        int totalCategories = categories.size();
        int totalProducts = products.size();
        int avgProductsPerCategory = totalCategories == 0 ? 0 : (int) Math.round((double) totalProducts / totalCategories);
        return new StatisticsDTO(totalCategories, totalProducts, avgProductsPerCategory);
    }
    public static class StatisticsDTO {
        private final int totalCategories;
        private final int totalProducts;
        private final int avgProductsPerCategory;
        public StatisticsDTO(int totalCategories, int totalProducts, int avgProductsPerCategory) {
            this.totalCategories = totalCategories;
            this.totalProducts = totalProducts;
            this.avgProductsPerCategory = avgProductsPerCategory;
        }
        public int getTotalCategories() { return totalCategories; }
        public int getTotalProducts() { return totalProducts; }
        public int getAvgProductsPerCategory() { return avgProductsPerCategory; }
    }

    @PostMapping
    public CategoryDTO createCategory(@RequestBody Category category) {
        return CategoryDTO.fromEntity(categoryService.saveCategory(category), List.of());
    }

    @PutMapping("/{id}")
    public CategoryDTO updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        return CategoryDTO.fromEntity(categoryService.saveCategory(category), List.of());
    }

    @PatchMapping("/{id}/toggle-active")
    public CategoryDTO toggleActive(@PathVariable Integer id) {
        Category category = categoryService.getCategoryById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));
        category.setIsActive(category.getIsActive() == null ? true : !category.getIsActive());
        return CategoryDTO.fromEntity(categoryService.saveCategory(category), List.of());
    }

    @PatchMapping("/reorder")
    public List<CategoryDTO> reorderCategories(@RequestBody List<ReorderRequest> reorderList) {
        Map<Integer, Integer> idToOrder = reorderList.stream()
            .collect(java.util.stream.Collectors.toMap(ReorderRequest::getId, ReorderRequest::getDisplayOrder));
        List<Integer> allIds = categoryService.getAllCategories().stream().map(Category::getId).toList();
        for (Integer id : idToOrder.keySet()) {
            if (!allIds.contains(id)) throw new IllegalArgumentException("ID không hợp lệ trong danh sách reorder!");
        }
        return categoryService.reorderCategories(idToOrder).stream()
            .map(c -> CategoryDTO.fromEntity(c, productService.getAllProducts().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(c.getId()))
                .map(p -> new CategoryDTO.ProductInfo(p.getId(), p.getName(), p.getPrice(), p.getIsAvailable()))
                .toList()))
            .toList();
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

class CategoryDetailDTO {
    private final Category category;
    private final long productCount;
    public CategoryDetailDTO(Category category, long productCount) {
        this.category = category;
        this.productCount = productCount;
    }
    public Category getCategory() { return category; }
    public long getProductCount() { return productCount; }
}

class MenuCategoryDTO {
    private final Category category;
    private final List<ProductDTO> products;
    public MenuCategoryDTO(Category category, List<ProductDTO> products) {
        this.category = category;
        this.products = products;
    }
    public Category getCategory() { return category; }
    public List<ProductDTO> getProducts() { return products; }
}