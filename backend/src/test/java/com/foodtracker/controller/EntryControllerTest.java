package com.foodtracker.controller;

import com.foodtracker.domain.MealType;
import com.foodtracker.domain.SourceType;
import com.foodtracker.dto.entry.EntryRequest;
import com.foodtracker.dto.entry.EntryResponse;
import com.foodtracker.dto.entry.EntryUpdateRequest;
import com.foodtracker.exception.EntryNotFoundException;
import com.foodtracker.service.EntryService;
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

@WebMvcTest(EntryController.class)
class EntryControllerTest extends AbstractControllerTest {

    @MockBean
    private EntryService entryService;

    @Test
    void createEntry_validProductRequest_returns201WithBody() throws Exception {
        EntryRequest request = buildProductRequest(200);
        EntryResponse response = buildResponse(1L, SourceType.PRODUCT, MealType.LUNCH, 200);
        when(entryService.createEntry(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sourceType").value("PRODUCT"))
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.amountG").value(200))
                .andExpect(jsonPath("$.calories").exists())
                .andExpect(jsonPath("$.proteinG").exists());
    }

    @Test
    void createEntry_validDishRequest_returns201WithBody() throws Exception {
        EntryRequest request = buildDishRequest(150);
        EntryResponse response = buildResponse(2L, SourceType.DISH, MealType.DINNER, 150);
        when(entryService.createEntry(anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.sourceType").value("DISH"));
    }

    @Test
    void createEntry_nullSourceType_returns400WithFieldErrors() throws Exception {
        EntryRequest request = new EntryRequest();
        request.setAmountG(100);
        request.setMealType(MealType.BREAKFAST);

        mockMvc.perform(post("/api/v1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createEntry_nullAmountG_returns400WithFieldErrors() throws Exception {
        EntryRequest request = new EntryRequest();
        request.setSourceType(SourceType.PRODUCT);
        request.setProductId(1L);
        request.setMealType(MealType.BREAKFAST);

        mockMvc.perform(post("/api/v1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createEntry_nullMealType_returns400WithFieldErrors() throws Exception {
        EntryRequest request = new EntryRequest();
        request.setSourceType(SourceType.PRODUCT);
        request.setProductId(1L);
        request.setAmountG(100);

        mockMvc.perform(post("/api/v1/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void listEntries_byDate_returns200WithArray() throws Exception {
        when(entryService.getEntriesByDate(anyLong(), any()))
                .thenReturn(List.of(
                        buildResponse(1L, SourceType.PRODUCT, MealType.BREAKFAST, 100),
                        buildResponse(2L, SourceType.DISH, MealType.LUNCH, 200)));

        mockMvc.perform(get("/api/v1/entries").param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listEntries_noDateParam_returns200WithArray() throws Exception {
        when(entryService.getEntriesByDate(anyLong(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getEntry_existingId_returns200WithBody() throws Exception {
        when(entryService.getEntry(anyLong(), anyLong()))
                .thenReturn(buildResponse(1L, SourceType.PRODUCT, MealType.BREAKFAST, 150));

        mockMvc.perform(get("/api/v1/entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amountG").value(150));
    }

    @Test
    void getEntry_unknownId_returns404() throws Exception {
        when(entryService.getEntry(anyLong(), anyLong()))
                .thenThrow(new EntryNotFoundException(999L));

        mockMvc.perform(get("/api/v1/entries/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateEntry_validRequest_returns200WithBody() throws Exception {
        EntryUpdateRequest request = buildUpdateRequest(300, MealType.DINNER);
        EntryResponse response = buildResponse(1L, SourceType.PRODUCT, MealType.DINNER, 300);
        when(entryService.updateEntry(anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/entries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountG").value(300))
                .andExpect(jsonPath("$.mealType").value("DINNER"));
    }

    @Test
    void updateEntry_unknownId_returns404() throws Exception {
        EntryUpdateRequest request = buildUpdateRequest(100, MealType.LUNCH);
        when(entryService.updateEntry(anyLong(), anyLong(), any()))
                .thenThrow(new EntryNotFoundException(999L));

        mockMvc.perform(put("/api/v1/entries/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEntry_existingId_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/entries/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEntry_unknownId_returns404() throws Exception {
        doThrow(new EntryNotFoundException(999L))
                .when(entryService).deleteEntry(anyLong(), anyLong());

        mockMvc.perform(delete("/api/v1/entries/999"))
                .andExpect(status().isNotFound());
    }

    private EntryRequest buildProductRequest(int amountG) {
        EntryRequest r = new EntryRequest();
        r.setSourceType(SourceType.PRODUCT);
        r.setProductId(1L);
        r.setAmountG(amountG);
        r.setMealType(MealType.LUNCH);
        r.setEatenAt(OffsetDateTime.now());
        return r;
    }

    private EntryRequest buildDishRequest(int amountG) {
        EntryRequest r = new EntryRequest();
        r.setSourceType(SourceType.DISH);
        r.setDishId(1L);
        r.setAmountG(amountG);
        r.setMealType(MealType.DINNER);
        r.setEatenAt(OffsetDateTime.now());
        return r;
    }

    private EntryUpdateRequest buildUpdateRequest(int amountG, MealType mealType) {
        EntryUpdateRequest r = new EntryUpdateRequest();
        r.setAmountG(amountG);
        r.setMealType(mealType);
        return r;
    }

    private EntryResponse buildResponse(Long id, SourceType sourceType, MealType mealType, int amountG) {
        return EntryResponse.builder()
                .id(id)
                .userId(1L)
                .eatenAt(OffsetDateTime.now())
                .mealType(mealType)
                .sourceType(sourceType)
                .productId(sourceType == SourceType.PRODUCT ? 1L : null)
                .productName(sourceType == SourceType.PRODUCT ? "Chicken Breast" : null)
                .dishId(sourceType == SourceType.DISH ? 1L : null)
                .dishName(sourceType == SourceType.DISH ? "Borsch" : null)
                .amountG(amountG)
                .calories(BigDecimal.valueOf(200))
                .proteinG(BigDecimal.valueOf(20))
                .fatG(BigDecimal.valueOf(5))
                .carbG(BigDecimal.valueOf(0))
                .build();
    }
}
