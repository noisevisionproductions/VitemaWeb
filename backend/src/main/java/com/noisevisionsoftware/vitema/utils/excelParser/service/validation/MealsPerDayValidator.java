package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.springframework.stereotype.Service;

@Service
public class MealsPerDayValidator {

    public ValidationResult validateMealsCount(int totalMeals, int mealsPerDay) {
        if (totalMeals == 0) {
            return new ValidationResult(
                    false,
                    "Nie wczytano żadnych posiłków z pliku Excel.",
                    ValidationSeverity.ERROR
            );
        }

        int remainder = totalMeals % mealsPerDay;
        int numberOfDays = totalMeals / mealsPerDay;

        if (remainder == 0) {
            return new ValidationResult(
                    true,
                    String.format("Plik zawiera %d pełnych dni po %d posiłków.", numberOfDays, mealsPerDay),
                    ValidationSeverity.SUCCESS
            );
        }

        return new ValidationResult(
                false,
                String.format("Liczba posiłków (%d) nie jest podzielna przez %d. Brakuje %d posiłków do pełnego dnia.",
                        totalMeals, mealsPerDay, mealsPerDay - remainder),
                ValidationSeverity.ERROR
        );
    }
}