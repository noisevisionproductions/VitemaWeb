package com.noisevisionsoftware.nutrilog.model.diet.template;

import com.noisevisionsoftware.nutrilog.model.meal.MealType;
import com.noisevisionsoftware.nutrilog.model.recipe.NutritionalValues;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateMealData {
    private String name;
    private MealType mealType;
    private String time;
    private String instructions;
    private List<DietTemplateIngredient> ingredients;
    private NutritionalValues nutritionalValues;
    private List<String> photos;
    private String mealTemplateId;
}
