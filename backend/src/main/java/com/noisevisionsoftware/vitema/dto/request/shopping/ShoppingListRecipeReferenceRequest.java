package com.noisevisionsoftware.vitema.dto.request.shopping;

import com.google.firebase.database.annotations.NotNull;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListRecipeReferenceRequest {
    @NotBlank
    private String recipeId;

    @NotBlank
    private String recipeName;

    @NotNull
    @Min(0)
    private int dayIndex;

    @NotNull
    private MealType mealType;

    @NotBlank
    private String mealTime;
}