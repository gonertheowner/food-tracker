package com.foodtracker.controller;

import com.foodtracker.dto.product.ProductRequest;
import com.foodtracker.dto.product.ProductResponse;
import com.foodtracker.exception.ProductNotFoundException;
import com.foodtracker.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest extends AbstractControllerTest {

    @MockBean
    private ProductService productService;

    @Test
    void createProduct_validRequest_returns201WithBody() throws Exception {
        ProductRequest request = buildRequest("Chicken Breast");
        ProductResponse response = buildResponse(1L, "Chicken Breast");
        when(productService.createProduct(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chicken Breast"))
                .andExpect(jsonPath("$.caloriesPer100g").exists())
                .andExpect(jsonPath("$.proteinPer100g").exists());
    }

    @Test
    void createProduct_blankName_returns400WithFieldErrors() throws Exception {
        ProductRequest request = buildRequest("");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.fieldErrors[0].message").exists());
    }

    @Test
    void createProduct_missingNutrition_returns400WithFieldErrors() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Valid Name");
        // caloriesPer100g is null — should fail @NotNull

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getProducts_returns200WithArray() throws Exception {
        when(productService.getUserProducts(anyLong()))
                .thenReturn(List.of(buildResponse(1L, "Apple"), buildResponse(2L, "Banana")));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getProduct_existingId_returns200WithBody() throws Exception {
        when(productService.getProduct(anyLong(), anyLong()))
                .thenReturn(buildResponse(1L, "Apple"));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    void getProduct_unknownId_returns404() throws Exception {
        when(productService.getProduct(anyLong(), anyLong()))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateProduct_validRequest_returns200WithBody() throws Exception {
        ProductRequest request = buildRequest("Updated Chicken");
        ProductResponse response = buildResponse(1L, "Updated Chicken");
        when(productService.updateProduct(anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Chicken"));
    }

    @Test
    void deleteProduct_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_unknownId_returns404() throws Exception {
        doThrow(new ProductNotFoundException(999L))
                .when(productService).deleteProduct(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    private ProductRequest buildRequest(String name) {
        ProductRequest r = new ProductRequest();
        r.setName(name);
        r.setCaloriesPer100g(BigDecimal.valueOf(200));
        r.setProteinPer100g(BigDecimal.valueOf(30));
        r.setFatPer100g(BigDecimal.valueOf(5));
        r.setCarbPer100g(BigDecimal.valueOf(0));
        r.setIsPublic(Boolean.FALSE);
        return r;
    }

    private ProductResponse buildResponse(Long id, String name) {
        return ProductResponse.builder()
                .id(id)
                .userId(1L)
                .name(name)
                .caloriesPer100g(BigDecimal.valueOf(200))
                .proteinPer100g(BigDecimal.valueOf(30))
                .fatPer100g(BigDecimal.valueOf(5))
                .carbPer100g(BigDecimal.valueOf(0))
                .isPublic(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
