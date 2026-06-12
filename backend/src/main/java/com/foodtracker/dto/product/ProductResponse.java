package com.foodtracker.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private Long userId;
    private String name;
    private String brand;
    private BigDecimal caloriesPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal carbPer100g;
    // Boxed Boolean so Lombok generates getIsPublic()/setIsPublic() → Jackson maps to "isPublic"
    private Boolean isPublic;
    private OffsetDateTime createdAt;
}
