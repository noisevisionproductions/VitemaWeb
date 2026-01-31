package com.noisevisionsoftware.vitema.service.diet.manual;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.IngredientSuggestion;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.MealSuggestionResponse;
import com.noisevisionsoftware.vitema.dto.response.recipe.NutritionalValuesResponse;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.utils.MealTemplateConverter;
import com.noisevisionsoftware.vitema.utils.SimilarityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MealSuggestionService Unit Tests")
class MealSuggestionServiceTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private MealTemplateService mealTemplateService;

    @Mock
    private MealTemplateConverter mealTemplateConverter;

    @Mock
    private SimilarityCalculator similarityCalculator;

    @InjectMocks
    private MealSuggestionService mealSuggestionService;

    private String userId;
    private String query;
    private Recipe recipe;
    private MealTemplate mealTemplate;
    private NutritionalValuesResponse nutritionalValuesResponse;
    private List<IngredientSuggestion> ingredientSuggestions;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        query = "chicken salad";

        // Setup Recipe
        recipe = Recipe.builder()
                .id("recipe-1")
                .name("Chicken Salad")
                .instructions("Mix ingredients")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(350.0)
                        .protein(30.0)
                        .carbs(15.0)
                        .fat(20.0)
                        .build())
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .createdAt(Timestamp.now())
                .build();

        // Setup MealTemplate
        mealTemplate = MealTemplate.builder()
                .id("template-1")
                .name("Chicken Caesar Salad")
                .instructions("Prepare salad")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(400.0)
                        .protein(35.0)
                        .carbs(20.0)
                        .fat(22.0)
                        .build())
                .photos(Collections.singletonList("template-photo.jpg"))
                .ingredients(new ArrayList<>())
                .usageCount(5)
                .lastUsed(Timestamp.now())
                .build();

        // Setup response objects
        nutritionalValuesResponse = NutritionalValuesResponse.builder()
                .calories(350.0)
                .protein(30.0)
                .carbs(15.0)
                .fat(20.0)
                .build();

        ingredientSuggestions = new ArrayList<>();
    }

    @Nested
    @DisplayName("searchMealSuggestions() tests")
    class SearchMealSuggestionsTests {

        @Test
        @DisplayName("Should return empty list when query is null")
        void shouldReturnEmptyList_When_QueryIsNull() {
            // Given - query is null
            String nullQuery = null;
            int limit = 10;

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(nullQuery, limit, userId);

            // Then
            assertThat(result).isEmpty();
            verify(recipeService, never()).searchRecipes(anyString());
            verify(mealTemplateService, never()).searchAccessibleTemplates(anyString(), anyString(), anyInt());
        }

        @Test
        @DisplayName("Should return empty list when query is empty")
        void shouldReturnEmptyList_When_QueryIsEmpty() {
            // Given - empty query
            String emptyQuery = "   ";
            int limit = 10;

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(emptyQuery, limit, userId);

            // Then
            assertThat(result).isEmpty();
            verify(recipeService, never()).searchRecipes(anyString());
            verify(mealTemplateService, never()).searchAccessibleTemplates(anyString(), anyString(), anyInt());
        }

        @Test
        @DisplayName("Should use default limit of 10 when limit is zero or negative")
        void shouldUseDefaultLimit_When_LimitIsInvalid() {
            // Given
            int invalidLimit = 0;
            when(recipeService.searchRecipes(query)).thenReturn(Collections.emptyList());
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, invalidLimit, userId);

            // Then
            assertThat(result).isNotNull();
            verify(recipeService).searchRecipes(query);
            verify(mealTemplateService).searchAccessibleTemplates(eq(query), eq(userId), anyInt());
        }

        @Test
        @DisplayName("Should return combined results from recipes and templates")
        void shouldReturnCombinedResults_When_BothSourcesHaveData() {
            // Given
            List<Recipe> recipes = Collections.singletonList(recipe);
            List<MealTemplate> templates = Collections.singletonList(mealTemplate);

            when(recipeService.searchRecipes(query)).thenReturn(recipes);
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(templates);
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.85);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);
            when(mealTemplateConverter.convertIngredientsToSuggestions(any()))
                    .thenReturn(ingredientSuggestions);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result)
                    .hasSize(2)
                    .extracting(MealSuggestionResponse::getSource)
                    .containsExactlyInAnyOrder("RECIPE", "TEMPLATE");

            verify(recipeService).searchRecipes(query);
            verify(mealTemplateService).searchAccessibleTemplates(eq(query), eq(userId), eq(5));
            verify(similarityCalculator, times(2)).calculateSimilarity(anyString(), anyString());
        }

        @Test
        @DisplayName("Should sort results with exact matches first")
        void shouldSortExactMatchesFirst_When_ResultsContainExactMatches() {
            // Given
            Recipe exactMatchRecipe = Recipe.builder()
                    .id("recipe-exact")
                    .name("chicken salad") // exact match
                    .instructions("Instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(new ArrayList<>())
                    .createdAt(Timestamp.now())
                    .build();

            Recipe nonExactMatchRecipe = Recipe.builder()
                    .id("recipe-non-exact")
                    .name("Grilled Chicken")
                    .instructions("Instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(new ArrayList<>())
                    .createdAt(Timestamp.now())
                    .build();

            List<Recipe> recipes = Arrays.asList(nonExactMatchRecipe, exactMatchRecipe);

            when(recipeService.searchRecipes(query)).thenReturn(recipes);
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(eq(query), eq("chicken salad"))).thenReturn(1.0);
            when(similarityCalculator.calculateSimilarity(eq(query), eq("Grilled Chicken"))).thenReturn(0.6);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).isExact()).isTrue();
            assertThat(result.get(0).getName()).isEqualToIgnoringCase("chicken salad");
        }

        @Test
        @DisplayName("Should sort by similarity when no exact matches")
        void shouldSortBySimilarity_When_NoExactMatches() {
            // Given
            Recipe highSimilarityRecipe = Recipe.builder()
                    .id("recipe-high")
                    .name("Chicken Caesar")
                    .instructions("Instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(new ArrayList<>())
                    .createdAt(Timestamp.now())
                    .build();

            Recipe lowSimilarityRecipe = Recipe.builder()
                    .id("recipe-low")
                    .name("Beef Stew")
                    .instructions("Instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(new ArrayList<>())
                    .createdAt(Timestamp.now())
                    .build();

            List<Recipe> recipes = Arrays.asList(lowSimilarityRecipe, highSimilarityRecipe);

            when(recipeService.searchRecipes(query)).thenReturn(recipes);
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(eq(query), eq("Chicken Caesar"))).thenReturn(0.9);
            when(similarityCalculator.calculateSimilarity(eq(query), eq("Beef Stew"))).thenReturn(0.3);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSimilarity()).isGreaterThan(result.get(1).getSimilarity());
        }

        @Test
        @DisplayName("Should limit results to specified limit")
        void shouldLimitResults_When_MoreResultsThanLimit() {
            // Given
            int limit = 5;
            List<Recipe> recipes = Arrays.asList(
                    createRecipe("recipe-1", "Chicken 1"),
                    createRecipe("recipe-2", "Chicken 2"),
                    createRecipe("recipe-3", "Chicken 3"),
                    createRecipe("recipe-4", "Chicken 4")
            );
            List<MealTemplate> templates = Arrays.asList(
                    createTemplate("template-1", "Chicken Template 1"),
                    createTemplate("template-2", "Chicken Template 2"),
                    createTemplate("template-3", "Chicken Template 3")
            );

            when(recipeService.searchRecipes(query)).thenReturn(recipes);
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(templates);
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.8);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);
            when(mealTemplateConverter.convertIngredientsToSuggestions(any()))
                    .thenReturn(ingredientSuggestions);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, limit, userId);

            // Then
            assertThat(result).hasSize(limit);
        }

        @Test
        @DisplayName("Should handle recipe without photos")
        void shouldHandleRecipeWithoutPhotos_When_PhotosAreNull() {
            // Given
            Recipe recipeWithoutPhotos = Recipe.builder()
                    .id("recipe-no-photos")
                    .name("Simple Meal")
                    .instructions("Instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(null)
                    .createdAt(Timestamp.now())
                    .build();

            when(recipeService.searchRecipes(query)).thenReturn(Collections.singletonList(recipeWithoutPhotos));
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.8);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPhotos()).isEmpty();
        }

        @Test
        @DisplayName("Should handle template without photos")
        void shouldHandleTemplateWithoutPhotos_When_PhotosAreNull() {
            // Given
            MealTemplate templateWithoutPhotos = MealTemplate.builder()
                    .id("template-no-photos")
                    .name("Simple Template")
                    .instructions("Instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(null)
                    .ingredients(new ArrayList<>())
                    .usageCount(1)
                    .lastUsed(Timestamp.now())
                    .build();

            when(recipeService.searchRecipes(query)).thenReturn(Collections.emptyList());
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.singletonList(templateWithoutPhotos));
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.8);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);
            when(mealTemplateConverter.convertIngredientsToSuggestions(any()))
                    .thenReturn(ingredientSuggestions);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPhotos()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when exception occurs")
        void shouldReturnEmptyList_When_ExceptionOccurs() {
            // Given
            when(recipeService.searchRecipes(query)).thenThrow(new RuntimeException("Database error"));

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should correctly map recipe fields to suggestion response")
        void shouldCorrectlyMapRecipeFields_When_ConvertingToSuggestion() {
            // Given
            when(recipeService.searchRecipes(query)).thenReturn(Collections.singletonList(recipe));
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(eq(query), eq(recipe.getName()))).thenReturn(0.85);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(recipe.getNutritionalValues()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).hasSize(1);
            MealSuggestionResponse suggestion = result.get(0);
            assertThat(suggestion.getId()).isEqualTo("recipe-" + recipe.getId());
            assertThat(suggestion.getName()).isEqualTo(recipe.getName());
            assertThat(suggestion.getInstructions()).isEqualTo(recipe.getInstructions());
            assertThat(suggestion.getPhotos()).isEqualTo(recipe.getPhotos());
            assertThat(suggestion.getSource()).isEqualTo("RECIPE");
            assertThat(suggestion.getSimilarity()).isEqualTo(0.85);
            assertThat(suggestion.getUsageCount()).isEqualTo(0);
            assertThat(suggestion.getIngredients()).isEmpty();
        }

        @Test
        @DisplayName("Should correctly map template fields to suggestion response")
        void shouldCorrectlyMapTemplateFields_When_ConvertingToSuggestion() {
            // Given
            when(recipeService.searchRecipes(query)).thenReturn(Collections.emptyList());
            when(mealTemplateService.searchAccessibleTemplates(eq(query), eq(userId), anyInt()))
                    .thenReturn(Collections.singletonList(mealTemplate));
            when(similarityCalculator.calculateSimilarity(eq(query), eq(mealTemplate.getName()))).thenReturn(0.9);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(mealTemplate.getNutritionalValues()))
                    .thenReturn(nutritionalValuesResponse);
            when(mealTemplateConverter.convertIngredientsToSuggestions(mealTemplate.getIngredients()))
                    .thenReturn(ingredientSuggestions);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.searchMealSuggestions(query, 10, userId);

            // Then
            assertThat(result).hasSize(1);
            MealSuggestionResponse suggestion = result.get(0);
            assertThat(suggestion.getId()).isEqualTo(mealTemplate.getId());
            assertThat(suggestion.getName()).isEqualTo(mealTemplate.getName());
            assertThat(suggestion.getInstructions()).isEqualTo(mealTemplate.getInstructions());
            assertThat(suggestion.getPhotos()).isEqualTo(mealTemplate.getPhotos());
            assertThat(suggestion.getSource()).isEqualTo("TEMPLATE");
            assertThat(suggestion.getSimilarity()).isEqualTo(0.9);
            assertThat(suggestion.getUsageCount()).isEqualTo(mealTemplate.getUsageCount());
        }
    }

    @Nested
    @DisplayName("findSimilarMeals() tests")
    class FindSimilarMealsTests {

        @Test
        @DisplayName("Should delegate to searchMealSuggestions with trimmed meal name")
        void shouldDelegateToSearch_When_FindingSimilarMeals() {
            // Given
            String mealName = "  Chicken Pasta  ";
            int limit = 5;
            when(recipeService.searchRecipes(anyString())).thenReturn(Collections.emptyList());
            when(mealTemplateService.searchAccessibleTemplates(anyString(), anyString(), anyInt()))
                    .thenReturn(Collections.emptyList());

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findSimilarMeals(mealName, limit, userId);

            // Then
            assertThat(result).isNotNull();
            verify(recipeService).searchRecipes("Chicken Pasta");
        }

        @Test
        @DisplayName("Should return results from searchMealSuggestions")
        void shouldReturnResults_When_FindingSimilarMeals() {
            // Given
            String mealName = "Pasta";
            List<Recipe> recipes = Collections.singletonList(recipe);

            when(recipeService.searchRecipes(mealName)).thenReturn(recipes);
            when(mealTemplateService.searchAccessibleTemplates(eq(mealName), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.8);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findSimilarMeals(mealName, 10, userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSource()).isEqualTo("RECIPE");
        }
    }

    @Nested
    @DisplayName("existsExactMeal() tests")
    class ExistsExactMealTests {

        @Test
        @DisplayName("Should return true when exact match exists")
        void shouldReturnTrue_When_ExactMatchExists() {
            // Given
            String mealName = "Chicken Salad";
            MealSuggestionResponse exactMatch = MealSuggestionResponse.builder()
                    .id("recipe-1")
                    .name("Chicken Salad")
                    .similarity(1.0)
                    .isExact(true)
                    .source("RECIPE")
                    .build();

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(recipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(1.0);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            boolean result = mealSuggestionService.existsExactMeal(mealName, userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true when exact match exists with different case")
        void shouldReturnTrue_When_ExactMatchExistsWithDifferentCase() {
            // Given
            String mealName = "CHICKEN SALAD";
            recipe.setName("Chicken Salad");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(recipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.9);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            boolean result = mealSuggestionService.existsExactMeal(mealName, userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when no exact match exists")
        void shouldReturnFalse_When_NoExactMatchExists() {
            // Given
            String mealName = "Beef Stew";
            recipe.setName("Chicken Salad");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(recipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.6);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            boolean result = mealSuggestionService.existsExactMeal(mealName, userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when no meals found")
        void shouldReturnFalse_When_NoMealsFound() {
            // Given
            String mealName = "Non-existent Meal";

            when(recipeService.searchRecipes(anyString())).thenReturn(Collections.emptyList());
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());

            // When
            boolean result = mealSuggestionService.existsExactMeal(mealName, userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should trim meal name before checking")
        void shouldTrimMealName_When_CheckingExistence() {
            // Given
            String mealName = "  Chicken Salad  ";
            recipe.setName("Chicken Salad");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(recipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.9);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            boolean result = mealSuggestionService.existsExactMeal(mealName, userId);

            // Then
            assertThat(result).isTrue();
            verify(recipeService).searchRecipes("Chicken Salad");
        }
    }

    @Nested
    @DisplayName("findHighlySimilarMeals() tests")
    class FindHighlySimilarMealsTests {

        @Test
        @DisplayName("Should return meals with similarity greater than 0.8")
        void shouldReturnHighlySimilarMeals_When_SimilarityAboveThreshold() {
            // Given
            String mealName = "Chicken";
            Recipe highSimilarityRecipe = createRecipe("recipe-high", "Chicken Breast");
            Recipe lowSimilarityRecipe = createRecipe("recipe-low", "Beef Stew");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Arrays.asList(highSimilarityRecipe, lowSimilarityRecipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(eq(mealName), eq("Chicken Breast"))).thenReturn(0.9);
            when(similarityCalculator.calculateSimilarity(eq(mealName), eq("Beef Stew"))).thenReturn(0.5);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findHighlySimilarMeals(mealName, userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSimilarity()).isGreaterThan(0.8);
        }

        @Test
        @DisplayName("Should return empty list when no meals above similarity threshold")
        void shouldReturnEmptyList_When_NoMealsAboveThreshold() {
            // Given
            String mealName = "Pizza";
            Recipe lowSimilarityRecipe = createRecipe("recipe-low", "Chicken Salad");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(lowSimilarityRecipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(anyString(), anyString())).thenReturn(0.4);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findHighlySimilarMeals(mealName, userId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should include meals with similarity exactly 0.8")
        void shouldIncludeMeals_When_SimilarityExactly08() {
            // Given
            String mealName = "Pasta";
            Recipe exactThresholdRecipe = createRecipe("recipe-threshold", "Pasta Carbonara");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(exactThresholdRecipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(eq(mealName), eq("Pasta Carbonara"))).thenReturn(0.8);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findHighlySimilarMeals(mealName, userId);

            // Then
            assertThat(result).isEmpty(); // 0.8 is NOT greater than 0.8
        }

        @Test
        @DisplayName("Should filter meals correctly with similarity just above threshold")
        void shouldIncludeMeals_When_SimilarityJustAboveThreshold() {
            // Given
            String mealName = "Pasta";
            Recipe justAboveThresholdRecipe = createRecipe("recipe-above", "Pasta Carbonara");

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(justAboveThresholdRecipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(similarityCalculator.calculateSimilarity(eq(mealName), eq("Pasta Carbonara"))).thenReturn(0.81);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findHighlySimilarMeals(mealName, userId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSimilarity()).isGreaterThan(0.8);
        }

        @Test
        @DisplayName("Should trim meal name before searching")
        void shouldTrimMealName_When_FindingHighlySimilarMeals() {
            // Given
            String mealName = "  Chicken  ";

            when(recipeService.searchRecipes(anyString())).thenReturn(Collections.emptyList());
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.emptyList());

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findHighlySimilarMeals(mealName, userId);

            // Then
            verify(recipeService).searchRecipes("Chicken");
        }

        @Test
        @DisplayName("Should include both recipes and templates with high similarity")
        void shouldIncludeBothSources_When_BothHaveHighSimilarity() {
            // Given
            String mealName = "Chicken";
            Recipe highSimilarityRecipe = createRecipe("recipe-high", "Chicken Breast");
            MealTemplate highSimilarityTemplate = MealTemplate.builder()
                    .id("template-high")
                    .name("Chicken Thighs")
                    .instructions("Cook")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .photos(new ArrayList<>())
                    .ingredients(new ArrayList<>())
                    .usageCount(3)
                    .lastUsed(Timestamp.now())
                    .build();

            when(recipeService.searchRecipes(anyString()))
                    .thenReturn(Collections.singletonList(highSimilarityRecipe));
            when(mealTemplateService.searchAccessibleTemplates(anyString(), eq(userId), anyInt()))
                    .thenReturn(Collections.singletonList(highSimilarityTemplate));
            when(similarityCalculator.calculateSimilarity(eq(mealName), eq("Chicken Breast"))).thenReturn(0.9);
            when(similarityCalculator.calculateSimilarity(eq(mealName), eq("Chicken Thighs"))).thenReturn(0.85);
            when(mealTemplateConverter.convertNutritionalValuesToResponse(any()))
                    .thenReturn(nutritionalValuesResponse);
            when(mealTemplateConverter.convertIngredientsToSuggestions(any()))
                    .thenReturn(ingredientSuggestions);

            // When
            List<MealSuggestionResponse> result = mealSuggestionService.findHighlySimilarMeals(mealName, userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(MealSuggestionResponse::getSource)
                    .containsExactlyInAnyOrder("RECIPE", "TEMPLATE");
        }
    }

    // Helper method to create test recipes
    private Recipe createRecipe(String id, String name) {
        return Recipe.builder()
                .id(id)
                .name(name)
                .instructions("Instructions")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(300.0)
                        .protein(25.0)
                        .carbs(20.0)
                        .fat(15.0)
                        .build())
                .photos(new ArrayList<>())
                .createdAt(Timestamp.now())
                .build();
    }

    // Helper method to create test meal templates
    private MealTemplate createTemplate(String id, String name) {
        return MealTemplate.builder()
                .id(id)
                .name(name)
                .instructions("Template Instructions")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(350.0)
                        .protein(28.0)
                        .carbs(22.0)
                        .fat(18.0)
                        .build())
                .photos(new ArrayList<>())
                .ingredients(new ArrayList<>())
                .usageCount(1)
                .lastUsed(Timestamp.now())
                .build();
    }
}
