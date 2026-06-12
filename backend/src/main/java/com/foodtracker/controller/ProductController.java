package com.foodtracker.controller;

import com.foodtracker.dto.product.ProductRequest;
import com.foodtracker.dto.product.ProductResponse;
import com.foodtracker.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // TODO: replace with authenticated user id once JWT auth is implemented
    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(CURRENT_USER_ID, request);
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productService.getUserProducts(CURRENT_USER_ID);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.getProduct(id, CURRENT_USER_ID);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.updateProduct(id, CURRENT_USER_ID, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.deleteProduct(id, CURRENT_USER_ID);
    }
}
