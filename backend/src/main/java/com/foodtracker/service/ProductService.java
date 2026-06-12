package com.foodtracker.service;

import com.foodtracker.domain.Product;
import com.foodtracker.dto.product.ProductRequest;
import com.foodtracker.dto.product.ProductResponse;
import com.foodtracker.exception.ProductNotFoundException;
import com.foodtracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(Long userId, ProductRequest request) {
        Product product = new Product();
        product.setUserId(userId);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id, Long userId) {
        Product product = findAccessible(id, userId);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getUserProducts(Long userId) {
        return productRepository.findByUserIdOrIsPublicTrue(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse updateProduct(Long id, Long userId, ProductRequest request) {
        Product product = findOwned(id, userId);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id, Long userId) {
        Product product = findOwned(id, userId);
        productRepository.delete(product);
    }

    // --- private helpers ---

    private Product findAccessible(Long id, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (!product.isPublic() && !Objects.equals(product.getUserId(), userId)) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    private Product findOwned(Long id, Long userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        if (!Objects.equals(product.getUserId(), userId)) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCaloriesPer100g(request.getCaloriesPer100g());
        product.setProteinPer100g(request.getProteinPer100g());
        product.setFatPer100g(request.getFatPer100g());
        product.setCarbPer100g(request.getCarbPer100g());
        product.setPublic(Boolean.TRUE.equals(request.getIsPublic()));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .userId(product.getUserId())
                .name(product.getName())
                .brand(product.getBrand())
                .caloriesPer100g(product.getCaloriesPer100g())
                .proteinPer100g(product.getProteinPer100g())
                .fatPer100g(product.getFatPer100g())
                .carbPer100g(product.getCarbPer100g())
                .isPublic(product.isPublic())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
