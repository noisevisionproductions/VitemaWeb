package com.noisevisionsoftware.nutrilog.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateNutritionResponse {
    private Double targetCalories;
    private Double targetProtein;
    private Double targetFat;
    private Double targetCarbs;
    private String calculationMethod;

    private String calculationMethodLabel;
    private boolean isComplete;
    private Double totalMacros;

    private String summaryText;
}