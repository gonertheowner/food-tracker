package com.foodtracker.controller;

import com.foodtracker.dto.dish.DishItemRequest;
import com.foodtracker.dto.dish.DishItemResponse;
import com.foodtracker.dto.dish.DishRequest;
import com.foodtracker.dto.dish.DishResponse;
import com.foodtracker.exception.DishNotFoundException;
import com.foodtracker.service.DishService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DishController.class)
class DishControllerTest extends AbstractControllerTest {

    @MockBean
    private DishService dishService;

    @Test
    void createDish_validRequest_returns201WithBody() throws Exception {
        DishRequest request = buildRequest("Borsch");
        DishResponse response = buildResponse(1L, "Borsch");
        when(dishService.createDish(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Borsch"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.caloriesPer100g").exists());
    }

    @Test
    void createDish_blankName_returns400WithFieldErrors() throws Exception {
        DishRequest request = buildRequest("");

        mockMvc.perform(post("/api/v1/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").exists())
                .andExpect(jsonPath("$.fieldErrors[0].message").exists());
    }

    @Test
    void createDish_emptyItems_returns400WithFieldErrors() throws Exception {
        DishRequest request = new DishRequest();
        request.setName("Valid Name");
        request.setItems(List.of());

        mockMvc.perform(post("/api/v1/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createDish_nullItems_returns400WithFieldErrors() throws Exception {
        DishRequest request = new DishRequest();
        request.setName("Valid Name");
        // items is null — should fail @NotNull

        mockMvc.perform(post("/api/v1/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getDishes_returns200WithArray() throws Exception {
        when(dishService.getUserDishes(anyLong()))
                .thenReturn(List.of(buildResponse(1L, "Borsch"), buildResponse(2L, "Pasta")));

        mockMvc.perform(get("/api/v1/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getDish_existingId_returns200WithBody() throws Exception {
        when(dishService.getDish(anyLong(), anyLong()))
                .thenReturn(buildResponse(1L, "Borsch"));

        mockMvc.perform(get("/api/v1/dishes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Borsch"))
                .andExpect(jsonPath("$.isPublic").exists())
                .andExpect(jsonPath("$.totalWeightG").exists());
    }

    @Test
    void getDish_unknownId_returns404() throws Exception {
        when(dishService.getDish(anyLong(), anyLong()))
                .thenThrow(new DishNotFoundException(999L));

        mockMvc.perform(get("/api/v1/dishes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateDish_validRequest_returns200WithBody() throws Exception {
        DishRequest request = buildRequest("Updated Borsch");
        DishResponse response = buildResponse(1L, "Updated Borsch");
        when(dishService.updateDish(anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/dishes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Borsch"));
    }

    @Test
    void deleteDish_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/dishes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDish_unknownId_returns404() throws Exception {
        doThrow(new DishNotFoundException(999L))
                .when(dishService).deleteDish(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/dishes/999"))
                .andExpect(status().isNotFound());
    }

    private DishRequest buildRequest(String name) {
        DishItemRequest item = new DishItemRequest();
        item.setProductId(1L);
        item.setAmountG(300);

        DishRequest r = new DishRequest();
        r.setName(name);
        r.setIsPublic(false);
        r.setItems(List.of(item));
        return r;
    }

    private DishResponse buildResponse(Long id, String name) {
        DishItemResponse item = DishItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("Chicken Breast")
                .amountG(300)
                .build();

        return DishResponse.builder()
                .id(id)
                .userId(1L)
                .name(name)
                .isPublic(false)
                .totalWeightG(BigDecimal.valueOf(300))
                .items(List.of(item))
                .caloriesPer100g(BigDecimal.valueOf(165))
                .proteinPer100g(BigDecimal.valueOf(31))
                .fatPer100g(BigDecimal.valueOf(3.6))
                .carbPer100g(BigDecimal.valueOf(0))
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
