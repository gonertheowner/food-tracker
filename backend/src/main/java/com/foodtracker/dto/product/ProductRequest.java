package com.foodtracker.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProductRequest {

    @NotBlank
    private String name;

    private String brand;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal caloriesPer100g;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal proteinPer100g;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal fatPer100g;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal carbPer100g;

    // Boxed Boolean so Lombok generates getIsPublic()/setIsPublic() → Jackson maps to "isPublic"
    private Boolean isPublic = false;
}
