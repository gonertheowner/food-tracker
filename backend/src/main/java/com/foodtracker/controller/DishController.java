package com.foodtracker.controller;

import com.foodtracker.dto.dish.DishRequest;
import com.foodtracker.dto.dish.DishResponse;
import com.foodtracker.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    // TODO: replace with authenticated user id once JWT auth is implemented
    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DishResponse create(@Valid @RequestBody DishRequest request) {
        return dishService.createDish(CURRENT_USER_ID, request);
    }

    @GetMapping
    public List<DishResponse> list() {
        return dishService.getUserDishes(CURRENT_USER_ID);
    }

    @GetMapping("/{id}")
    public DishResponse get(@PathVariable Long id) {
        return dishService.getDish(id, CURRENT_USER_ID);
    }

    @PutMapping("/{id}")
    public DishResponse update(@PathVariable Long id, @Valid @RequestBody DishRequest request) {
        return dishService.updateDish(id, CURRENT_USER_ID, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dishService.deleteDish(id, CURRENT_USER_ID);
    }
}
