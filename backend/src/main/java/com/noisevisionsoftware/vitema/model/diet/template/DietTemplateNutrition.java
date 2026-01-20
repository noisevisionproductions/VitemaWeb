package com.noisevisionsoftware.vitema.model.diet.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateNutrition {
    private Double targetCalories;
    private Double targetProtein;
    private Double targetFat;
    private Double targetCarbs;
    private String calculationMethod;
}
