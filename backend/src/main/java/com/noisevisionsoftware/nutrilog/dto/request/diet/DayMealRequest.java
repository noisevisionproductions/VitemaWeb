package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.diet.MealType;
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