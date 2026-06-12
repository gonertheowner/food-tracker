package com.foodtracker.service;

import com.foodtracker.domain.Product;
import com.foodtracker.dto.product.ProductRequest;
import com.foodtracker.dto.product.ProductResponse;
import com.foodtracker.exception.ProductNotFoundException;
import com.foodtracker.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_savesAndReturnsResponse() {
        ProductRequest request = buildRequest("Chicken Breast");
        Product saved = buildEntity(1L, 1L, "Chicken Breast");
        when(productRepository.save(any())).thenReturn(saved);

        ProductResponse result = productService.createProduct(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Chicken Breast");
        verify(productRepository).save(any());
    }

    @Test
    void getProduct_success_returnsResponse() {
        Product product = buildEntity(5L, 1L, "Apple");
        product.setPublic(true);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProduct(5L, 1L);

        assertThat(result.getName()).isEqualTo("Apple");
    }

    @Test
    void getProduct_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L, 1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getProduct_privateAndNotOwner_throwsException() {
        Product product = buildEntity(5L, 2L, "Private");
        product.setPublic(false);
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.getProduct(5L, 1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getUserProducts_returnsCombinedList() {
        when(productRepository.findByUserIdOrIsPublicTrue(1L))
                .thenReturn(List.of(buildEntity(1L, 1L, "Mine"), buildEntity(2L, null, "Public")));

        List<ProductResponse> result = productService.getUserProducts(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void updateProduct_notOwner_throwsException() {
        Product product = buildEntity(5L, 2L, "Other");
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.updateProduct(5L, 1L, buildRequest("New Name")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void updateProduct_owner_updatesAndReturns() {
        Product product = buildEntity(5L, 1L, "Old Name");
        Product updated = buildEntity(5L, 1L, "New Name");
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(updated);

        ProductResponse result = productService.updateProduct(5L, 1L, buildRequest("New Name"));

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void deleteProduct_owner_deletesSuccessfully() {
        Product product = buildEntity(5L, 1L, "ToDelete");
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        productService.deleteProduct(5L, 1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_notOwner_throwsException() {
        Product product = buildEntity(5L, 2L, "Other");
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteProduct(5L, 1L))
                .isInstanceOf(ProductNotFoundException.class);
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

    private Product buildEntity(Long id, Long userId, String name) {
        Product p = new Product();
        p.setId(id);
        p.setUserId(userId);
        p.setName(name);
        p.setCaloriesPer100g(BigDecimal.valueOf(200));
        p.setProteinPer100g(BigDecimal.valueOf(30));
        p.setFatPer100g(BigDecimal.valueOf(5));
        p.setCarbPer100g(BigDecimal.valueOf(0));
        return p;
    }
}
