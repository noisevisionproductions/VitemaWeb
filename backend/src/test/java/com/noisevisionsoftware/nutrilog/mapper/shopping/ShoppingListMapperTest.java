package com.noisevisionsoftware.nutrilog.mapper.shopping;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.shopping.CategorizedShoppingListItemRequest;
import com.noisevisionsoftware.nutrilog.dto.request.shopping.ShoppingListItemRequest;
import com.noisevisionsoftware.nutrilog.dto.request.shopping.ShoppingListRecipeReferenceRequest;
import com.noisevisionsoftware.nutrilog.dto.request.shopping.UpdateShoppingListRequest;
import com.noisevisionsoftware.nutrilog.dto.response.shopping.CategorizedShoppingListItemResponse;
import com.noisevisionsoftware.nutrilog.dto.response.shopping.ShoppingListRecipeReferenceResponse;
import com.noisevisionsoftware.nutrilog.dto.response.shopping.ShoppingListResponse;
import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import com.noisevisionsoftware.nutrilog.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.nutrilog.model.shopping.ShoppingList;
import com.noisevisionsoftware.nutrilog.model.shopping.ShoppingListRecipeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ShoppingListMapperTest {

    private ShoppingListMapper mapper;

    private static final String TEST_ID = "test-id";
    private static final String TEST_DIET_ID = "test-diet-id";
    private static final String TEST_USER_ID = "noisevisionproductions";
    private static final String TEST_TIMESTAMP_STRING = "2025-02-23T10:57:01Z";
    private static final Timestamp TEST_TIMESTAMP = Timestamp.parseTimestamp(TEST_TIMESTAMP_STRING);

    @BeforeEach
    void setUp() {
        mapper = new ShoppingListMapper();
    }

    @Test
    void toModel_WhenUpdateRequestIsComplete_ShouldMapAllFields() {
        // given
        UpdateShoppingListRequest request = createUpdateShoppingListRequest();

        // when
        Map<String, List<CategorizedShoppingListItem>> result = mapper.toModel(request);

        // then
        assertThat(result)
                .containsKey("DAIRY")
                .satisfies(map -> {
                    List<CategorizedShoppingListItem> dairyItems = map.get("DAIRY");
                    assertThat(dairyItems).hasSize(1);
                    CategorizedShoppingListItem item = dairyItems.getFirst();
                    assertThat(item)
                            .satisfies(i -> {
                                assertThat(i.getName()).isEqualTo("Milk");
                                assertThat(i.getQuantity()).isEqualTo(1.0);
                                assertThat(i.getUnit()).isEqualTo("L");
                                assertThat(i.getOriginal()).isEqualTo("1L milk");

                                List<ShoppingListRecipeReference> recipes = i.getRecipes();
                                assertThat(recipes).hasSize(1);
                                ShoppingListRecipeReference recipe = recipes.getFirst();
                                assertThat(recipe.getRecipeId()).isEqualTo("recipe-1");
                                assertThat(recipe.getRecipeName()).isEqualTo("Test Recipe");
                                assertThat(recipe.getDayIndex()).isEqualTo(0);
                                assertThat(recipe.getMealType()).isEqualTo(MealType.BREAKFAST);
                                assertThat(recipe.getMealTime()).isEqualTo("08:00");
                            });
                });
    }

    @Test
    void toResponse_WhenShoppingListIsComplete_ShouldMapAllFields() {
        // given
        ShoppingList shoppingList = createTestShoppingList();

        // when
        ShoppingListResponse response = mapper.toResponse(shoppingList);

        // then
        assertThat(response)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(TEST_ID);
                    assertThat(r.getDietId()).isEqualTo(TEST_DIET_ID);
                    assertThat(r.getUserId()).isEqualTo(TEST_USER_ID);
                    assertThat(r.getCreatedAt()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(r.getStartDate()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(r.getEndDate()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(r.getVersion()).isEqualTo(1);

                    Map<String, List<CategorizedShoppingListItemResponse>> items = r.getItems();
                    assertThat(items)
                            .containsKey("DAIRY")
                            .satisfies(map -> {
                                List<CategorizedShoppingListItemResponse> dairyItems = map.get("DAIRY");
                                assertThat(dairyItems).hasSize(1);
                                CategorizedShoppingListItemResponse item = dairyItems.getFirst();
                                assertThat(item.getName()).isEqualTo("Milk");
                                assertThat(item.getQuantity()).isEqualTo(1.0);
                                assertThat(item.getUnit()).isEqualTo("L");
                                assertThat(item.getOriginal()).isEqualTo("1L milk");

                                List<ShoppingListRecipeReferenceResponse> recipes = item.getRecipes();
                                assertThat(recipes).hasSize(1);
                                ShoppingListRecipeReferenceResponse recipe = recipes.getFirst();
                                assertThat(recipe.getRecipeId()).isEqualTo("recipe-1");
                                assertThat(recipe.getRecipeName()).isEqualTo("Test Recipe");
                                assertThat(recipe.getDayIndex()).isEqualTo(0);
                                assertThat(recipe.getMealType()).isEqualTo("BREAKFAST");
                                assertThat(recipe.getMealTime()).isEqualTo("08:00");
                            });
                });
    }

    @Test
    void toModel_WhenShoppingListItemRequestIsComplete_ShouldMapAllFields() {
        // given
        ShoppingListItemRequest request = createShoppingListItemRequest();

        // when
        CategorizedShoppingListItem result = mapper.toModel(request);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(item -> {
                    assertThat(item.getName()).isEqualTo("Milk");
                    assertThat(item.getQuantity()).isEqualTo(1.0);
                    assertThat(item.getUnit()).isEqualTo("L");
                    assertThat(item.getOriginal()).isEqualTo("1L milk");
                    assertThat(item.getRecipes()).hasSize(1);
                });
    }

    private UpdateShoppingListRequest createUpdateShoppingListRequest() {
        ShoppingListRecipeReferenceRequest recipeReference = ShoppingListRecipeReferenceRequest.builder()
                .recipeId("recipe-1")
                .recipeName("Test Recipe")
                .dayIndex(0)
                .mealType(MealType.BREAKFAST)
                .mealTime("08:00")
                .build();

        CategorizedShoppingListItemRequest item = CategorizedShoppingListItemRequest.builder()
                .name("Milk")
                .quantity(1.0)
                .unit("L")
                .original("1L milk")
                .recipes(Collections.singletonList(recipeReference))
                .build();

        Map<String, List<CategorizedShoppingListItemRequest>> items = new HashMap<>();
        items.put("DAIRY", Collections.singletonList(item));

        return UpdateShoppingListRequest.builder()
                .items(items)
                .build();
    }

    private ShoppingListItemRequest createShoppingListItemRequest() {
        ShoppingListRecipeReferenceRequest recipeReference = ShoppingListRecipeReferenceRequest.builder()
                .recipeId("recipe-1")
                .recipeName("Test Recipe")
                .dayIndex(0)
                .mealType(MealType.BREAKFAST)
                .mealTime("08:00")
                .build();

        return ShoppingListItemRequest.builder()
                .name("Milk")
                .quantity(1.0)
                .unit("L")
                .original("1L milk")
                .recipes(Collections.singletonList(recipeReference))
                .build();
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