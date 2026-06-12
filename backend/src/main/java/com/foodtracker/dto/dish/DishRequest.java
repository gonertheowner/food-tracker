package com.foodtracker.dto.dish;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DishRequest {

    @NotBlank
    private String name;

    // Boxed Boolean so Lombok generates getIsPublic()/setIsPublic() → Jackson maps to "isPublic"
    private Boolean isPublic = false;

    @NotNull
    @Size(min = 1)
    private List<@Valid DishItemRequest> items;
}
