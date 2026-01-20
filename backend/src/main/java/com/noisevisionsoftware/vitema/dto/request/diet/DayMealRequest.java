package com.noisevisionsoftware.vitema.dto.request.diet;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DayMealRequest {
    @NotBlank
    private String recipeId;

    @NotNull
    private MealType mealType;

    @NotBlank
    private String time;
}