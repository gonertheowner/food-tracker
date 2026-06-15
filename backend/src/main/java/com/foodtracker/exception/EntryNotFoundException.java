package com.foodtracker.exception;

public class EntryNotFoundException extends RuntimeException {

    public EntryNotFoundException(Long id) {
        super("Entry not found: " + id);
    }
}
