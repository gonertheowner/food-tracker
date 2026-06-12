package com.foodtracker.repository;

import com.foodtracker.domain.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    List<Dish> findByUserId(Long userId);

    List<Dish> findByUserIdOrIsPublicTrue(Long userId);
}
