package com.noisevisionsoftware.vitema.controller;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeMapper;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private RecipeMapper recipeMapper;

    @InjectMocks
    private RecipeController recipeController;

    private static final String TEST_RECIPE_ID = "test-recipe-id";

    @Test
    void getRecipeById_WhenRecipeExists_ShouldReturnRecipeResponse() {
        // given
        Recipe recipe = createTestRecipe();
        RecipeResponse expectedResponse = createTestRecipeResponse();

        when(recipeService.getRecipeById(TEST_RECIPE_ID)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(expectedResponse);

        // when
        ResponseEntity<RecipeResponse> response = recipeController.getRecipeById(TEST_RECIPE_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedResponse);
    }

    @Test
    void getRecipeById_WhenRecipeDoesNotExist_ShouldReturnNotFound() {
        // given
        when(recipeService.getRecipeById(TEST_RECIPE_ID))
                .thenThrow(new NotFoundException("Recipe not found: " + TEST_RECIPE_ID));

        // when
        ResponseEntity<RecipeResponse> response = recipeController.getRecipeById(TEST_RECIPE_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getRecipesByIds_ShouldReturnListOfRecipeResponses() {
        // given
        String idsParam = TEST_RECIPE_ID + "," + "test-recipe-id-2";
        List<String> requestedIds = Arrays.asList(idsParam.split(","));

        List<Recipe> recipes = Arrays.asList(
                createTestRecipe(),
                createTestRecipe("test-recipe-id-2")
        );

        List<RecipeResponse> expectedResponses = Arrays.asList(
                createTestRecipeResponse(),
                createTestRecipeResponse("test-recipe-id-2")
        );

        when(recipeService.getRecipesByIds(requestedIds)).thenReturn(recipes);
        when(recipeMapper.toResponse(recipes.get(0))).thenReturn(expectedResponses.get(0));
        when(recipeMapper.toResponse(recipes.get(1))).thenReturn(expectedResponses.get(1));

        // when
        ResponseEntity<List<RecipeResponse>> response = recipeController.getRecipesByIds(idsParam);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .isEqualTo(expectedResponses);
    }

    @Test
    void updateRecipe_WhenRecipeExists_ShouldReturnUpdatedRecipeResponse() {
        // given
        RecipeUpdateRequest updateRequest = createTestRecipeUpdateRequest();
        Recipe recipe = createTestRecipe();
        Recipe updatedRecipe = createTestRecipe();
        RecipeResponse expectedResponse = createTestRecipeResponse();

        when(recipeMapper.toModel(updateRequest)).thenReturn(recipe);
        when(recipeService.updateRecipe(TEST_RECIPE_ID, recipe)).thenReturn(updatedRecipe);
        when(recipeMapper.toResponse(updatedRecipe)).thenReturn(expectedResponse);

        // when
        ResponseEntity<RecipeResponse> response = recipeController.updateRecipe(TEST_RECIPE_ID, updateRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(expectedResponse);

        verify(recipeMapper).toModel(updateRequest);
        verify(recipeService).updateRecipe(TEST_RECIPE_ID, recipe);
        verify(recipeMapper).toResponse(updatedRecipe);
    }

    @Test
    void updateRecipe_WhenRecipeDoesNotExist_ShouldThrowNotFoundException() {
        // given
        RecipeUpdateRequest updateRequest = createTestRecipeUpdateRequest();
        Recipe recipe = createTestRecipe();

        when(recipeMapper.toModel(updateRequest)).thenReturn(recipe);
        when(recipeService.updateRecipe(TEST_RECIPE_ID, recipe))
                .thenThrow(new NotFoundException("Recipe not found: " + TEST_RECIPE_ID));

        // when/then
        assertThatThrownBy(() -> recipeController.updateRecipe(TEST_RECIPE_ID, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Recipe not found: " + TEST_RECIPE_ID);
    }

    private Recipe createTestRecipe() {
        return createTestRecipe(TEST_RECIPE_ID);
    }

    private Recipe createTestRecipe(String id) {
        return Recipe.builder()
                .id(id)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(com.google.cloud.Timestamp.parseTimestamp("2025-02-23T10:48:27Z"))
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();
    }

    private RecipeResponse createTestRecipeResponse() {
        return createTestRecipeResponse(TEST_RECIPE_ID);
    }

    private RecipeResponse createTestRecipeResponse(String id) {
        return RecipeResponse.builder()
                .id(id)
                .name("Test Recipe")
                .instructions("Test instructions")
                .createdAt(Timestamp.parseTimestamp("2025-02-23T10:45:23"))
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .build();
    }

    private RecipeUpdateRequest createTestRecipeUpdateRequest() {
        return RecipeUpdateRequest.builder()
                .name("Test Recipe")
                .instructions("Test instructions")
                .build();
    }
}