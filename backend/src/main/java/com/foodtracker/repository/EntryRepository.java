package com.foodtracker.repository;

import com.foodtracker.domain.Entry;
import com.foodtracker.domain.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findByUserId(Long userId);

    List<Entry> findByUserIdAndEatenAtBetween(Long userId, OffsetDateTime from, OffsetDateTime to);

    List<Entry> findByUserIdAndMealType(Long userId, MealType mealType);
}
