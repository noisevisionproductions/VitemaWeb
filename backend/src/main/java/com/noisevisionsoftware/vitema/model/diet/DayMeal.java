package com.noisevisionsoftware.vitema.model.diet;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMeal {
    private String recipeId;
    private String name;
    private MealType mealType;
    private String time;
    private String instructions;
    private List<RecipeIngredient> ingredients;
    private NutritionalValues nutritionalValues;
}