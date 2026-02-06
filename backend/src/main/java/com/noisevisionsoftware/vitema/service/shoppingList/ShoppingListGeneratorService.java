package com.noisevisionsoftware.vitema.service.shoppingList;

import com.noisevisionsoftware.vitema.model.diet.Day;
import com.noisevisionsoftware.vitema.model.diet.DayMeal;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.product.jpa.ProductEntity;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingListRecipeReference;
import com.noisevisionsoftware.vitema.repository.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingListGeneratorService {

    private final ProductJpaRepository productRepository;

    private static final String DEFAULT_CATEGORY = "inne";

    public Map<String, List<CategorizedShoppingListItem>> generateItemsFromDiet(Diet diet) {
        List<IngredientContext> allIngredients = extractAllIngredients(diet);

        Set<Long> productIds = allIngredients.stream()
                .map(ctx -> ctx.ingredient.getProductId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ProductEntity> dbProducts = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        Map<String, CategorizedShoppingListItem> aggregatedItems = new HashMap<>();
        Map<String, String> itemCategories = new HashMap<>();

        for (IngredientContext ctx : allIngredients) {
            processIngredient(ctx, dbProducts, aggregatedItems, itemCategories);
        }

        Map<String, List<CategorizedShoppingListItem>> result = new HashMap<>();
        for (Map.Entry<String, CategorizedShoppingListItem> entry : aggregatedItems.entrySet()) {
            String aggregationKey = entry.getKey();
            CategorizedShoppingListItem item = entry.getValue();
            String categoryId = itemCategories.getOrDefault(aggregationKey, DEFAULT_CATEGORY);

            result.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(item);
        }

        result.values().forEach(list -> list.sort(Comparator.comparing(CategorizedShoppingListItem::getName)));

        return result;
    }

    private void processIngredient(
            IngredientContext ctx,
            Map<Long, ProductEntity> dbProducts,
            Map<String, CategorizedShoppingListItem> aggregatedItems,
            Map<String, String> itemCategories
    ) {
        RecipeIngredient ing = ctx.ingredient;

        String determinedName = ing.getName();
        String categoryId = DEFAULT_CATEGORY;
        String unit = ing.getUnit() != null ? ing.getUnit().toLowerCase() : "szt";
        String aggregationKey;

        boolean isDbProduct = ing.getProductId() != null && dbProducts.containsKey(ing.getProductId());

        if (isDbProduct) {
            ProductEntity dbProduct = dbProducts.get(ing.getProductId());
            determinedName = dbProduct.getName(); // Nadpisujemy nazwą z bazy
            categoryId = dbProduct.getCategory() != null ? dbProduct.getCategory() : DEFAULT_CATEGORY;
            aggregationKey = "DB_" + dbProduct.getId() + "_" + unit;
        } else {
            aggregationKey = "RAW_" + determinedName.trim().toLowerCase() + "_" + unit;
            if (ing.getCategoryId() != null && !ing.getCategoryId().isEmpty()) {
                categoryId = ing.getCategoryId();
            }
        }

        ShoppingListRecipeReference reference = ShoppingListRecipeReference.builder()
                .recipeName(ctx.mealName)
                .dayIndex(ctx.dayIndex)
                .mealType(ctx.mealType)
                .mealTime(ctx.mealTime)
                .build();

        String finalNameForLambda = determinedName;

        CategorizedShoppingListItem item = aggregatedItems.computeIfAbsent(aggregationKey, k -> CategorizedShoppingListItem.builder()
                .name(finalNameForLambda)
                .quantity(0.0)
                .unit(unit)
                .original(finalNameForLambda)
                .recipes(new ArrayList<>())
                .build());

        double quantityToAdd = ing.getQuantity() != null ? ing.getQuantity() : 0.0;
        item.setQuantity(item.getQuantity() + quantityToAdd);
        item.getRecipes().add(reference);

        itemCategories.put(aggregationKey, categoryId);
    }

    private record IngredientContext(
            RecipeIngredient ingredient,
            String mealName,
            int dayIndex,
            MealType mealType,
            String mealTime
    ) {
    }

    private List<IngredientContext> extractAllIngredients(Diet diet) {
        List<IngredientContext> list = new ArrayList<>();
        if (diet.getDays() == null) return list;

        for (int i = 0; i < diet.getDays().size(); i++) {
            Day day = diet.getDays().get(i);
            if (day.getMeals() == null) continue;

            for (DayMeal meal : day.getMeals()) {
                if (meal.getIngredients() == null) continue;
                for (RecipeIngredient ing : meal.getIngredients()) {
                    list.add(new IngredientContext(ing, meal.getName(), i, meal.getMealType(), meal.getTime()));
                }
            }
        }
        return list;
    }
}