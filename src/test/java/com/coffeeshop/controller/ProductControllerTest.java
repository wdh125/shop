package com.coffeeshop.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.coffeeshop.controller.ProductController.ReorderRequest;
import com.coffeeshop.dto.admin.request.AdminProductRequestDTO;
import com.coffeeshop.dto.admin.response.AdminProductResponseDTO;
import com.coffeeshop.dto.customer.response.CustomerProductResponseDTO;
import com.coffeeshop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private AdminProductRequestDTO productRequest;
    private AdminProductResponseDTO adminProductResponse;
    private CustomerProductResponseDTO customerProductResponse;

    @BeforeEach
    void setUp() {
        productRequest = new AdminProductRequestDTO();
        productRequest.setName("Americano");
        productRequest.setDescription("Classic black coffee");
        productRequest.setPrice(new BigDecimal("50000"));
        productRequest.setCategoryId(1);
        productRequest.setImage("americano.jpg");
        productRequest.setIsAvailable(true);
        productRequest.setDisplayOrder(1);

        adminProductResponse = new AdminProductResponseDTO();
        adminProductResponse.setId(1);
        adminProductResponse.setName("Americano");
        adminProductResponse.setDescription("Classic black coffee");
        adminProductResponse.setPrice(new BigDecimal("50000"));
        adminProductResponse.setCategoryId(1);
        adminProductResponse.setCategoryName("Coffee");
        adminProductResponse.setImage("americano.jpg");
        adminProductResponse.setIsAvailable(true);
        adminProductResponse.setDisplayOrder(1);

        customerProductResponse = new CustomerProductResponseDTO();
        customerProductResponse.setId(1);
        customerProductResponse.setName("Americano");
        customerProductResponse.setDescription("Classic black coffee");
        customerProductResponse.setPrice(new BigDecimal("50000"));
        customerProductResponse.setCategoryName("Coffee");
        customerProductResponse.setImage("americano.jpg");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllProducts_Success() throws Exception {
        List<AdminProductResponseDTO> products = Arrays.asList(adminProductResponse);
        when(productService.getFilteredAdminProducts(anyInt(), anyBoolean(), anyString(), anyString()))
                .thenReturn(products);

        mockMvc.perform(get("/api/products")
                .param("categoryId", "1")
                .param("isAvailable", "true")
                .param("search", "americano")
                .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Americano"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllProducts_Forbidden() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAvailableProducts_Success() throws Exception {
        List<CustomerProductResponseDTO> products = Arrays.asList(customerProductResponse);
        when(productService.getFilteredCustomerProducts(anyInt(), anyString(), anyString()))
                .thenReturn(products);

        mockMvc.perform(get("/api/products/available")
                .param("categoryId", "1")
                .param("search", "americano")
                .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Americano"));
    }

    @Test
    void testGetAvailableProducts_NoParams() throws Exception {
        List<CustomerProductResponseDTO> products = Arrays.asList(customerProductResponse);
        when(productService.getFilteredCustomerProducts(null, null, ""))
                .thenReturn(products);

        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetProductsByCategory_Success() throws Exception {
        List<CustomerProductResponseDTO> products = Arrays.asList(customerProductResponse);
        when(productService.getCustomerProductsByCategory(1)).thenReturn(products);

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetProductsByCategory_EmptyList() throws Exception {
        when(productService.getCustomerProductsByCategory(999)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/products/category/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetProductById_Success() throws Exception {
        when(productService.getAdminProductById(1)).thenReturn(adminProductResponse);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Americano"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetProductById_NotFound() throws Exception {
        when(productService.getAdminProductById(999))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetProductById_Forbidden() throws Exception {
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_Success() throws Exception {
        when(productService.createProduct(any(AdminProductRequestDTO.class))).thenReturn(adminProductResponse);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Americano"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_InvalidData() throws Exception {
        AdminProductRequestDTO invalidRequest = new AdminProductRequestDTO();
        // Missing required fields

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateProduct_InvalidPrice() throws Exception {
        AdminProductRequestDTO invalidRequest = new AdminProductRequestDTO();
        invalidRequest.setName("Test Product");
        invalidRequest.setPrice(new BigDecimal("-100")); // Negative price
        invalidRequest.setCategoryId(1);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateProduct_Forbidden() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProduct_Success() throws Exception {
        when(productService.updateProduct(eq(1), any(AdminProductRequestDTO.class))).thenReturn(adminProductResponse);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateProduct_NotFound() throws Exception {
        when(productService.updateProduct(eq(999), any(AdminProductRequestDTO.class)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(put("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteProduct_NotFound() throws Exception {
        doNothing().when(productService).deleteProduct(999);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isOk()); // Delete should not fail even if not found
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testDeleteProduct_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testToggleAvailable_Success() throws Exception {
        AdminProductResponseDTO toggledProduct = new AdminProductResponseDTO();
        toggledProduct.setId(1);
        toggledProduct.setIsAvailable(false);
        when(productService.toggleProductAvailable(1)).thenReturn(toggledProduct);

        mockMvc.perform(patch("/api/products/1/toggle-available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isAvailable").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReorderProducts_Success() throws Exception {
        List<ReorderRequest> reorderList = Arrays.asList(
                createReorderRequest(1, 2),
                createReorderRequest(2, 1)
        );

        List<AdminProductResponseDTO> reorderedProducts = Arrays.asList(adminProductResponse);
        when(productService.reorderProducts(any())).thenReturn(reorderedProducts);

        mockMvc.perform(patch("/api/products/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReorderProducts_EmptyList() throws Exception {
        List<ReorderRequest> emptyList = Arrays.asList();

        mockMvc.perform(patch("/api/products/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyList)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testReorderProducts_Forbidden() throws Exception {
        List<ReorderRequest> reorderList = Arrays.asList(createReorderRequest(1, 2));

        mockMvc.perform(patch("/api/products/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderList)))
                .andExpect(status().isForbidden());
    }

    private ReorderRequest createReorderRequest(Integer id, Integer displayOrder) {
        ReorderRequest request = new ReorderRequest();
        request.setId(id);
        request.setDisplayOrder(displayOrder);
        return request;
    }
}