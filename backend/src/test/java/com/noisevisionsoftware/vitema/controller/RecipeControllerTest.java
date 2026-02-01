package com.noisevisionsoftware.vitema.controller;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipeImageResponse;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.vitema.dto.response.recipe.RecipesPageResponse;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeMapper;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void getAllRecipes_ShouldReturnPaginatedRecipes() {
        // given
        int page = 0;
        int size = 50;
        String sortBy = "createdAt";
        String sortDir = "desc";

        List<Recipe> recipes = Arrays.asList(
                createTestRecipe(),
                createTestRecipe("test-recipe-id-2")
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Recipe> recipesPage = new PageImpl<>(recipes, pageable, 2);

        List<RecipeResponse> expectedResponses = Arrays.asList(
                createTestRecipeResponse(),
                createTestRecipeResponse("test-recipe-id-2")
        );

        when(recipeService.getAllRecipes(any(Pageable.class))).thenReturn(recipesPage);
        when(recipeMapper.toResponse(recipes.get(0))).thenReturn(expectedResponses.get(0));
        when(recipeMapper.toResponse(recipes.get(1))).thenReturn(expectedResponses.get(1));

        // when
        ResponseEntity<RecipesPageResponse> response = recipeController.getAllRecipes(page, size, sortBy, sortDir);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent())
                .hasSize(2)
                .isEqualTo(expectedResponses);
        assertThat(response.getBody().getPage()).isEqualTo(page);
        assertThat(response.getBody().getSize()).isEqualTo(size);
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
        assertThat(response.getBody().getTotalPages()).isEqualTo(1);
    }

    @Test
    void getAllRecipes_WithCustomPagination_ShouldReturnCorrectPage() {
        // given
        int page = 1;
        int size = 10;
        String sortBy = "name";
        String sortDir = "asc";

        List<Recipe> recipes = Collections.singletonList(createTestRecipe());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        // Page 1 with 1 item, size 10 means: page 0 has 10 items, page 1 has 1 item, total = 11
        Page<Recipe> recipesPage = new PageImpl<>(recipes, pageable, 11);

        when(recipeService.getAllRecipes(any(Pageable.class))).thenReturn(recipesPage);
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(createTestRecipeResponse());

        // when
        ResponseEntity<RecipesPageResponse> response = recipeController.getAllRecipes(page, size, sortBy, sortDir);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPage()).isEqualTo(page);
        assertThat(response.getBody().getSize()).isEqualTo(size);
        assertThat(response.getBody().getTotalElements()).isEqualTo(11);
        assertThat(response.getBody().getTotalPages()).isEqualTo(2);
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
    void getRecipesByIds_WhenIdsIsNull_ShouldReturnEmptyList() {
        // when
        ResponseEntity<List<RecipeResponse>> response = recipeController.getRecipesByIds(null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getRecipesByIds_WhenIdsIsEmpty_ShouldReturnEmptyList() {
        // when
        ResponseEntity<List<RecipeResponse>> response = recipeController.getRecipesByIds("");

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEmpty();
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

    @Test
    void deleteRecipe_WhenRecipeExists_ShouldReturnOk() {
        // given
        doNothing().when(recipeService).deleteRecipe(TEST_RECIPE_ID);

        // when
        ResponseEntity<Void> response = recipeController.deleteRecipe(TEST_RECIPE_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(recipeService).deleteRecipe(TEST_RECIPE_ID);
    }

    @Test
    void deleteRecipe_WhenRecipeDoesNotExist_ShouldReturnNotFound() {
        // given
        doThrow(new NotFoundException("Recipe not found: " + TEST_RECIPE_ID))
                .when(recipeService).deleteRecipe(TEST_RECIPE_ID);

        // when
        ResponseEntity<Void> response = recipeController.deleteRecipe(TEST_RECIPE_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(recipeService).deleteRecipe(TEST_RECIPE_ID);
    }

    @Test
    void deleteRecipe_WhenExceptionOccurs_ShouldReturnInternalServerError() {
        // given
        doThrow(new RuntimeException("Database error"))
                .when(recipeService).deleteRecipe(TEST_RECIPE_ID);

        // when
        ResponseEntity<Void> response = recipeController.deleteRecipe(TEST_RECIPE_ID);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(recipeService).deleteRecipe(TEST_RECIPE_ID);
    }

    @Test
    void uploadImage_WhenValidImage_ShouldReturnImageUrl() throws BadRequestException {
        // given
        MultipartFile mockFile = org.mockito.Mockito.mock(MultipartFile.class);
        String expectedImageUrl = "https://storage.googleapis.com/bucket/image.jpg";

        when(recipeService.uploadImage(TEST_RECIPE_ID, mockFile)).thenReturn(expectedImageUrl);

        // when
        ResponseEntity<RecipeImageResponse> response = recipeController.uploadImage(TEST_RECIPE_ID, mockFile);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getImageUrl()).isEqualTo(expectedImageUrl);
        verify(recipeService).uploadImage(TEST_RECIPE_ID, mockFile);
    }

    @Test
    void uploadImage_WhenServiceThrowsException_ShouldPropagateException() throws BadRequestException {
        // given
        MultipartFile mockFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(recipeService.uploadImage(TEST_RECIPE_ID, mockFile))
                .thenThrow(new BadRequestException("Invalid image format"));

        // when/then
        assertThatThrownBy(() -> recipeController.uploadImage(TEST_RECIPE_ID, mockFile))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid image format");
    }

    @Test
    void deleteImage_WhenValidRequest_ShouldReturnOk() throws BadRequestException {
        // given
        RecipeImageResponse request = RecipeImageResponse.builder()
                .imageUrl("https://storage.googleapis.com/bucket/image.jpg")
                .build();

        doNothing().when(recipeService).deleteImage(TEST_RECIPE_ID, request.getImageUrl());

        // when
        ResponseEntity<Void> response = recipeController.deleteImage(TEST_RECIPE_ID, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(recipeService).deleteImage(TEST_RECIPE_ID, request.getImageUrl());
    }

    @Test
    void deleteImage_WhenServiceThrowsException_ShouldPropagateException() throws BadRequestException {
        // given
        RecipeImageResponse request = RecipeImageResponse.builder()
                .imageUrl("https://storage.googleapis.com/bucket/image.jpg")
                .build();

        doThrow(new BadRequestException("Image not found"))
                .when(recipeService).deleteImage(TEST_RECIPE_ID, request.getImageUrl());

        // when/then
        assertThatThrownBy(() -> recipeController.deleteImage(TEST_RECIPE_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Image not found");
    }

    @Test
    void searchRecipes_WhenQueryProvided_ShouldReturnSearchResults() {
        // given
        String query = "pasta";
        List<Recipe> searchResults = Arrays.asList(
                createTestRecipe(),
                createTestRecipe("test-recipe-id-2")
        );

        List<RecipeResponse> expectedResponses = Arrays.asList(
                createTestRecipeResponse(),
                createTestRecipeResponse("test-recipe-id-2")
        );

        when(recipeService.searchRecipes(query)).thenReturn(searchResults);
        when(recipeMapper.toResponse(searchResults.get(0))).thenReturn(expectedResponses.get(0));
        when(recipeMapper.toResponse(searchResults.get(1))).thenReturn(expectedResponses.get(1));

        // when
        ResponseEntity<List<RecipeResponse>> response = recipeController.searchRecipes(query);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .hasSize(2)
                .isEqualTo(expectedResponses);
        verify(recipeService).searchRecipes(query);
    }

    @Test
    void searchRecipes_WhenNoResults_ShouldReturnEmptyList() {
        // given
        String query = "nonexistent";
        when(recipeService.searchRecipes(query)).thenReturn(new ArrayList<>());

        // when
        ResponseEntity<List<RecipeResponse>> response = recipeController.searchRecipes(query);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isEmpty();
        verify(recipeService).searchRecipes(query);
    }

    @Test
    void uploadBase64Image_WhenValidBase64Data_ShouldReturnImageUrl() throws BadRequestException {
        // given
        String base64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
        Map<String, String> request = new HashMap<>();
        request.put("imageData", base64Image);
        String expectedImageUrl = "https://storage.googleapis.com/bucket/image.jpg";

        when(recipeService.uploadBase64Image(base64Image)).thenReturn(expectedImageUrl);

        // when
        ResponseEntity<RecipeImageResponse> response = recipeController.uploadBase64Image(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getImageUrl()).isEqualTo(expectedImageUrl);
        verify(recipeService).uploadBase64Image(base64Image);
    }

    @Test
    void uploadBase64Image_WhenImageDataIsNull_ShouldThrowBadRequestException() {
        // given
        Map<String, String> request = new HashMap<>();
        request.put("imageData", null);

        // when/then
        assertThatThrownBy(() -> recipeController.uploadBase64Image(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Brak danych obrazu");
    }

    @Test
    void uploadBase64Image_WhenImageDataIsMissing_ShouldThrowBadRequestException() {
        // given
        Map<String, String> request = new HashMap<>();

        // when/then
        assertThatThrownBy(() -> recipeController.uploadBase64Image(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Brak danych obrazu");
    }

    @Test
    void uploadBase64Image_WhenServiceThrowsException_ShouldPropagateException() throws BadRequestException {
        // given
        String base64Image = "invalid-base64";
        Map<String, String> request = new HashMap<>();
        request.put("imageData", base64Image);

        when(recipeService.uploadBase64Image(base64Image))
                .thenThrow(new BadRequestException("Invalid base64 format"));

        // when/then
        assertThatThrownBy(() -> recipeController.uploadBase64Image(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid base64 format");
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