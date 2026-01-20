package com.noisevisionsoftware.vitema.mapper.shopping;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingListRecipeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirestoreShoppingMapperTest {

    private FirestoreShoppingMapper mapper;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private static final String TEST_ID = "test-id";
    private static final String TEST_DIET_ID = "test-diet-id";
    private static final String TEST_USER_ID = "noisevisionproductions";
    private static final Timestamp TEST_TIMESTAMP = Timestamp.parseTimestamp("2025-02-23T10:55:46Z");

    @BeforeEach
    void setUp() {
        mapper = new FirestoreShoppingMapper();
    }

    @Test
    void toFirestoreMap_WhenShoppingListIsComplete_ShouldMapAllFields() {
        // given
        ShoppingList shoppingList = createTestShoppingList();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(shoppingList);

        // then
        assertThat(result)
                .containsEntry("id", TEST_ID)
                .containsEntry("dietId", TEST_DIET_ID)
                .containsEntry("userId", TEST_USER_ID)
                .containsEntry("createdAt", TEST_TIMESTAMP)
                .containsEntry("startDate", TEST_TIMESTAMP)
                .containsEntry("endDate", TEST_TIMESTAMP)
                .containsEntry("version", 1);

        @SuppressWarnings("unchecked")
        Map<String, List<Map<String, Object>>> items = (Map<String, List<Map<String, Object>>>) result.get("items");
        assertThat(items)
                .containsKey("DAIRY")
                .satisfies(map -> {
                    List<Map<String, Object>> dairyItems = map.get("DAIRY");
                    assertThat(dairyItems).hasSize(1);
                    Map<String, Object> item = dairyItems.getFirst();
                    assertThat(item)
                            .containsEntry("name", "Milk")
                            .containsEntry("quantity", 1.0)
                            .containsEntry("unit", "L")
                            .containsEntry("original", "1L milk");

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> recipes = (List<Map<String, Object>>) item.get("recipes");
                    assertThat(recipes).hasSize(1);
                    Map<String, Object> recipe = recipes.getFirst();
                    assertThat(recipe)
                            .containsEntry("recipeId", "recipe-1")
                            .containsEntry("recipeName", "Test Recipe")
                            .containsEntry("dayIndex", 0)
                            .containsEntry("mealType", "BREAKFAST")
                            .containsEntry("mealTime", "08:00");
                });
    }

    @Test
    void toShoppingList_WhenDocumentExists_ShouldMapAllFields() {
        // given
        Map<String, Object> recipeRef = new HashMap<>();
        recipeRef.put("recipeId", "recipe-1");
        recipeRef.put("recipeName", "Test Recipe");
        recipeRef.put("dayIndex", 0L);
        recipeRef.put("mealType", "BREAKFAST");
        recipeRef.put("mealTime", "08:00");

        Map<String, Object> item = new HashMap<>();
        item.put("name", "Milk");
        item.put("quantity", 1.0);
        item.put("unit", "L");
        item.put("original", "1L milk");
        item.put("recipes", Collections.singletonList(recipeRef));

        Map<String, List<Map<String, Object>>> items = new HashMap<>();
        items.put("DAIRY", Collections.singletonList(item));

        Map<String, Object> data = new HashMap<>();
        data.put("id", TEST_ID);
        data.put("dietId", TEST_DIET_ID);
        data.put("userId", TEST_USER_ID);
        data.put("items", items);
        data.put("createdAt", TEST_TIMESTAMP);
        data.put("startDate", TEST_TIMESTAMP);
        data.put("endDate", TEST_TIMESTAMP);
        data.put("version", 1L);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(data);
        when(documentSnapshot.getId()).thenReturn(TEST_ID);

        // when
        ShoppingList result = mapper.toShoppingList(documentSnapshot);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(list -> {
                    assertThat(list.getId()).isEqualTo(TEST_ID);
                    assertThat(list.getDietId()).isEqualTo(TEST_DIET_ID);
                    assertThat(list.getUserId()).isEqualTo(TEST_USER_ID);
                    assertThat(list.getCreatedAt()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(list.getStartDate()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(list.getEndDate()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(list.getVersion()).isEqualTo(1);

                    Map<String, List<CategorizedShoppingListItem>> listItems = list.getItems();
                    assertThat(listItems)
                            .containsKey("DAIRY")
                            .satisfies(itemMap -> {
                                List<CategorizedShoppingListItem> dairyItems = itemMap.get("DAIRY");
                                assertThat(dairyItems).hasSize(1);
                                CategorizedShoppingListItem dairyItem = dairyItems.getFirst();
                                assertThat(dairyItem.getName()).isEqualTo("Milk");
                                assertThat(dairyItem.getQuantity()).isEqualTo(1.0);
                                assertThat(dairyItem.getUnit()).isEqualTo("L");
                                assertThat(dairyItem.getOriginal()).isEqualTo("1L milk");

                                List<ShoppingListRecipeReference> itemRecipes = dairyItem.getRecipes();
                                assertThat(itemRecipes).hasSize(1);
                                ShoppingListRecipeReference itemRecipe = itemRecipes.getFirst();
                                assertThat(itemRecipe.getRecipeId()).isEqualTo("recipe-1");
                                assertThat(itemRecipe.getRecipeName()).isEqualTo("Test Recipe");
                                assertThat(itemRecipe.getDayIndex()).isEqualTo(0);
                                assertThat(itemRecipe.getMealType()).isEqualTo(MealType.BREAKFAST);
                                assertThat(itemRecipe.getMealTime()).isEqualTo("08:00");
                            });
                });
    }

    @Test
    void toShoppingList_WhenDocumentDoesNotExist_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        ShoppingList result = mapper.toShoppingList(documentSnapshot);

        // then
        assertThat(result).isNull();
    }

    private ShoppingList createTestShoppingList() {
        ShoppingListRecipeReference recipeReference = ShoppingListRecipeReference.builder()
                .recipeId("recipe-1")
                .recipeName("Test Recipe")
                .dayIndex(0)
                .mealType(MealType.BREAKFAST)
                .mealTime("08:00")
                .build();

        CategorizedShoppingListItem item = CategorizedShoppingListItem.builder()
                .name("Milk")
                .quantity(1.0)
                .unit("L")
                .original("1L milk")
                .recipes(Collections.singletonList(recipeReference))
                .build();

        Map<String, List<CategorizedShoppingListItem>> items = new HashMap<>();
        items.put("DAIRY", Collections.singletonList(item));

        return ShoppingList.builder()
                .id(TEST_ID)
                .dietId(TEST_DIET_ID)
                .userId(TEST_USER_ID)
                .items(items)
                .createdAt(TEST_TIMESTAMP)
                .startDate(TEST_TIMESTAMP)
                .endDate(TEST_TIMESTAMP)
                .version(1)
                .build();
    }
}