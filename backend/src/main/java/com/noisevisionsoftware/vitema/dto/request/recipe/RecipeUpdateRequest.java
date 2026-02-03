package com.noisevisionsoftware.vitema.dto.request.recipe;

import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeUpdateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String instructions;
    private List<RecipeIngredient> ingredients;
    private NutritionalValuesRequest nutritionalValues;
    private List<String> photos;
    private Boolean isPublic;
}