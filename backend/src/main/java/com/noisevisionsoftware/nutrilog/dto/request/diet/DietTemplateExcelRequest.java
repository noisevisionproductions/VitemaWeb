package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.noisevisionsoftware.nutrilog.model.meal.MealType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateExcelRequest {
    private MultipartFile file;
    private int mealsPerDay;
    private String startDate;
    private int duration;
    private Map<String, String> mealTimes;
    private List<MealType> mealTypes;
    private Integer skipColumnsCount;
    private Boolean calorieValidationEnabled;
    private Integer targetCalories;
    private Integer calorieErrorMargin;

    // Metoda pomocnicza do sprawdzenia, czy walidacja kalorii jest wymagana
    public boolean isCalorieValidationRequired() {
        return Boolean.TRUE.equals(calorieValidationEnabled) &&
                targetCalories != null &&
                targetCalories > 0;
    }
}