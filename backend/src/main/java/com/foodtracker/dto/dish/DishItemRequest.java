package com.foodtracker.dto.dish;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DishItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    private Integer amountG;
}
