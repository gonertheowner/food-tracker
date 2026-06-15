package com.foodtracker.repository;

import com.foodtracker.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntryRepositoryTest extends AbstractDbTest {

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Product product;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(buildUser("user1@test.com"));
        user2 = userRepository.save(buildUser("user2@test.com"));
        product = productRepository.save(buildProduct(user1.getId()));
    }

    @Test
    void save_productEntry_persistsAndFindById() {
        Entry entry = buildProductEntry(user1.getId(), MealType.BREAKFAST);
        Entry saved = entryRepository.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(entryRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void save_productEntry_correctFields() {
        Entry entry = buildProductEntry(user1.getId(), MealType.LUNCH);
        Entry saved = entryRepository.save(entry);

        Entry found = entryRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getSourceType()).isEqualTo(SourceType.PRODUCT);
        assertThat(found.getMealType()).isEqualTo(MealType.LUNCH);
        assertThat(found.getAmountG()).isEqualTo(200);
        assertThat(found.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(found.getProteinG()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(found.getFatG()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(found.getCarbG()).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    void findByUserId_returnsOnlyUserEntries() {
        entryRepository.save(buildProductEntry(user1.getId(), MealType.BREAKFAST));
        entryRepository.save(buildProductEntry(user1.getId(), MealType.LUNCH));
        entryRepository.save(buildProductEntry(user2.getId(), MealType.DINNER));

        List<Entry> result = entryRepository.findByUserId(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getUserId().equals(user1.getId()));
    }

    @Test
    void findByUserIdAndEatenAtBetween_returnsOnlyEntriesInRange() {
        OffsetDateTime day = OffsetDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime dayBefore = OffsetDateTime.of(2024, 1, 14, 12, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime dayAfter = OffsetDateTime.of(2024, 1, 16, 12, 0, 0, 0, ZoneOffset.UTC);

        Entry inRange = buildProductEntry(user1.getId(), MealType.BREAKFAST);
        inRange.setEatenAt(day);
        Entry beforeRange = buildProductEntry(user1.getId(), MealType.LUNCH);
        beforeRange.setEatenAt(dayBefore);
        Entry afterRange = buildProductEntry(user1.getId(), MealType.DINNER);
        afterRange.setEatenAt(dayAfter);

        entryRepository.save(inRange);
        entryRepository.save(beforeRange);
        entryRepository.save(afterRange);

        OffsetDateTime from = OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.of(2024, 1, 15, 23, 59, 59, 999_999_999, ZoneOffset.UTC);

        List<Entry> result = entryRepository.findByUserIdAndEatenAtBetween(user1.getId(), from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMealType()).isEqualTo(MealType.BREAKFAST);
    }

    @Test
    void findByUserIdAndMealType_returnsOnlyMatchingMealType() {
        entryRepository.save(buildProductEntry(user1.getId(), MealType.BREAKFAST));
        entryRepository.save(buildProductEntry(user1.getId(), MealType.BREAKFAST));
        entryRepository.save(buildProductEntry(user1.getId(), MealType.LUNCH));

        List<Entry> result = entryRepository.findByUserIdAndMealType(user1.getId(), MealType.BREAKFAST);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getMealType() == MealType.BREAKFAST);
    }

    @Test
    void deleteEntry_removesFromDb() {
        Entry entry = entryRepository.save(buildProductEntry(user1.getId(), MealType.SNACK));
        Long id = entry.getId();

        entryRepository.delete(entry);

        assertThat(entryRepository.findById(id)).isEmpty();
    }

    @Test
    void save_updateAmountG_persistsChange() {
        Entry entry = entryRepository.save(buildProductEntry(user1.getId(), MealType.BREAKFAST));
        entry.setAmountG(350);
        entry.setCalories(BigDecimal.valueOf(700));
        Entry updated = entryRepository.save(entry);

        assertThat(updated.getAmountG()).isEqualTo(350);
        assertThat(updated.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    private User buildUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash("hash");
        u.setGoalCalories(2000);
        return u;
    }

    private Product buildProduct(Long userId) {
        Product p = new Product();
        p.setUserId(userId);
        p.setName("Chicken Breast");
        p.setCaloriesPer100g(BigDecimal.valueOf(200));
        p.setProteinPer100g(BigDecimal.valueOf(20));
        p.setFatPer100g(BigDecimal.valueOf(10));
        p.setCarbPer100g(BigDecimal.valueOf(5));
        p.setPublic(false);
        return p;
    }

    private Entry buildProductEntry(Long userId, MealType mealType) {
        Entry e = new Entry();
        e.setUserId(userId);
        e.setSourceType(SourceType.PRODUCT);
        e.setProductId(product.getId());
        e.setMealType(mealType);
        e.setAmountG(200);
        e.setEatenAt(OffsetDateTime.now(ZoneOffset.UTC));
        e.setCalories(BigDecimal.valueOf(400));
        e.setProteinG(BigDecimal.valueOf(40));
        e.setFatG(BigDecimal.valueOf(20));
        e.setCarbG(BigDecimal.valueOf(10));
        return e;
    }
}
