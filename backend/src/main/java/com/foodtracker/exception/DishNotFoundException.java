package com.foodtracker.exception;

public class DishNotFoundException extends RuntimeException {

    public DishNotFoundException(Long id) {
        super("Dish not found: " + id);
    }
}
