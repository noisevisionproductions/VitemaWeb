package com.noisevisionsoftware.nutrilog.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateResponse {
    private String id;
    private String name;
    private String description;
    private String category;
    private String categoryLabel;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
    private int version;

    private int duration;
    private int mealsPerDay;
    private Map<String, String> mealTimes;
    private List<String> mealTypes;

    private List<DietTemplateDayResponse> days;
    private DietTemplateNutritionResponse targetNutrition;

    private int usageCount;
    private String lastUsed;
    private String notes;

    private int totalMeals;
    private int totalIngredients;
    private boolean hasPhotos;
}