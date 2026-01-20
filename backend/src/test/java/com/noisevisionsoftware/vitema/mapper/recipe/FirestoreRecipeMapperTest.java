package com.noisevisionsoftware.vitema.mapper.recipe;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirestoreRecipeMapperTest {

    private FirestoreRecipeMapper mapper;

    @Mock
    private DocumentSnapshot documentSnapshot;

    private static final String TEST_RECIPE_ID = "test-recipe-id";
    private static final Timestamp TEST_TIMESTAMP = Timestamp.parseTimestamp("2025-02-23T10:51:35Z");

    @BeforeEach
    void setUp() {
        mapper = new FirestoreRecipeMapper();
    }

    @Test
    void toFirestoreMap_WhenRecipeIsComplete_ShouldMapAllFields() {
        // given
        Recipe recipe = createTestRecipe();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(recipe);

        // then
        assertThat(result)
                .containsEntry("name", "Test Recipe")
                .containsEntry("instructions", "Test instructions")
                .containsEntry("createdAt", TEST_TIMESTAMP)
                .containsEntry("parentRecipeId", "parent-recipe-id")
                .containsKey("photos")
                .containsKey("nutritionalValues");

        assertThat(result.get("photos"))
                .asList()
                .containsExactly("photo1.jpg", "photo2.jpg");

        @SuppressWarnings("unchecked")
        Map<String, Object> nutritionalValues = (Map<String, Object>) result.get("nutritionalValues");
        assertThat(nutritionalValues)
                .containsEntry("calories", 500.0)
                .containsEntry("protein", 30.0)
                .containsEntry("fat", 20.0)
                .containsEntry("carbs", 50.0);
    }

    @Test
    void toFirestoreMap_WhenNutritionalValuesAreNull_ShouldMapOtherFields() {
        // given
        Recipe recipe = Recipe.builder()
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(TEST_TIMESTAMP)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();

        // when
        Map<String, Object> result = mapper.toFirestoreMap(recipe);

        // then
        assertThat(result)
                .containsEntry("name", "Test Recipe")
                .containsEntry("instructions", "Test instructions")
                .containsEntry("createdAt", TEST_TIMESTAMP)
                .containsEntry("nutritionalValues", null);
    }

    @Test
    void toRecipe_WhenDocumentExists_ShouldMapAllFields() {
        // given
        Map<String, Object> nutritionalValues = new HashMap<>();
        nutritionalValues.put("calories", 500.0);
        nutritionalValues.put("protein", 30.0);
        nutritionalValues.put("fat", 20.0);
        nutritionalValues.put("carbs", 50.0);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Recipe");
        data.put("instructions", "Test instructions");
        data.put("createdAt", TEST_TIMESTAMP);
        data.put("photos", Arrays.asList("photo1.jpg", "photo2.jpg"));
        data.put("nutritionalValues", nutritionalValues);
        data.put("parentRecipeId", "parent-recipe-id");

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(data);
        when(documentSnapshot.getId()).thenReturn(TEST_RECIPE_ID);

        // when
        Recipe result = mapper.toRecipe(documentSnapshot);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(recipe -> {
                    assertThat(recipe.getId()).isEqualTo(TEST_RECIPE_ID);
                    assertThat(recipe.getName()).isEqualTo("Test Recipe");
                    assertThat(recipe.getInstructions()).isEqualTo("Test instructions");
                    assertThat(recipe.getCreatedAt()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(recipe.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
                    assertThat(recipe.getParentRecipeId()).isEqualTo("parent-recipe-id");

                    NutritionalValues nv = recipe.getNutritionalValues();
                    assertThat(nv).isNotNull();
                    assertThat(nv.getCalories()).isEqualTo(500.0);
                    assertThat(nv.getProtein()).isEqualTo(30.0);
                    assertThat(nv.getFat()).isEqualTo(20.0);
                    assertThat(nv.getCarbs()).isEqualTo(50.0);
                });
    }

    @Test
    void toRecipe_WhenDocumentDoesNotExist_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(false);

        // when
        Recipe result = mapper.toRecipe(documentSnapshot);

        // then
        assertThat(result).isNull();
    }

    @Test
    void toRecipe_WhenDocumentDataIsNull_ShouldReturnNull() {
        // given
        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(null);

        // when
        Recipe result = mapper.toRecipe(documentSnapshot);

        // then
        assertThat(result).isNull();
    }

    @Test
    void toRecipe_WhenNutritionalValuesAreNull_ShouldMapOtherFields() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Test Recipe");
        data.put("instructions", "Test instructions");
        data.put("createdAt", TEST_TIMESTAMP);
        data.put("photos", Arrays.asList("photo1.jpg", "photo2.jpg"));
        data.put("nutritionalValues", null);

        when(documentSnapshot.exists()).thenReturn(true);
        when(documentSnapshot.getData()).thenReturn(data);
        when(documentSnapshot.getId()).thenReturn(TEST_RECIPE_ID);

        // when
        Recipe result = mapper.toRecipe(documentSnapshot);

        // then
        assertThat(result)
                .isNotNull()
                .satisfies(recipe -> {
                    assertThat(recipe.getId()).isEqualTo(TEST_RECIPE_ID);
                    assertThat(recipe.getName()).isEqualTo("Test Recipe");
                    assertThat(recipe.getInstructions()).isEqualTo("Test instructions");
                    assertThat(recipe.getCreatedAt()).isEqualTo(TEST_TIMESTAMP);
                    assertThat(recipe.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
                    assertThat(recipe.getNutritionalValues()).isNull();
                });
    }

    private Recipe createTestRecipe() {
        return Recipe.builder()
                .id(TEST_RECIPE_ID)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(TEST_TIMESTAMP)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .nutritionalValues(NutritionalValues.builder()
                        .calories(500.0)
                        .protein(30.0)
                        .fat(20.0)
                        .carbs(50.0)
                        .build())
                .parentRecipeId("parent-recipe-id")
                .build();
    }
}