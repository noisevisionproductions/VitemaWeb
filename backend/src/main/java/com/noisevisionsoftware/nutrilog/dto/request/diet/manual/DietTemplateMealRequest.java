package com.noisevisionsoftware.nutrilog.dto.request.diet.manual;

import com.noisevisionsoftware.nutrilog.dto.request.recipe.NutritionalValuesRequest;
import com.noisevisionsoftware.nutrilog.model.meal.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateMealRequest {

    @NotBlank(message = "Nazwa posiłku jest wymagana")
    private String name;

    @NotNull(message = "Typ posiłku jest wymagany")
    private MealType mealType;

    @NotBlank(message = "Godzina posiłku jest wymagana")
    private String time;

    private String instructions;
    private String mealTemplateId;

    @Valid
    private List<DietTemplateIngredientRequest> ingredients;

    @Valid
    private NutritionalValuesRequest nutritionalValues;

    private List<String> photos;
}