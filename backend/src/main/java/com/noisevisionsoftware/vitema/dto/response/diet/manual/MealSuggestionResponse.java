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
public class MealSuggestionResponse {

    private String id;
    private String name;
    private String instructions;
    private NutritionalValuesResponse nutritionalValues;
    private List<String> photos;
    private List<IngredientSuggestion> ingredients;
    private Double similarity;
    private boolean isExact;
    private String source; // "RECIPE" or "TEMPLATE"
    private int usageCount;
    private String lastUsed;
}
