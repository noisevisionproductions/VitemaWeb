package com.noisevisionsoftware.vitema.model.diet;

import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietMeal {

    private String originalRecipeId;
    private String name;
    @Builder.Default
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    private String instructions;
    private NutritionalValues nutritionalValues;
    private String mealType;
    private String time;
    private boolean isCustomized;
}