package com.noisevisionsoftware.vitema.dto.request.diet.manual;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateNutritionRequest {

    @PositiveOrZero(message = "Kalorie muszą być nieujemne")
    private Double targetCalories;

    @PositiveOrZero(message = "Białko musi być nieujemne")
    private Double targetProtein;

    @PositiveOrZero(message = "Tłuszcze muszą być nieujemne")
    private Double targetFat;

    @PositiveOrZero(message = "Węglowodany muszą być nieujemne")
    private Double targetCarbs;

    private String calculationMethod;
}