package com.foodtracker.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "brand", columnDefinition = "text")
    private String brand;

    @Column(name = "calories_per_100g", nullable = false, precision = 10, scale = 2)
    private BigDecimal caloriesPer100g;

    @Column(name = "protein_per_100g", nullable = false, precision = 10, scale = 2)
    private BigDecimal proteinPer100g;

    @Column(name = "fat_per_100g", nullable = false, precision = 10, scale = 2)
    private BigDecimal fatPer100g;

    @Column(name = "carb_per_100g", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbPer100g;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}
