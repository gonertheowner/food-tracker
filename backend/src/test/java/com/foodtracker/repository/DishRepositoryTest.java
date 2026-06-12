package com.foodtracker.repository;

import com.foodtracker.domain.Dish;
import com.foodtracker.domain.DishItem;
import com.foodtracker.domain.Product;
import com.foodtracker.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DishRepositoryTest extends AbstractDbTest {

    @Autowired
    private DishRepository dishRepository;

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
    void saveAndFindById() {
        Dish dish = buildDish(user1.getId(), "Borsch", false);
        Dish saved = dishRepository.save(dish);

        assertThat(saved.getId()).isNotNull();
        assertThat(dishRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void saveWithItems_cascadesItems() {
        Dish dish = buildDishWithItems(user1.getId(), "Borsch", false);
        Dish saved = dishRepository.save(dish);

        Dish found = dishRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getAmountG()).isEqualTo(300);
    }

    @Test
    void findByUserId_returnsOnlyUserDishes() {
        dishRepository.save(buildDish(user1.getId(), "User1 Dish", false));
        dishRepository.save(buildDish(user2.getId(), "User2 Dish", false));

        List<Dish> result = dishRepository.findByUserId(user1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("User1 Dish");
    }

    @Test
    void findByUserIdOrIsPublicTrue_returnsCombined() {
        dishRepository.save(buildDish(user1.getId(), "Mine", false));
        dishRepository.save(buildDish(user2.getId(), "Theirs Private", false));
        dishRepository.save(buildDish(user2.getId(), "Theirs Public", true));

        List<Dish> result = dishRepository.findByUserIdOrIsPublicTrue(user1.getId());

        assertThat(result).extracting(Dish::getName)
                .containsExactlyInAnyOrder("Mine", "Theirs Public");
    }

    @Test
    void deleteDish_cascadesItems() {
        Dish dish = buildDishWithItems(user1.getId(), "ToDelete", false);
        Dish saved = dishRepository.save(dish);
        Long dishId = saved.getId();

        dishRepository.delete(saved);

        assertThat(dishRepository.findById(dishId)).isEmpty();
    }

    @Test
    void updateDish_replacesItems() {
        Dish dish = buildDishWithItems(user1.getId(), "Original", false);
        Dish saved = dishRepository.save(dish);

        saved.getItems().clear();
        DishItem newItem = new DishItem();
        newItem.setDish(saved);
        newItem.setProduct(product);
        newItem.setAmountG(150);
        saved.getItems().add(newItem);
        saved.setTotalWeightG(BigDecimal.valueOf(150));
        Dish updated = dishRepository.save(saved);

        assertThat(updated.getItems()).hasSize(1);
        assertThat(updated.getItems().get(0).getAmountG()).isEqualTo(150);
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
        p.setCaloriesPer100g(BigDecimal.valueOf(165));
        p.setProteinPer100g(BigDecimal.valueOf(31));
        p.setFatPer100g(BigDecimal.valueOf(3.6));
        p.setCarbPer100g(BigDecimal.valueOf(0));
        p.setPublic(false);
        return p;
    }

    private Dish buildDish(Long userId, String name, boolean isPublic) {
        Dish d = new Dish();
        d.setUserId(userId);
        d.setName(name);
        d.setPublic(isPublic);
        d.setTotalWeightG(BigDecimal.valueOf(300));
        return d;
    }

    private Dish buildDishWithItems(Long userId, String name, boolean isPublic) {
        Dish d = buildDish(userId, name, isPublic);
        DishItem item = new DishItem();
        item.setDish(d);
        item.setProduct(product);
        item.setAmountG(300);
        d.getItems().add(item);
        return d;
    }
}
