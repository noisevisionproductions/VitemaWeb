package com.noisevisionsoftware.vitema.dto.response.diet;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DayMealResponse {
    private String recipeId;
    private MealType mealType;
    private String time;
}