package com.foodtracker.dto.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishResponse {

    private Long id;
    private Long userId;
    private String name;
    // Boxed Boolean so Lombok generates getIsPublic()/setIsPublic() → Jackson maps to "isPublic"
    private Boolean isPublic;
    private BigDecimal totalWeightG;
    private List<DishItemResponse> items;
    private BigDecimal caloriesPer100g;
    private BigDecimal proteinPer100g;
    private BigDecimal fatPer100g;
    private BigDecimal carbPer100g;
    private OffsetDateTime createdAt;
}
