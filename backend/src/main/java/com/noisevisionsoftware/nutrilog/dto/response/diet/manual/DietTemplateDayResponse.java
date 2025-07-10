package com.noisevisionsoftware.nutrilog.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateDayResponse {
    private int dayNumber;
    private String dayName;
    private String notes;
    private List<DietTemplateMealResponse> meals;

    private int totalMeals;
    private int totalIngredients;
    private boolean hasNotes;
    private DietTemplateNutritionResponse dailyNutrition;
}