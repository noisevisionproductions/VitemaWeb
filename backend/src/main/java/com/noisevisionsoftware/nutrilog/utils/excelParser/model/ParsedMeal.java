package com.noisevisionsoftware.nutrilog.utils.excelParser.model;

import com.noisevisionsoftware.nutrilog.model.diet.MealType;
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
public class ParsedMeal {
    private String name;
    private String instructions;
    private List<ParsedProduct> ingredients;
    private NutritionalValues nutritionalValues;
    private MealType mealType;
    private String time;
    private List<String> photos;
}