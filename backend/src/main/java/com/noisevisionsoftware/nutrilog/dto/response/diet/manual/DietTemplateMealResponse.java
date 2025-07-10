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
public class DietTemplateMealResponse {
    private String name;
    private String mealType;
    private String time;
    private String instructions;
    private List<DietTemplateIngredientResponse> ingredients;
    private Object nutritionalValues;
    private List<String> photos;
    private String mealTemplateId;
}