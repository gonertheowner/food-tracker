package com.foodtracker.dto.entry;

import com.foodtracker.domain.MealType;
import com.foodtracker.domain.SourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EntryRequest {

    @NotNull
    private SourceType sourceType;

    private Long productId;

    private Long dishId;

    @NotNull
    @Positive
    private Integer amountG;

    @NotNull
    private MealType mealType;

    private OffsetDateTime eatenAt;
}
