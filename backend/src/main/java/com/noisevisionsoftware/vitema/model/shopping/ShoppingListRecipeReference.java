package com.noisevisionsoftware.vitema.model.shopping;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShoppingListRecipeReference {
    private String recipeId;
    private String recipeName;
    private int dayIndex;
    private MealType mealType;
    private String mealTime;
}