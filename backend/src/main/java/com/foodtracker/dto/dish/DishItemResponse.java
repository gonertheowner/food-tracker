package com.foodtracker.dto.dish;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer amountG;
}
