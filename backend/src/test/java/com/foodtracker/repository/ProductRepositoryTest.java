package com.foodtracker.repository;

import com.foodtracker.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryTest extends AbstractDbTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void saveAndFindById() {
        Product product = buildProduct(null, "Apple", false);
        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByUserId_returnsOnlyUserProducts() {
        productRepository.save(buildProduct(10L, "User Product", false));
        productRepository.save(buildProduct(20L, "Other User Product", false));

        List<Product> result = productRepository.findByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("User Product");
    }

    @Test
    void findByIsPublicTrue_returnsOnlyPublicProducts() {
        productRepository.save(buildProduct(10L, "Private", false));
        productRepository.save(buildProduct(null, "Public System", true));

        List<Product> result = productRepository.findByIsPublicTrue();

        assertThat(result).allMatch(Product::isPublic);
        assertThat(result).anyMatch(p -> p.getName().equals("Public System"));
    }

    @Test
    void findByUserIdOrIsPublicTrue_returnsCombined() {
        productRepository.save(buildProduct(10L, "Mine", false));
        productRepository.save(buildProduct(20L, "Theirs Private", false));
        productRepository.save(buildProduct(null, "Public", true));

        List<Product> result = productRepository.findByUserIdOrIsPublicTrue(10L);

        assertThat(result).extracting(Product::getName)
                .containsExactlyInAnyOrder("Mine", "Public");
    }

    @Test
    void deleteProduct_removesFromDb() {
        Product saved = productRepository.save(buildProduct(10L, "ToDelete", false));

        productRepository.delete(saved);

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    private Product buildProduct(Long userId, String name, boolean isPublic) {
        Product p = new Product();
        p.setUserId(userId);
        p.setName(name);
        p.setCaloriesPer100g(BigDecimal.valueOf(100));
        p.setProteinPer100g(BigDecimal.valueOf(5));
        p.setFatPer100g(BigDecimal.valueOf(2));
        p.setCarbPer100g(BigDecimal.valueOf(20));
        p.setPublic(isPublic);
        return p;
    }
}
