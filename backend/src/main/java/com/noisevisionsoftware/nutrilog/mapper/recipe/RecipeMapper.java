package com.noisevisionsoftware.nutrilog.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.recipe.NutritionalValuesRequest;
import com.noisevisionsoftware.nutrilog.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.NutritionalValuesResponse;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.nutrilog.model.recipe.NutritionalValues;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

    public RecipeResponse toResponse(Recipe recipe) {
        if (recipe == null) return null;

        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .instructions(recipe.getInstructions())
                .createdAt(recipe.getCreatedAt())
                .photos(recipe.getPhotos())
                .nutritionalValues(toNutritionalValuesResponse(recipe.getNutritionalValues()))
                .parentRecipeId(recipe.getParentRecipeId())
                .build();
    }

    public Recipe toModel(RecipeUpdateRequest request) {
        return Recipe.builder()
                .name(request.getName())
                .instructions(request.getInstructions())
                .nutritionalValues(toNutritionalValues(request.getNutritionalValues()))
                .build();
    }

    private NutritionalValuesResponse toNutritionalValuesResponse(NutritionalValues values) {
        if (values == null) return null;

        return NutritionalValuesResponse.builder()
                .calories(values.getCalories())
                .protein(values.getProtein())
                .fat(values.getFat())
                .carbs(values.getCarbs())
                .build();
    }

    private NutritionalValues toNutritionalValues(NutritionalValuesRequest request) {
        if (request == null) return null;

        return NutritionalValues.builder()
                .calories(request.getCalories())
                .protein(request.getProtein())
                .fat(request.getFat())
                .carbs(request.getCarbs())
                .build();
    }

    private LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toDate().toInstant()
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}
