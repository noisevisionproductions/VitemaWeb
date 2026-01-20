package com.noisevisionsoftware.vitema.dto.request.diet.manual;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class DietTemplateRequest {

    @NotBlank(message = "Nazwa szablonu jest wymagana")
    private String name;

    private String description;

    @NotBlank(message = "Kategoria jest wymagana")
    private String category;

    @NotNull(message = "Czas trwania jest wymagany")
    @Positive(message = "Czas trwania musi być większy od 0")
    private Integer duration;

    @NotNull(message = "Liczba posiłków dziennie jest wymagana")
    @Positive(message = "Liczba posiłków musi być większa od 0")
    private Integer mealsPerDay;

    private Map<String, String> mealTimes;
    private List<String> mealTypes;
    private List<DietTemplateDayRequest> days;
    private DietTemplateNutritionRequest targetNutrition;
    private String notes;

    private ManualDietRequest dietData;
}
