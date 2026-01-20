package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.dto.request.diet.CalorieValidationRequest;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * Validator sprawdzający zgodność kalorii w diecie z wartością oczekiwaną
 * */
@Service
@Slf4j
public class CalorieValidator {

    /*
     * Waliduje, czy średnia ilość kalorii w posiłkach mieści się w określonych granicach
     * @param meals posiłki do sprawdzenia
     * */
    public ValidationResult validateCalories(
            List<ParsedMeal> meals,
            CalorieValidationRequest validation,
            int mealsPerDay
    ) {
        if (!validation.isValidationEnabled() || validation.getTargetCalories() == null) {
            return new ValidationResult(true, "Walidacja kalorii pominięta", ValidationSeverity.SUCCESS);
        }

        double totalCalories = 0;
        int mealsWithNutrition = 0;

        for (ParsedMeal meal : meals) {
            if (meal.getNutritionalValues() != null && meal.getNutritionalValues().getCalories() != null) {
                totalCalories += meal.getNutritionalValues().getCalories();
                mealsWithNutrition++;
            }
        }

        if (mealsWithNutrition == 0) {
            return new ValidationResult(
                    false,
                    "Brak informacji o kaloriach w posiłkach. Uzupelnij dane lub wyłącz walidację kalorii.",
                    ValidationSeverity.ERROR
            );
        }

        int daysCount = (int) Math.ceil((double) meals.size() / mealsPerDay);
        if (daysCount == 0) daysCount = 1;

        double averageDailyCalories = totalCalories / daysCount;

        int marginPercent = validation.getErrorMarginPercent() != null ? validation.getErrorMarginPercent() : 5;

        double margin = (validation.getTargetCalories() * marginPercent) / 100.0;
        double lowerLimit = validation.getTargetCalories() - margin;
        double upperLimit = validation.getTargetCalories() + margin;

        boolean isWithingLimit = averageDailyCalories >= lowerLimit && averageDailyCalories <= upperLimit;

        if (isWithingLimit) {
            return new ValidationResult(
                    true,
                    String.format(
                            "Walidacja kalorii zakończona pomyślnie. Średnio %.0f kcal dziennie (oczekiwano %d ±%d%%).",
                            averageDailyCalories, validation.getTargetCalories(), marginPercent
                    ),
                    ValidationSeverity.SUCCESS
            );
        } else {
            return new ValidationResult(
                    false,
                    String.format(
                            "Kalorie w diecie (%.0f kcal dziennie) różnią się od wartości docelowej (%d kcal) o więcej niż %d%%. " +
                                    "Dozwolony przedział: %.0f - %.0f kcal dziennie.",
                            averageDailyCalories, validation.getTargetCalories(), marginPercent, lowerLimit, upperLimit
                    ),
                    ValidationSeverity.ERROR
            );
        }
    }

    /*
     * Oblicza szczegółowe informacje o kaloriach dla każdego dnia diety
     *
     * */
    public CalorieAnalysisResult analyzeCalories(List<ParsedMeal> meals, int mealsPerDay) {
        if (meals == null || meals.isEmpty()) {
            return null;
        }

        List<Double> dailyCalories = getDoubles(meals, mealsPerDay);

        List<Double> validCalories = dailyCalories.stream()
                .filter(cal -> cal > 0)
                .toList();

        if (dailyCalories.isEmpty()) {
            return null;
        }

        double totalCalories = validCalories.stream().mapToDouble(Double::doubleValue).sum();
        double averageCalories = totalCalories / validCalories.size();

        double minCalories = validCalories.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxCalories = validCalories.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        boolean hasDailyVariation = (maxCalories - minCalories) > 100;

        return new CalorieAnalysisResult(
                (int) Math.round(averageCalories),
                dailyCalories.stream().map(d -> (int) Math.round(d)).toList(),
                hasDailyVariation
        );
    }

    private static List<Double> getDoubles(List<ParsedMeal> meals, int mealsPerDay) {
        List<Double> dailyCalories = new ArrayList<>();
        int days = (int) Math.ceil((double) meals.size() / mealsPerDay);

        for (int day = 0; day < days; day++) {
            double dayCalories = 0;
            boolean hasMealNutrition = false;

            int startIdx = day * mealsPerDay;
            int endIdx = Math.min((day + 1) * mealsPerDay, meals.size());

            for (int i = startIdx; i < endIdx; i++) {
                ParsedMeal meal = meals.get(i);
                if (meal.getNutritionalValues() != null && meal.getNutritionalValues().getCalories() != null) {
                    dayCalories += meal.getNutritionalValues().getCalories();
                    hasMealNutrition = true;
                }
            }

            dailyCalories.add(hasMealNutrition ? dayCalories : 0.0);
        }
        return dailyCalories;
    }

    /*
     * Klasa przechowująca wyniki analizy kalorii
     * */
    public record CalorieAnalysisResult(
            int averageCalories,
            List<Integer> dailyCalories,
            boolean hasDailyVariation
    ) {
    }
}
