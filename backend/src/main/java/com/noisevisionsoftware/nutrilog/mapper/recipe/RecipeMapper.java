package com.noisevisionsoftware.nutrilog.mapper.recipe;

import com.noisevisionsoftware.nutrilog.dto.request.recipe.RecipeUpdateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.NutritionalValuesResponse;
import com.noisevisionsoftware.nutrilog.dto.response.recipe.RecipeResponse;
import com.noisevisionsoftware.nutrilog.model.recipe.NutritionalValues;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
                .photos(recipe.getPhotos() != null ? recipe.getPhotos() : new ArrayList<>())
                .nutritionalValues(toNutritionalValuesResponse(recipe.getNutritionalValues()))
                .parentRecipeId(recipe.getParentRecipeId())
                .build();
    }

    public Recipe toModel(RecipeUpdateRequest request) {
        NutritionalValues nutritionalValues = null;
        if (request.getNutritionalValues() != null) {
            nutritionalValues = NutritionalValues.builder()
                    .calories(request.getNutritionalValues().getCalories())
                    .protein(request.getNutritionalValues().getProtein())
                    .fat(request.getNutritionalValues().getFat())
                    .carbs(request.getNutritionalValues().getCarbs())
                    .build();
        }

        return Recipe.builder()
                .name(request.getName())
                .instructions(request.getInstructions())
                .nutritionalValues(nutritionalValues)
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
}
