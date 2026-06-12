package com.foodtracker.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "entries")
@Getter
@Setter
@NoArgsConstructor
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "eaten_at", nullable = false)
    private OffsetDateTime eatenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(name = "amount_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountG;

    @Column(name = "calories", nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "fat_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal fatG;

    @Column(name = "carb_g", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbG;
}
