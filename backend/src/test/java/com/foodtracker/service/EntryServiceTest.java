package com.foodtracker.service;

import com.foodtracker.domain.*;
import com.foodtracker.dto.entry.EntryRequest;
import com.foodtracker.dto.entry.EntryResponse;
import com.foodtracker.dto.entry.EntryUpdateRequest;
import com.foodtracker.exception.DishNotFoundException;
import com.foodtracker.exception.EntryNotFoundException;
import com.foodtracker.exception.InvalidRequestException;
import com.foodtracker.exception.ProductNotFoundException;
import com.foodtracker.repository.DishRepository;
import com.foodtracker.repository.EntryRepository;
import com.foodtracker.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private EntryService entryService;

    // --- create with PRODUCT ---

    @Test
    void createEntry_productSource_savesAndReturnsResponse() {
        Product product = buildProduct(1L, 1L, 200, 20, 10, 5);
        Entry saved = buildProductEntry(1L, 1L, product.getId(), 200);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(entryRepository.save(any())).thenReturn(saved);

        EntryResponse result = entryService.createEntry(1L, buildProductRequest(1L, 200, MealType.LUNCH));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSourceType()).isEqualTo(SourceType.PRODUCT);
        assertThat(result.getAmountG()).isEqualTo(200);
        // calories = 200 kcal/100g * 200g / 100 = 400 kcal
        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(400));
        verify(entryRepository).save(any());
    }

    @Test
    void createEntry_productSource_snapshotCalculatedCorrectly() {
        // 150g of product with 200 kcal/100g, 30g protein/100g, 8g fat/100g, 2g carb/100g
        // Expected: calories=300, protein=45, fat=12, carb=3
        Product product = buildProduct(1L, 1L, 200, 30, 8, 2);
        Entry saved = buildProductEntry(1L, 1L, product.getId(), 150);
        saved.setCalories(BigDecimal.valueOf(300));
        saved.setProteinG(BigDecimal.valueOf(45));
        saved.setFatG(BigDecimal.valueOf(12));
        saved.setCarbG(BigDecimal.valueOf(3));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(entryRepository.save(any())).thenAnswer(inv -> {
            Entry e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EntryResponse result = entryService.createEntry(1L, buildProductRequest(1L, 150, MealType.BREAKFAST));

        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(result.getProteinG()).isEqualByComparingTo(BigDecimal.valueOf(45.00));
        assertThat(result.getFatG()).isEqualByComparingTo(BigDecimal.valueOf(12.00));
        assertThat(result.getCarbG()).isEqualByComparingTo(BigDecimal.valueOf(3.00));
    }

    @Test
    void createEntry_productSource_nullProductId_throwsInvalidRequest() {
        EntryRequest request = new EntryRequest();
        request.setSourceType(SourceType.PRODUCT);
        request.setProductId(null);
        request.setAmountG(100);
        request.setMealType(MealType.LUNCH);

        assertThatThrownBy(() -> entryService.createEntry(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createEntry_productNotFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entryService.createEntry(1L, buildProductRequest(99L, 100, MealType.LUNCH)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createEntry_privateProductFromAnotherUser_throwsProductNotFoundException() {
        Product privateProduct = buildProduct(1L, 2L, 200, 20, 10, 5);
        privateProduct.setPublic(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(privateProduct));

        assertThatThrownBy(() -> entryService.createEntry(1L, buildProductRequest(1L, 100, MealType.LUNCH)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createEntry_publicProductFromAnotherUser_succeeds() {
        Product publicProduct = buildProduct(1L, 2L, 200, 20, 10, 5);
        publicProduct.setPublic(true);
        Entry saved = buildProductEntry(1L, 1L, publicProduct.getId(), 100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(publicProduct));
        when(entryRepository.save(any())).thenReturn(saved);

        EntryResponse result = entryService.createEntry(1L, buildProductRequest(1L, 100, MealType.LUNCH));

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createEntry_systemProduct_nullUserId_succeeds() {
        Product systemProduct = buildProduct(1L, null, 200, 20, 10, 5);
        systemProduct.setPublic(false);
        Entry saved = buildProductEntry(1L, 1L, systemProduct.getId(), 100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(systemProduct));
        when(entryRepository.save(any())).thenReturn(saved);

        EntryResponse result = entryService.createEntry(1L, buildProductRequest(1L, 100, MealType.SNACK));

        assertThat(result.getId()).isEqualTo(1L);
    }

    // --- create with DISH ---

    @Test
    void createEntry_dishSource_savesAndReturnsResponse() {
        Dish dish = buildDishWithItems(1L, 1L);
        Entry saved = buildDishEntry(1L, 1L, dish.getId(), 150);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
        when(entryRepository.save(any())).thenReturn(saved);

        EntryResponse result = entryService.createEntry(1L, buildDishRequest(1L, 150, MealType.DINNER));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSourceType()).isEqualTo(SourceType.DISH);
        assertThat(result.getAmountG()).isEqualTo(150);
        verify(entryRepository).save(any());
    }

    @Test
    void createEntry_dishSource_snapshotCalculatedCorrectly() {
        // Dish: 200g of product (200 kcal/100g) → dish has 200 kcal/100g
        // Entry: 150g of dish → calories = 200 * 150 / 100 = 300
        Dish dish = buildDishWithItems(1L, 1L);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(dish));
        when(entryRepository.save(any())).thenAnswer(inv -> {
            Entry e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        EntryResponse result = entryService.createEntry(1L, buildDishRequest(1L, 150, MealType.DINNER));

        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
    }

    @Test
    void createEntry_dishSource_nullDishId_throwsInvalidRequest() {
        EntryRequest request = new EntryRequest();
        request.setSourceType(SourceType.DISH);
        request.setDishId(null);
        request.setAmountG(100);
        request.setMealType(MealType.LUNCH);

        assertThatThrownBy(() -> entryService.createEntry(1L, request))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void createEntry_dishNotFound_throwsDishNotFoundException() {
        when(dishRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entryService.createEntry(1L, buildDishRequest(99L, 100, MealType.DINNER)))
                .isInstanceOf(DishNotFoundException.class);
    }

    @Test
    void createEntry_privateDishFromAnotherUser_throwsDishNotFoundException() {
        Dish privateDish = buildDishWithItems(1L, 2L);
        privateDish.setPublic(false);
        when(dishRepository.findById(1L)).thenReturn(Optional.of(privateDish));

        assertThatThrownBy(() -> entryService.createEntry(1L, buildDishRequest(1L, 100, MealType.DINNER)))
                .isInstanceOf(DishNotFoundException.class);
    }

    // --- get single ---

    @Test
    void getEntry_ownedEntry_returnsResponse() {
        Product product = buildProduct(1L, 1L, 200, 20, 10, 5);
        Entry entry = buildProductEntry(5L, 1L, 1L, 200);
        when(entryRepository.findById(5L)).thenReturn(Optional.of(entry));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        EntryResponse result = entryService.getEntry(5L, 1L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getAmountG()).isEqualTo(200);
    }

    @Test
    void getEntry_notFound_throwsEntryNotFoundException() {
        when(entryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> entryService.getEntry(99L, 1L))
                .isInstanceOf(EntryNotFoundException.class);
    }

    @Test
    void getEntry_notOwner_throwsEntryNotFoundException() {
        Entry entry = buildProductEntry(5L, 2L, 1L, 200);
        when(entryRepository.findById(5L)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> entryService.getEntry(5L, 1L))
                .isInstanceOf(EntryNotFoundException.class);
    }

    // --- list by date ---

    @Test
    void getEntriesByDate_returnsFilteredList() {
        Product product = buildProduct(1L, 1L, 200, 20, 10, 5);
        Entry e1 = buildProductEntry(1L, 1L, 1L, 100);
        Entry e2 = buildProductEntry(2L, 1L, 1L, 200);
        when(entryRepository.findByUserIdAndEatenAtBetween(any(), any(), any()))
                .thenReturn(List.of(e1, e2));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        List<EntryResponse> result = entryService.getEntriesByDate(1L, LocalDate.of(2024, 1, 15));

        assertThat(result).hasSize(2);
    }

    @Test
    void getEntriesByDate_noEntries_returnsEmptyList() {
        when(entryRepository.findByUserIdAndEatenAtBetween(any(), any(), any()))
                .thenReturn(List.of());

        List<EntryResponse> result = entryService.getEntriesByDate(1L, LocalDate.of(2024, 1, 15));

        assertThat(result).isEmpty();
    }

    // --- update ---

    @Test
    void updateEntry_owner_recalculatesAndReturns() {
        Product product = buildProduct(1L, 1L, 200, 20, 10, 5);
        Entry entry = buildProductEntry(5L, 1L, 1L, 200);
        when(entryRepository.findById(5L)).thenReturn(Optional.of(entry));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(entryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EntryUpdateRequest request = new EntryUpdateRequest();
        request.setAmountG(300);
        request.setMealType(MealType.DINNER);

        EntryResponse result = entryService.updateEntry(5L, 1L, request);

        assertThat(result.getAmountG()).isEqualTo(300);
        assertThat(result.getMealType()).isEqualTo(MealType.DINNER);
        // calories recalculated: 200 kcal/100g * 300g / 100 = 600
        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(600.00));
    }

    @Test
    void updateEntry_notFound_throwsEntryNotFoundException() {
        when(entryRepository.findById(99L)).thenReturn(Optional.empty());

        EntryUpdateRequest request = new EntryUpdateRequest();
        request.setAmountG(100);
        request.setMealType(MealType.LUNCH);

        assertThatThrownBy(() -> entryService.updateEntry(99L, 1L, request))
                .isInstanceOf(EntryNotFoundException.class);
    }

    @Test
    void updateEntry_notOwner_throwsEntryNotFoundException() {
        Entry entry = buildProductEntry(5L, 2L, 1L, 200);
        when(entryRepository.findById(5L)).thenReturn(Optional.of(entry));

        EntryUpdateRequest request = new EntryUpdateRequest();
        request.setAmountG(100);
        request.setMealType(MealType.LUNCH);

        assertThatThrownBy(() -> entryService.updateEntry(5L, 1L, request))
                .isInstanceOf(EntryNotFoundException.class);
    }

    // --- delete ---

    @Test
    void deleteEntry_owner_deletesSuccessfully() {
        Entry entry = buildProductEntry(5L, 1L, 1L, 200);
        when(entryRepository.findById(5L)).thenReturn(Optional.of(entry));

        entryService.deleteEntry(5L, 1L);

        verify(entryRepository).delete(entry);
    }

    @Test
    void deleteEntry_notOwner_throwsEntryNotFoundException() {
        Entry entry = buildProductEntry(5L, 2L, 1L, 200);
        when(entryRepository.findById(5L)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> entryService.deleteEntry(5L, 1L))
                .isInstanceOf(EntryNotFoundException.class);
    }

    // --- builders ---

    private EntryRequest buildProductRequest(Long productId, int amountG, MealType mealType) {
        EntryRequest r = new EntryRequest();
        r.setSourceType(SourceType.PRODUCT);
        r.setProductId(productId);
        r.setAmountG(amountG);
        r.setMealType(mealType);
        r.setEatenAt(OffsetDateTime.now());
        return r;
    }

    private EntryRequest buildDishRequest(Long dishId, int amountG, MealType mealType) {
        EntryRequest r = new EntryRequest();
        r.setSourceType(SourceType.DISH);
        r.setDishId(dishId);
        r.setAmountG(amountG);
        r.setMealType(mealType);
        r.setEatenAt(OffsetDateTime.now());
        return r;
    }

    private Product buildProduct(Long id, Long userId, int calories, int protein, int fat, int carb) {
        Product p = new Product();
        p.setId(id);
        p.setUserId(userId);
        p.setName("Test Product");
        p.setCaloriesPer100g(BigDecimal.valueOf(calories));
        p.setProteinPer100g(BigDecimal.valueOf(protein));
        p.setFatPer100g(BigDecimal.valueOf(fat));
        p.setCarbPer100g(BigDecimal.valueOf(carb));
        p.setPublic(false);
        return p;
    }

    private Dish buildDishWithItems(Long id, Long userId) {
        Product product = buildProduct(10L, userId, 200, 20, 10, 5);

        DishItem item = new DishItem();
        item.setProduct(product);
        item.setAmountG(200);

        Dish dish = new Dish();
        dish.setId(id);
        dish.setUserId(userId);
        dish.setName("Test Dish");
        dish.setPublic(false);
        dish.setTotalWeightG(BigDecimal.valueOf(200));
        dish.getItems().add(item);
        return dish;
    }

    private Entry buildProductEntry(Long id, Long userId, Long productId, int amountG) {
        Entry e = new Entry();
        e.setId(id);
        e.setUserId(userId);
        e.setSourceType(SourceType.PRODUCT);
        e.setProductId(productId);
        e.setMealType(MealType.LUNCH);
        e.setAmountG(amountG);
        e.setEatenAt(OffsetDateTime.now(ZoneOffset.UTC));
        e.setCalories(BigDecimal.valueOf(400));
        e.setProteinG(BigDecimal.valueOf(40));
        e.setFatG(BigDecimal.valueOf(20));
        e.setCarbG(BigDecimal.valueOf(10));
        return e;
    }

    private Entry buildDishEntry(Long id, Long userId, Long dishId, int amountG) {
        Entry e = new Entry();
        e.setId(id);
        e.setUserId(userId);
        e.setSourceType(SourceType.DISH);
        e.setDishId(dishId);
        e.setMealType(MealType.DINNER);
        e.setAmountG(amountG);
        e.setEatenAt(OffsetDateTime.now(ZoneOffset.UTC));
        e.setCalories(BigDecimal.valueOf(300));
        e.setProteinG(BigDecimal.valueOf(30));
        e.setFatG(BigDecimal.valueOf(15));
        e.setCarbG(BigDecimal.valueOf(7.50));
        return e;
    }
}
