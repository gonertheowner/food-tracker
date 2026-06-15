package com.foodtracker.dto.entry;

import com.foodtracker.domain.MealType;
import com.foodtracker.domain.SourceType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class EntryResponse {
    private Long id;
    private Long userId;
    private OffsetDateTime eatenAt;
    private MealType mealType;
    private SourceType sourceType;
    private Long productId;
    private String productName;
    private Long dishId;
    private String dishName;
    private Integer amountG;
    private BigDecimal calories;
    private BigDecimal proteinG;
    private BigDecimal fatG;
    private BigDecimal carbG;
}
