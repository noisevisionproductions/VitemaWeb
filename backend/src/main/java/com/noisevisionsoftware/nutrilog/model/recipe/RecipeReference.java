package com.noisevisionsoftware.nutrilog.model.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.diet.MealType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeReference {
    private String id;
    private String recipeId;
    private String dietId;
    private String userId;
    private MealType mealType;
    private Timestamp addedAt;
}
