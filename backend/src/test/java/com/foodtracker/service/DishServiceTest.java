package com.foodtracker.service;

import com.foodtracker.domain.Dish;
import com.foodtracker.domain.DishItem;
import com.foodtracker.domain.Product;
import com.foodtracker.dto.dish.DishItemRequest;
import com.foodtracker.dto.dish.DishRequest;
import com.foodtracker.dto.dish.DishResponse;
import com.foodtracker.exception.DishNotFoundException;
import com.foodtracker.exception.ProductNotFoundException;
import com.foodtracker.repository.DishRepository;
import com.foodtracker.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DishService dishService;

    @Test
    void createDish_savesAndReturnsResponse() {
        Product product = buildProduct(1L, 1L);
        Dish saved = buildDish(1L, 1L, "Borsch", List.of(buildItem(product, 300)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(dishRepository.save(any())).thenReturn(saved);

        DishResponse result = dishService.createDish(1L, buildRequest("Borsch"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Borsch");
        assertThat(result.getItems()).hasSize(1);
        verify(dishRepository).save(any());
    }

    @Test
    void createDish_productNotFound_throwsException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dishService.createDish(1L, buildRequest("Borsch")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createDish_productPrivateAndNotOwner_throwsException() {
        Product privateProduct = buildProduct(1L, 2L);
        privateProduct.setPublic(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(privateProduct));

        assertThatThrownBy(() -> dishService.createDish(1L, buildRequest("Borsch")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createDish_publicProductFromAnotherUser_succeeds() {
        Product publicProduct = buildProduct(1L, 2L);
        publicProduct.setPublic(true);
        Dish saved = buildDish(1L, 1L, "Borsch", List.of(buildItem(publicProduct, 300)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(publicProduct));
        when(dishRepository.save(any())).thenReturn(saved);

        DishResponse result = dishService.createDish(1L, buildRequest("Borsch"));

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getDish_accessible_returnsResponse() {
        Product product = buildProduct(1L, 1L);
        Dish dish = buildDish(5L, 1L, "Borsch", List.of(buildItem(product, 300)));
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));

        DishResponse result = dishService.getDish(5L, 1L);

        assertThat(result.getName()).isEqualTo("Borsch");
    }

    @Test
    void getDish_publicAndNotOwner_returnsResponse() {
        Product product = buildProduct(1L, 2L);
        Dish dish = buildDish(5L, 2L, "Public Borsch", List.of(buildItem(product, 300)));
        dish.setPublic(true);
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));

        DishResponse result = dishService.getDish(5L, 1L);

        assertThat(result.getName()).isEqualTo("Public Borsch");
    }

    @Test
    void getDish_notFound_throwsException() {
        when(dishRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dishService.getDish(99L, 1L))
                .isInstanceOf(DishNotFoundException.class);
    }

    @Test
    void getDish_privateAndNotOwner_throwsException() {
        Dish dish = buildDish(5L, 2L, "Private", new ArrayList<>());
        dish.setPublic(false);
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));

        assertThatThrownBy(() -> dishService.getDish(5L, 1L))
                .isInstanceOf(DishNotFoundException.class);
    }

    @Test
    void getUserDishes_returnsCombinedList() {
        Product product = buildProduct(1L, 1L);
        when(dishRepository.findByUserIdOrIsPublicTrue(1L)).thenReturn(List.of(
                buildDish(1L, 1L, "Mine", List.of(buildItem(product, 200))),
                buildDish(2L, 2L, "Public", List.of(buildItem(product, 100)))
        ));

        List<DishResponse> result = dishService.getUserDishes(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void updateDish_owner_updatesAndReturns() {
        Product product = buildProduct(1L, 1L);
        Dish dish = buildDish(5L, 1L, "Old Name", new ArrayList<>(List.of(buildItem(product, 200))));
        Dish updated = buildDish(5L, 1L, "New Name", List.of(buildItem(product, 300)));
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(dishRepository.save(any())).thenReturn(updated);

        DishResponse result = dishService.updateDish(5L, 1L, buildRequest("New Name"));

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void updateDish_notOwner_throwsException() {
        Dish dish = buildDish(5L, 2L, "Other", new ArrayList<>());
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));

        assertThatThrownBy(() -> dishService.updateDish(5L, 1L, buildRequest("Updated")))
                .isInstanceOf(DishNotFoundException.class);
    }

    @Test
    void deleteDish_owner_deletesSuccessfully() {
        Dish dish = buildDish(5L, 1L, "ToDelete", new ArrayList<>());
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));

        dishService.deleteDish(5L, 1L);

        verify(dishRepository).delete(dish);
    }

    @Test
    void deleteDish_notOwner_throwsException() {
        Dish dish = buildDish(5L, 2L, "Other", new ArrayList<>());
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));

        assertThatThrownBy(() -> dishService.deleteDish(5L, 1L))
                .isInstanceOf(DishNotFoundException.class);
    }

    @Test
    void caloriesPer100g_computedCorrectly() {
        // 300g of 200 kcal/100g product → 600 kcal total → 200 kcal/100g dish
        Product product = buildProduct(1L, 1L);
        product.setCaloriesPer100g(BigDecimal.valueOf(200));
        Dish saved = buildDish(1L, 1L, "Simple", List.of(buildItem(product, 300)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(dishRepository.save(any())).thenReturn(saved);

        DishResponse result = dishService.createDish(1L, buildRequest("Simple"));

        assertThat(result.getCaloriesPer100g()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    void caloriesPer100g_multipleItems_computedCorrectly() {
        // 100g @ 100 kcal/100g + 200g @ 100 kcal/100g = 300 kcal total / 300g = 100 kcal/100g
        Product product = buildProduct(1L, 1L);
        product.setCaloriesPer100g(BigDecimal.valueOf(100));
        DishItem item1 = buildItem(product, 100);
        DishItem item2 = buildItem(product, 200);
        Dish saved = buildDish(1L, 1L, "Mixed", List.of(item1, item2));
        // totalWeightG = 300

        DishItemRequest itemReq1 = new DishItemRequest();
        itemReq1.setProductId(1L);
        itemReq1.setAmountG(100);

        DishItemRequest itemReq2 = new DishItemRequest();
        itemReq2.setProductId(1L);
        itemReq2.setAmountG(200);

        DishRequest request = new DishRequest();
        request.setName("Mixed");
        request.setIsPublic(false);
        request.setItems(List.of(itemReq1, itemReq2));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(dishRepository.save(any())).thenReturn(saved);

        DishResponse result = dishService.createDish(1L, request);

        assertThat(result.getCaloriesPer100g()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    private DishRequest buildRequest(String name) {
        DishItemRequest itemReq = new DishItemRequest();
        itemReq.setProductId(1L);
        itemReq.setAmountG(300);

        DishRequest r = new DishRequest();
        r.setName(name);
        r.setIsPublic(false);
        r.setItems(List.of(itemReq));
        return r;
    }

    private Product buildProduct(Long id, Long userId) {
        Product p = new Product();
        p.setId(id);
        p.setUserId(userId);
        p.setName("Chicken Breast");
        p.setCaloriesPer100g(BigDecimal.valueOf(200));
        p.setProteinPer100g(BigDecimal.valueOf(31));
        p.setFatPer100g(BigDecimal.valueOf(3.6));
        p.setCarbPer100g(BigDecimal.valueOf(0));
        p.setPublic(false);
        return p;
    }

    private Dish buildDish(Long id, Long userId, String name, List<DishItem> items) {
        Dish d = new Dish();
        d.setId(id);
        d.setUserId(userId);
        d.setName(name);
        d.setPublic(false);
        BigDecimal totalWeight = items.stream()
                .map(item -> BigDecimal.valueOf(item.getAmountG()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        d.setTotalWeightG(totalWeight.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : totalWeight);
        d.setItems(new ArrayList<>(items));
        return d;
    }

    private DishItem buildItem(Product product, int amountG) {
        DishItem item = new DishItem();
        item.setId(1L);
        item.setProduct(product);
        item.setAmountG(amountG);
        return item;
    }
}
