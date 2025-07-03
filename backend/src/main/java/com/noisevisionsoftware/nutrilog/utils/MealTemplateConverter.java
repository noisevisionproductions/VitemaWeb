package com.noisevisionsoftware.nutrilog.utils;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.diet.manual.MealIngredientRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.manual.SaveMealTemplateRequest;
import com.noisevisionsoftware.nutrilog.dto.request.recipe.NutritionalValuesRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.IngredientSuggestion;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.MealIngredientResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.MealTemplateResponse;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.NutritionalValuesResponse;
import com.noisevisionsoftware.nutrilog.model.meal.MealIngredient;
import com.noisevisionsoftware.nutrilog.model.meal.MealTemplate;
import com.noisevisionsoftware.nutrilog.model.recipe.NutritionalValues;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Konwerter odpowiedzialny za mapowanie obiektów związanych z szablonami posiłków
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MealTemplateConverter {

    /**
     * Konwertuje SaveMealTemplateRequest na MealTemplate
     */
    public MealTemplate convertRequestToTemplate(SaveMealTemplateRequest request) {
        if (request == null) return null;

        return MealTemplate.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .instructions(request.getInstructions())
                .nutritionalValues(convertNutritionalValuesFromRequest(request.getNutritionalValues()))
                .photos(request.getPhotos() != null ? new ArrayList<>(request.getPhotos()) : new ArrayList<>())
                .ingredients(convertIngredientsFromRequest(request.getIngredients()))
                .mealType(request.getMealType())
                .category(request.getCategory())
                .createdAt(Timestamp.ofTimeSecondsAndNanos(
                        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), 0))
                .updatedAt(Timestamp.ofTimeSecondsAndNanos(
                        LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), 0))
                .usageCount(0)
                .build();
    }

    /**
     * Konwertuje Recipe na MealTemplateResponse
     * Uwaga: Recipe nie ma wszystkich pól z MealTemplate, więc niektóre będą null
     */
    public MealTemplateResponse convertRecipeToTemplate(Recipe recipe) {
        if (recipe == null) return null;

        return MealTemplateResponse.builder()
                .id("recipe-" + recipe.getId())
                .name(recipe.getName())
                .instructions(recipe.getInstructions())
                .nutritionalValues(convertNutritionalValuesToResponse(recipe.getNutritionalValues()))
                .photos(recipe.getPhotos() != null ? new ArrayList<>(recipe.getPhotos()) : new ArrayList<>())
                .ingredients(new ArrayList<>()) // Recipe nie ma szczegółowych składników w modelu
                .mealType(null) // Recipe nie ma mealType
                .category(null) // Recipe nie ma category
                .createdBy(null) // Recipe nie ma createdBy
                .createdAt(recipe.getCreatedAt() != null ? recipe.getCreatedAt().toString() : null)
                .updatedAt(null) // Recipe nie ma updatedAt
                .usageCount(0) // Recipe nie ma usageCount
                .build();
    }

    /**
     * Konwertuje MealTemplate na MealTemplateResponse
     */
    public MealTemplateResponse convertTemplateToResponse(MealTemplate template) {
        if (template == null) return null;

        return MealTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .instructions(template.getInstructions())
                .nutritionalValues(convertNutritionalValuesToResponse(template.getNutritionalValues()))
                .photos(template.getPhotos())
                .ingredients(convertIngredientsToResponse(template.getIngredients()))
                .mealType(template.getMealType())
                .category(template.getCategory())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt() != null ? template.getCreatedAt().toString() : null)
                .updatedAt(template.getUpdatedAt() != null ? template.getUpdatedAt().toString() : null)
                .usageCount(template.getUsageCount())
                .build();
    }

    /**
     * Konwertuje listę MealIngredient na listę IngredientSuggestion dla sugestii
     */
    public List<IngredientSuggestion> convertIngredientsToSuggestions(List<MealIngredient> ingredients) {
        if (ingredients == null) return new ArrayList<>();

        return ingredients.stream()
                .map(ingredient -> IngredientSuggestion.builder()
                        .name(ingredient.getName())
                        .quantity(ingredient.getQuantity())
                        .unit(ingredient.getUnit())
                        .original(ingredient.getOriginal())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Konwertuje listę MealIngredient na listę MealIngredientResponse
     */
    public List<MealIngredientResponse> convertIngredientsToResponse(List<MealIngredient> ingredients) {
        if (ingredients == null) return new ArrayList<>();

        return ingredients.stream()
                .map(ingredient -> MealIngredientResponse.builder()
                        .id(ingredient.getId())
                        .name(ingredient.getName())
                        .quantity(String.valueOf(ingredient.getQuantity()))
                        .unit(ingredient.getUnit())
                        .original(ingredient.getOriginal())
                        .categoryId(ingredient.getCategoryId())
                        .hasCustomUnit(ingredient.isHasCustomUnit())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Konwertuje NutritionalValues na NutritionalValuesResponse
     */
    public NutritionalValuesResponse convertNutritionalValuesToResponse(NutritionalValues nutritionalValues) {
        if (nutritionalValues == null) return null;

        return NutritionalValuesResponse.builder()
                .calories(nutritionalValues.getCalories() != null ? nutritionalValues.getCalories() : 0.0)
                .protein(nutritionalValues.getProtein() != null ? nutritionalValues.getProtein() : 0.0)
                .fat(nutritionalValues.getFat() != null ? nutritionalValues.getFat() : 0.0)
                .carbs(nutritionalValues.getCarbs() != null ? nutritionalValues.getCarbs() : 0.0)
                .build();
    }

    /**
     * Konwertuje NutritionalValues na Map dla kompatybilności wstecznej
     */
    public Map<String, Object> convertNutritionalValues(NutritionalValues nutritionalValues) {
        if (nutritionalValues == null) return null;

        Map<String, Object> values = new HashMap<>();
        values.put("calories", nutritionalValues.getCalories() != null ? nutritionalValues.getCalories() : 0.0);
        values.put("protein", nutritionalValues.getProtein() != null ? nutritionalValues.getProtein() : 0.0);
        values.put("fat", nutritionalValues.getFat() != null ? nutritionalValues.getFat() : 0.0);
        values.put("carbs", nutritionalValues.getCarbs() != null ? nutritionalValues.getCarbs() : 0.0);

        return values;
    }

    private NutritionalValues convertNutritionalValuesFromRequest(NutritionalValuesRequest request) {
        if (request == null) return null;

        return NutritionalValues.builder()
                .calories(request.getCalories())
                .protein(request.getProtein())
                .fat(request.getFat())
                .carbs(request.getCarbs())
                .build();
    }

    private List<MealIngredient> convertIngredientsFromRequest(List<MealIngredientRequest> ingredientRequests) {
        if (ingredientRequests == null) return new ArrayList<>();

        return ingredientRequests.stream()
                .map(request -> MealIngredient.builder()
                        .id(UUID.randomUUID().toString())
                        .name(request.getName())
                        .quantity(request.getQuantity())
                        .unit(request.getUnit())
                        .original(request.getOriginal())
                        .categoryId(request.getCategoryId())
                        .hasCustomUnit(request.isHasCustomUnit())
                        .build())
                .collect(Collectors.toList());
    }
}