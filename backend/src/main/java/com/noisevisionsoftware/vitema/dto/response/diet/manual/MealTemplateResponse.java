package com.noisevisionsoftware.vitema.dto.response.diet.manual;

import com.noisevisionsoftware.vitema.dto.response.recipe.NutritionalValuesResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealTemplateResponse {

    private String id;
    private String name;
    private String instructions;
    private NutritionalValuesResponse nutritionalValues;
    private List<String> photos;
    private List<MealIngredientResponse> ingredients;
    private String mealType;
    private String category;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private int usageCount;
}
