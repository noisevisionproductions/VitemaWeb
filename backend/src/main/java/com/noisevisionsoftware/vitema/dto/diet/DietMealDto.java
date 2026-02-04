package com.noisevisionsoftware.vitema.dto.diet;

import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietMealDto {

    private String originalRecipeId;
    private String name;
    private String mealType;
    private String time;
    private String instructions;
    private List<DietIngredientDto> ingredients;
    private NutritionalValues nutritionalValues;
}