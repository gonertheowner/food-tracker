package com.foodtracker.service;

import com.foodtracker.domain.Dish;
import com.foodtracker.domain.DishItem;
import com.foodtracker.domain.Product;
import com.foodtracker.dto.dish.DishItemRequest;
import com.foodtracker.dto.dish.DishItemResponse;
import com.foodtracker.dto.dish.DishRequest;
import com.foodtracker.dto.dish.DishResponse;
import com.foodtracker.exception.DishNotFoundException;
import com.foodtracker.exception.ProductNotFoundException;
import com.foodtracker.repository.DishRepository;
import com.foodtracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class DishService {

    private final DishRepository dishRepository;
    private final ProductRepository productRepository;

    public DishResponse createDish(Long userId, DishRequest request) {
        Dish dish = new Dish();
        dish.setUserId(userId);
        applyRequest(dish, userId, request);
        return toResponse(dishRepository.save(dish));
    }

    @Transactional(readOnly = true)
    public DishResponse getDish(Long id, Long userId) {
        return toResponse(findAccessible(id, userId));
    }

    @Transactional(readOnly = true)
    public List<DishResponse> getUserDishes(Long userId) {
        return dishRepository.findByUserIdOrIsPublicTrue(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DishResponse updateDish(Long id, Long userId, DishRequest request) {
        Dish dish = findOwned(id, userId);
        applyRequest(dish, userId, request);
        return toResponse(dishRepository.save(dish));
    }

    public void deleteDish(Long id, Long userId) {
        dishRepository.delete(findOwned(id, userId));
    }

    // --- private helpers ---

    private void applyRequest(Dish dish, Long userId, DishRequest request) {
        dish.setName(request.getName());
        dish.setPublic(Boolean.TRUE.equals(request.getIsPublic()));

        List<DishItem> newItems = request.getItems().stream()
                .map(itemReq -> buildItem(dish, userId, itemReq))
                .toList();

        dish.getItems().clear();
        dish.getItems().addAll(newItems);

        BigDecimal totalWeight = newItems.stream()
                .map(i -> BigDecimal.valueOf(i.getAmountG()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dish.setTotalWeightG(totalWeight);
    }

    private DishItem buildItem(Dish dish, Long userId, DishItemRequest itemReq) {
        Product product = productRepository.findById(itemReq.getProductId())
                .filter(p -> p.isPublic() || Objects.equals(p.getUserId(), userId))
                .orElseThrow(() -> new ProductNotFoundException(itemReq.getProductId()));

        DishItem item = new DishItem();
        item.setDish(dish);
        item.setProduct(product);
        item.setAmountG(itemReq.getAmountG());
        return item;
    }

    private Dish findAccessible(Long id, Long userId) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new DishNotFoundException(id));
        if (!dish.isPublic() && !Objects.equals(dish.getUserId(), userId)) {
            throw new DishNotFoundException(id);
        }
        return dish;
    }

    private Dish findOwned(Long id, Long userId) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new DishNotFoundException(id));
        if (!Objects.equals(dish.getUserId(), userId)) {
            throw new DishNotFoundException(id);
        }
        return dish;
    }

    private DishResponse toResponse(Dish dish) {
        List<DishItemResponse> itemResponses = dish.getItems().stream()
                .map(item -> DishItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .amountG(item.getAmountG())
                        .build())
                .toList();

        BigDecimal[] macros = computeMacrosPer100g(dish.getItems(), dish.getTotalWeightG());

        return DishResponse.builder()
                .id(dish.getId())
                .userId(dish.getUserId())
                .name(dish.getName())
                .isPublic(dish.isPublic())
                .totalWeightG(dish.getTotalWeightG())
                .items(itemResponses)
                .caloriesPer100g(macros[0])
                .proteinPer100g(macros[1])
                .fatPer100g(macros[2])
                .carbPer100g(macros[3])
                .createdAt(dish.getCreatedAt())
                .build();
    }

    private BigDecimal[] computeMacrosPer100g(List<DishItem> items, BigDecimal totalWeightG) {
        if (items.isEmpty() || totalWeightG.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        }

        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal carb = BigDecimal.ZERO;

        for (DishItem item : items) {
            Product p = item.getProduct();
            BigDecimal weight = BigDecimal.valueOf(item.getAmountG());
            calories = calories.add(p.getCaloriesPer100g().multiply(weight));
            protein = protein.add(p.getProteinPer100g().multiply(weight));
            fat = fat.add(p.getFatPer100g().multiply(weight));
            carb = carb.add(p.getCarbPer100g().multiply(weight));
        }

        // Divide by totalWeightG to get per-gram, then multiply by 100 to get per 100g
        // Equivalent to: sum(macro * weight) / totalWeight
        return new BigDecimal[]{
                calories.divide(totalWeightG, 2, RoundingMode.HALF_UP),
                protein.divide(totalWeightG, 2, RoundingMode.HALF_UP),
                fat.divide(totalWeightG, 2, RoundingMode.HALF_UP),
                carb.divide(totalWeightG, 2, RoundingMode.HALF_UP)
        };
    }
}
