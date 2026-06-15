package com.foodtracker.controller;

import com.foodtracker.dto.entry.EntryRequest;
import com.foodtracker.dto.entry.EntryResponse;
import com.foodtracker.dto.entry.EntryUpdateRequest;
import com.foodtracker.service.EntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/v1/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    // TODO: replace with authenticated user id once JWT auth is implemented
    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponse create(@Valid @RequestBody EntryRequest request) {
        return entryService.createEntry(CURRENT_USER_ID, request);
    }

    @GetMapping
    public List<EntryResponse> listByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = date != null ? date : LocalDate.now(ZoneOffset.UTC);
        return entryService.getEntriesByDate(CURRENT_USER_ID, effectiveDate);
    }

    @GetMapping("/{id}")
    public EntryResponse get(@PathVariable Long id) {
        return entryService.getEntry(id, CURRENT_USER_ID);
    }

    @PutMapping("/{id}")
    public EntryResponse update(@PathVariable Long id, @Valid @RequestBody EntryUpdateRequest request) {
        return entryService.updateEntry(id, CURRENT_USER_ID, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        entryService.deleteEntry(id, CURRENT_USER_ID);
    }
}
