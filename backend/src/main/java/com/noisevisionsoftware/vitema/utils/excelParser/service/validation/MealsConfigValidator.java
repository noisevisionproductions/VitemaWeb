package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MealsConfigValidator {

    public List<ValidationResult> validateMealConfig(Map<String, String> mealTimes, List<MealType> mealTypes) {
        List<ValidationResult> validations = new ArrayList<>();

        if (mealTimes == null || mealTypes == null) {
            validations.add(new ValidationResult(
                    false,
                    "Brak konfiguracji czasów lub typów posiłków",
                    ValidationSeverity.ERROR
            ));
            return validations;
        }

        // Walidacja kolejności czasów posiłków
        for (int i = 0; i < mealTypes.size() - 1; i++) {
            int currentTime = getTimeInMinutes(mealTimes.get("meal_" + i));
            int nextTime = getTimeInMinutes(mealTimes.get("meal_" + (i + 1)));
            int timeDiff = nextTime - currentTime;

            if (timeDiff <= 0) {
                validations.add(new ValidationResult(
                        false,
                        String.format("%s nie może być wcześniej niż %s",
                                MealType.getMealTypeLabel(mealTypes.get(i + 1)),
                                MealType.getMealTypeLabel(mealTypes.get(i))),
                        ValidationSeverity.ERROR
                ));
            } else if (timeDiff < 120) {
                validations.add(new ValidationResult(
                        true,
                        String.format("Odstęp między %s a %s jest mniejszy niż 2 godziny",
                                MealType.getMealTypeLabel(mealTypes.get(i)),
                                MealType.getMealTypeLabel(mealTypes.get(i + 1))),
                        ValidationSeverity.WARNING
                ));
            }
        }

        // Walidacja typowych pór posiłków
        for (int i = 0; i < mealTypes.size(); i++) {
            int time = getTimeInMinutes(mealTimes.get("meal_" + i));
            MealType type = mealTypes.get(i);

            validateMealTypeTime(type, time).ifPresent(validations::add);
        }

        // Walidacja duplikatów typów posiłków
        Map<MealType, Long> typeCounts = mealTypes.stream()
                .collect(Collectors.groupingBy(type -> type, Collectors.counting()));

        typeCounts.forEach((type, count) -> {
            if (count > 1) {
                validations.add(new ValidationResult(
                        false,
                        String.format("%s występuje %d razy", MealType.getMealTypeLabel(type), count),
                        ValidationSeverity.ERROR
                ));
            }
        });

        return validations;
    }

    private boolean isValidTimeFormat(String time) {
        return time != null && time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private int getTimeInMinutes(String time) {
        if (!isValidTimeFormat(time)) {
            throw new IllegalArgumentException("Nieprawidłowy format czasu: " + time);
        }

        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private Optional<ValidationResult> validateMealTypeTime(MealType type, int timeInMinutes) {
        if (type == null) {
            return Optional.empty();
        }

        switch (type) {
            case BREAKFAST:
                if (timeInMinutes < 240 || timeInMinutes > 600) {
                    return Optional.of(new ValidationResult(
                            true,
                            String.format("Nietypowa pora na %s", MealType.getMealTypeLabel(type)),
                            ValidationSeverity.WARNING
                    ));
                }
                break;
            case DINNER:
                if (timeInMinutes > 1440) {
                    return Optional.of(new ValidationResult(
                            true,
                            String.format("Bardzo późna pora na %s", MealType.getMealTypeLabel(type)),
                            ValidationSeverity.WARNING
                    ));
                }
                break;
        }
        return Optional.empty();
    }
}