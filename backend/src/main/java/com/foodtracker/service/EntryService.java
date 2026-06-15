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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class EntryService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final EntryRepository entryRepository;
    private final ProductRepository productRepository;
    private final DishRepository dishRepository;

    public EntryResponse createEntry(Long userId, EntryRequest request) {
        Entry entry = new Entry();
        entry.setUserId(userId);
        entry.setMealType(request.getMealType());
        entry.setSourceType(request.getSourceType());
        entry.setAmountG(request.getAmountG());
        entry.setEatenAt(request.getEatenAt() != null ? request.getEatenAt() : OffsetDateTime.now(ZoneOffset.UTC));

        if (request.getSourceType() == SourceType.PRODUCT) {
            if (request.getProductId() == null) {
                throw new InvalidRequestException("productId is required when sourceType is PRODUCT");
            }
            Product product = findAccessibleProduct(request.getProductId(), userId);
            entry.setProductId(product.getId());
            applyProductSnapshot(entry, product, request.getAmountG());
        } else {
            if (request.getDishId() == null) {
                throw new InvalidRequestException("dishId is required when sourceType is DISH");
            }
            Dish dish = findAccessibleDish(request.getDishId(), userId);
            entry.setDishId(dish.getId());
            applyDishSnapshot(entry, dish, request.getAmountG());
        }

        return toResponse(entryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public EntryResponse getEntry(Long id, Long userId) {
        return toResponse(findOwned(id, userId));
    }

    @Transactional(readOnly = true)
    public List<EntryResponse> getEntriesByDate(Long userId, LocalDate date) {
        OffsetDateTime from = date.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime to = from.plusDays(1).minusNanos(1);
        return entryRepository.findByUserIdAndEatenAtBetween(userId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    public EntryResponse updateEntry(Long id, Long userId, EntryUpdateRequest request) {
        Entry entry = findOwned(id, userId);
        entry.setMealType(request.getMealType());
        entry.setAmountG(request.getAmountG());
        if (request.getEatenAt() != null) {
            entry.setEatenAt(request.getEatenAt());
        }

        if (entry.getSourceType() == SourceType.PRODUCT) {
            Product product = findAccessibleProduct(entry.getProductId(), userId);
            applyProductSnapshot(entry, product, request.getAmountG());
        } else {
            Dish dish = findAccessibleDish(entry.getDishId(), userId);
            applyDishSnapshot(entry, dish, request.getAmountG());
        }

        return toResponse(entryRepository.save(entry));
    }

    public void deleteEntry(Long id, Long userId) {
        entryRepository.delete(findOwned(id, userId));
    }

    // --- private helpers ---

    private Entry findOwned(Long id, Long userId) {
        Entry entry = entryRepository.findById(id)
                .orElseThrow(() -> new EntryNotFoundException(id));
        if (!Objects.equals(entry.getUserId(), userId)) {
            throw new EntryNotFoundException(id);
        }
        return entry;
    }

    private Product findAccessibleProduct(Long productId, Long userId) {
        return productRepository.findById(productId)
                .filter(p -> p.isPublic() || p.getUserId() == null || Objects.equals(p.getUserId(), userId))
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Dish findAccessibleDish(Long dishId, Long userId) {
        return dishRepository.findById(dishId)
                .filter(d -> d.isPublic() || Objects.equals(d.getUserId(), userId))
                .orElseThrow(() -> new DishNotFoundException(dishId));
    }

    private void applyProductSnapshot(Entry entry, Product product, int amountG) {
        BigDecimal amount = BigDecimal.valueOf(amountG);
        entry.setCalories(product.getCaloriesPer100g().multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        entry.setProteinG(product.getProteinPer100g().multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        entry.setFatG(product.getFatPer100g().multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        entry.setCarbG(product.getCarbPer100g().multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
    }

    private void applyDishSnapshot(Entry entry, Dish dish, int amountG) {
        List<DishItem> items = dish.getItems();
        BigDecimal totalWeight = dish.getTotalWeightG();
        BigDecimal amount = BigDecimal.valueOf(amountG);

        if (items.isEmpty() || totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            entry.setCalories(BigDecimal.ZERO.setScale(2));
            entry.setProteinG(BigDecimal.ZERO.setScale(2));
            entry.setFatG(BigDecimal.ZERO.setScale(2));
            entry.setCarbG(BigDecimal.ZERO.setScale(2));
            return;
        }

        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal carb = BigDecimal.ZERO;

        for (DishItem item : items) {
            Product p = item.getProduct();
            BigDecimal w = BigDecimal.valueOf(item.getAmountG());
            calories = calories.add(p.getCaloriesPer100g().multiply(w));
            protein = protein.add(p.getProteinPer100g().multiply(w));
            fat = fat.add(p.getFatPer100g().multiply(w));
            carb = carb.add(p.getCarbPer100g().multiply(w));
        }

        // per100g of dish * amountG / 100
        entry.setCalories(calories.divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        entry.setProteinG(protein.divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        entry.setFatG(fat.divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        entry.setCarbG(carb.divide(totalWeight, 10, RoundingMode.HALF_UP).multiply(amount).divide(HUNDRED, 2, RoundingMode.HALF_UP));
    }

    private EntryResponse toResponse(Entry entry) {
        String productName = null;
        String dishName = null;
        if (entry.getProductId() != null) {
            productName = productRepository.findById(entry.getProductId())
                    .map(Product::getName).orElse(null);
        }
        if (entry.getDishId() != null) {
            dishName = dishRepository.findById(entry.getDishId())
                    .map(Dish::getName).orElse(null);
        }
        return EntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .eatenAt(entry.getEatenAt())
                .mealType(entry.getMealType())
                .sourceType(entry.getSourceType())
                .productId(entry.getProductId())
                .productName(productName)
                .dishId(entry.getDishId())
                .dishName(dishName)
                .amountG(entry.getAmountG())
                .calories(entry.getCalories())
                .proteinG(entry.getProteinG())
                .fatG(entry.getFatG())
                .carbG(entry.getCarbG())
                .build();
    }
}
