package com.noisevisionsoftware.vitema.dto.response.shopping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShoppingListRecipeReferenceResponse {
    private String recipeId;
    private String recipeName;
    private int dayIndex;
    private String mealType;
    private String mealTime;
}