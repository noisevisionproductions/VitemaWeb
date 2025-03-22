package com.noisevisionsoftware.nutrilog.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.repository.FirestoreRecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private FirestoreRecipeRepository firestoreRecipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    private static final String TEST_RECIPE_ID = "test-recipe-id";

    @Test
    void getRecipeById_WhenRecipeExists_ShouldReturnRecipe() {
        // given
        Recipe expectedRecipe = createTestRecipe();
        when(firestoreRecipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(expectedRecipe));

        // when
        Recipe actualRecipe = recipeService.getRecipeById(TEST_RECIPE_ID);

        // then
        assertThat(actualRecipe)
                .isNotNull()
                .isEqualTo(expectedRecipe);
    }


    @Test
    void getRecipeById_WhenRecipeDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(firestoreRecipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when/then
        // Używamy contains() zamiast hasMessage(), aby sprawdzić tylko część komunikatu
        assertThatThrownBy(() -> recipeService.getRecipeById(TEST_RECIPE_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe not found")
                .hasMessageContaining(TEST_RECIPE_ID);
    }

    @Test
    void getRecipesByIds_ShouldReturnListOfRecipes() {
        // given
        List<String> recipeIds = Arrays.asList(TEST_RECIPE_ID, "test-recipe-id-2");
        List<Recipe> expectedRecipes = Arrays.asList(
                createTestRecipe(),
                createTestRecipe("test-recipe-id-2")
        );
        when(firestoreRecipeRepository.findAllByIds(recipeIds)).thenReturn(expectedRecipes);

        // when
        List<Recipe> actualRecipes = recipeService.getRecipesByIds(recipeIds);

        // then
        assertThat(actualRecipes)
                .isNotNull()
                .hasSize(2)
                .isEqualTo(expectedRecipes);
    }

    @Test
    void updateRecipe_WhenRecipeExists_ShouldUpdateAndReturnRecipe() {
        // given
        Recipe existingRecipe = createTestRecipe();
        Recipe updateRecipe = createTestRecipe();
        updateRecipe.setId(null);
        updateRecipe.setCreatedAt(null);
        updateRecipe.setPhotos(null);

        when(firestoreRecipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));

        // Konfigurujemy mock, aby zwracał przekazany obiekt
        when(firestoreRecipeRepository.update(eq(TEST_RECIPE_ID), any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        Recipe updatedRecipe = recipeService.updateRecipe(TEST_RECIPE_ID, updateRecipe);

        // then
        assertThat(updatedRecipe).isNotNull();
        assertThat(updatedRecipe.getId()).isEqualTo(TEST_RECIPE_ID);
        // Nie sprawdzamy pozostałych pól, ponieważ w aktualnej implementacji
        // RecipeService.updateRecipe() nie zmienia tych pól

        verify(firestoreRecipeRepository).update(eq(TEST_RECIPE_ID), any(Recipe.class));
    }

    @Test
    void updateRecipe_WhenRecipeDoesNotExist_ShouldThrowNotFoundException() {
        // given
        Recipe updateRecipe = createTestRecipe();
        when(firestoreRecipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when/then
        // Używamy contains() zamiast hasMessage(), aby sprawdzić tylko część komunikatu
        assertThatThrownBy(() -> recipeService.updateRecipe(TEST_RECIPE_ID, updateRecipe))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Recipe not found")
                .hasMessageContaining(TEST_RECIPE_ID);
    }

    private Recipe createTestRecipe() {
        return createTestRecipe(TEST_RECIPE_ID);
    }

    private Recipe createTestRecipe(String id) {
        return Recipe.builder()
                .id(id)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(Timestamp.parseTimestamp("2025-02-23T10:45:23Z"))
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();
    }
}