package com.foodtracker.dto.entry;

import com.foodtracker.domain.MealType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EntryUpdateRequest {

    @NotNull
    @Positive
    private Integer amountG;

    @NotNull
    private MealType mealType;

    private OffsetDateTime eatenAt;
}
