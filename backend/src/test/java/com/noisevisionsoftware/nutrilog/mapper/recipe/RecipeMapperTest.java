package com.noisevisionsoftware.nutrilog.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.recipe.NutritionalValuesRequest;
import com.noisevisionsoftware.nutrilog.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.NutritionalValuesResponse;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.nutrilog.model.recipe.NutritionalValues;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeMapperTest {

    private RecipeMapper mapper;
    private static final String TEST_RECIPE_ID = "test-recipe-id";
    private static final Timestamp TEST_TIMESTAMP = Timestamp.parseTimestamp("2025-02-23T10:52:47Z");

    @BeforeEach
    void setUp() {
        mapper = new RecipeMapper();
    }

    @Test
    void toResponse_WhenRecipeIsComplete_ShouldMapAllFields() {
        // given
        Recipe recipe = createTestRecipe();

        // when
        RecipeResponse response = mapper.toResponse(recipe);

        // then
        assertThat(response)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(TEST_RECIPE_ID);
                    assertThat(r.getName()).isEqualTo("Test Recipe");
                    assertThat(r.getInstructions()).isEqualTo("Test instructions");
                    assertThat(r.getCreatedAt()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(r.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
                    assertThat(r.getParentRecipeId()).isEqualTo("parent-recipe-id");

                    NutritionalValuesResponse nv = r.getNutritionalValues();
                    assertThat(nv)
                            .isNotNull()
                            .satisfies(values -> {
                                assertThat(values.getCalories()).isEqualTo(500.0);
                                assertThat(values.getProtein()).isEqualTo(30.0);
                                assertThat(values.getFat()).isEqualTo(20.0);
                                assertThat(values.getCarbs()).isEqualTo(50.0);
                            });
                });
    }

    @Test
    void toResponse_WhenRecipeIsNull_ShouldReturnNull() {
        // when
        RecipeResponse response = mapper.toResponse(null);

        // then
        assertThat(response).isNull();
    }

    @Test
    void toResponse_WhenNutritionalValuesAreNull_ShouldMapOtherFields() {
        // given
        Recipe recipe = Recipe.builder()
                .id(TEST_RECIPE_ID)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(TEST_TIMESTAMP)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();

        // when
        RecipeResponse response = mapper.toResponse(recipe);

        // then
        assertThat(response)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getId()).isEqualTo(TEST_RECIPE_ID);
                    assertThat(r.getName()).isEqualTo("Test Recipe");
                    assertThat(r.getInstructions()).isEqualTo("Test instructions");
                    assertThat(r.getCreatedAt()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(r.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
                    assertThat(r.getNutritionalValues()).isNull();
                });
    }

    @Test
    void toModel_WhenRequestIsComplete_ShouldMapAllFields() {
        // given
        RecipeUpdateRequest request = createTestRecipeUpdateRequest();

        // when
        Recipe recipe = mapper.toModel(request);

        // then
        assertThat(recipe)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getName()).isEqualTo("Test Recipe");
                    assertThat(r.getInstructions()).isEqualTo("Test instructions");

                    NutritionalValues nv = r.getNutritionalValues();
                    assertThat(nv)
                            .isNotNull()
                            .satisfies(values -> {
                                assertThat(values.getCalories()).isEqualTo(500.0);
                                assertThat(values.getProtein()).isEqualTo(30.0);
                                assertThat(values.getFat()).isEqualTo(20.0);
                                assertThat(values.getCarbs()).isEqualTo(50.0);
                            });
                });
    }

    @Test
    void toModel_WhenNutritionalValuesAreNull_ShouldMapOtherFields() {
        // given
        RecipeUpdateRequest request = RecipeUpdateRequest.builder()
                .name("Test Recipe")
                .instructions("Test instructions")
                .build();

        // when
        Recipe recipe = mapper.toModel(request);

        // then
        assertThat(recipe)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getName()).isEqualTo("Test Recipe");
                    assertThat(r.getInstructions()).isEqualTo("Test instructions");
                    assertThat(r.getNutritionalValues()).isNull();
                });
    }

    private Recipe createTestRecipe() {
        return Recipe.builder()
                .id(TEST_RECIPE_ID)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(TEST_TIMESTAMP)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .nutritionalValues(createTestNutritionalValues())
                .parentRecipeId("parent-recipe-id")
                .build();
    }

    private NutritionalValues createTestNutritionalValues() {
        return NutritionalValues.builder()
                .calories(500.0)
                .protein(30.0)
                .fat(20.0)
                .carbs(50.0)
                .build();
    }

    private RecipeUpdateRequest createTestRecipeUpdateRequest() {
        return RecipeUpdateRequest.builder()
                .name("Test Recipe")
                .instructions("Test instructions")
                .nutritionalValues(createTestNutritionalValuesRequest())
                .build();
    }

    private NutritionalValuesRequest createTestNutritionalValuesRequest() {
        return NutritionalValuesRequest.builder()
                .calories(500.0)
                .protein(30.0)
                .fat(20.0)
                .carbs(50.0)
                .build();
    }
}