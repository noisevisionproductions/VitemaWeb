package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service odpowiedzialny za walidację diet ręcznych
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DietValidationService {

    private static final int MIN_MEALS_PER_DAY = 1;
    private static final int MAX_MEALS_PER_DAY = 10;
    private static final int MIN_DIET_DURATION = 1;
    private static final int MAX_DIET_DURATION = 365;

    /**
     * Przeprowadza pełną walidację ręcznej diety
     */
    public Map<String, Object> validateManualDiet(ManualDietRequest request) {
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Walidacja podstawowych pól
        validateBasicFields(request, errors);

        // Walidacja dat
        validateDates(request, errors, warnings);

        // Walidacja dni i posiłków
        validateDaysAndMeals(request, errors, warnings);

        // Walidacja składników
        validateIngredients(request, warnings);

        validation.put("isValid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("summary", createValidationSummary(request, errors, warnings));

        return validation;
    }

    /**
     * Szybka walidacja tylko krytycznych pól
     */
    public boolean isValidForSaving(ManualDietRequest request) {
        if (request == null) return false;
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) return false;
        if (request.getDays() == null || request.getDays().isEmpty()) return false;
        if (request.getMealsPerDay() <= 0) return false;

        return true;
    }

    /**
     * Waliduje pojedynczy dzień diety
     */
    public List<String> validateDay(ParsedDay day, int dayNumber, int expectedMealsCount) {
        List<String> errors = new ArrayList<>();

        if (day == null) {
            errors.add("Dzień " + dayNumber + " jest pusty");
            return errors;
        }

        if (day.getMeals() == null) {
            errors.add("Dzień " + dayNumber + " nie ma zdefiniowanych posiłków");
            return errors;
        }

        if (day.getMeals().size() != expectedMealsCount) {
            errors.add("Dzień " + dayNumber + " ma " + day.getMeals().size() +
                    " posiłków, oczekiwano " + expectedMealsCount);
        }

        // Walidacja każdego posiłku w dniu
        for (int i = 0; i < day.getMeals().size(); i++) {
            ParsedMeal meal = day.getMeals().get(i);
            errors.addAll(validateMeal(meal, dayNumber, i + 1));
        }

        return errors;
    }

    /**
     * Waliduje pojedynczy posiłek
     */
    public List<String> validateMeal(ParsedMeal meal, int dayNumber, int mealNumber) {
        List<String> errors = new ArrayList<>();

        if (meal == null) {
            errors.add("Posiłek " + mealNumber + " w dniu " + dayNumber + " jest pusty");
            return errors;
        }

        if (meal.getName() == null || meal.getName().trim().isEmpty()) {
            errors.add("Posiłek " + mealNumber + " w dniu " + dayNumber + " musi mieć nazwę");
        }

        if (meal.getIngredients() != null) {
            for (int i = 0; i < meal.getIngredients().size(); i++) {
                ParsedProduct ingredient = meal.getIngredients().get(i);
                List<String> ingredientErrors = validateIngredient(ingredient, dayNumber, mealNumber, i + 1);
                errors.addAll(ingredientErrors);
            }
        }

        return errors;
    }

    // Metody pomocnicze (private)

    private void validateBasicFields(ManualDietRequest request, List<String> errors) {
        if (request == null) {
            errors.add("Żądanie jest puste");
            return;
        }

        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            errors.add("ID użytkownika jest wymagane");
        }

        if (request.getMealsPerDay() < MIN_MEALS_PER_DAY || request.getMealsPerDay() > MAX_MEALS_PER_DAY) {
            errors.add("Liczba posiłków dziennie musi być między " + MIN_MEALS_PER_DAY + " a " + MAX_MEALS_PER_DAY);
        }

        if (request.getDuration() < MIN_DIET_DURATION || request.getDuration() > MAX_DIET_DURATION) {
            errors.add("Czas trwania diety musi być między " + MIN_DIET_DURATION + " a " + MAX_DIET_DURATION + " dni");
        }
    }

    private void validateDates(ManualDietRequest request, List<String> errors, List<String> warnings) {
        if (request.getStartDate() == null || request.getStartDate().trim().isEmpty()) {
            errors.add("Data rozpoczęcia diety jest wymagana");
            return;
        }

        try {
            LocalDate startDate = LocalDate.parse(request.getStartDate());
            LocalDate today = LocalDate.now();

            if (startDate.isBefore(today.minusDays(30))) {
                warnings.add("Data rozpoczęcia diety jest z przeszłości (ponad 30 dni temu)");
            }

            if (startDate.isAfter(today.plusDays(365))) {
                warnings.add("Data rozpoczęcia diety jest bardzo daleko w przyszłości");
            }
        } catch (DateTimeParseException e) {
            errors.add("Nieprawidłowy format daty rozpoczęcia. Oczekiwany format: YYYY-MM-DD");
        }
    }

    private void validateDaysAndMeals(ManualDietRequest request, List<String> errors, List<String> warnings) {
        if (request.getDays() == null || request.getDays().isEmpty()) {
            errors.add("Dieta musi zawierać przynajmniej jeden dzień");
            return;
        }

        if (request.getDays().size() != request.getDuration()) {
            warnings.add("Liczba dni w diecie (" + request.getDays().size() +
                    ") nie odpowiada deklarowanemu czasowi trwania (" + request.getDuration() + ")");
        }

        // Walidacja każdego dnia
        for (int i = 0; i < request.getDays().size(); i++) {
            ParsedDay day = request.getDays().get(i);
            List<String> dayErrors = validateDay(day, i + 1, request.getMealsPerDay());
            errors.addAll(dayErrors);
        }
    }

    private void validateIngredients(ManualDietRequest request, List<String> warnings) {
        int totalIngredients = 0;
        int emptyMeals = 0;

        for (ParsedDay day : request.getDays()) {
            if (day.getMeals() != null) {
                for (ParsedMeal meal : day.getMeals()) {
                    if (meal.getIngredients() == null || meal.getIngredients().isEmpty()) {
                        emptyMeals++;
                    } else {
                        totalIngredients += meal.getIngredients().size();
                    }
                }
            }
        }

        if (emptyMeals > 0) {
            warnings.add("Liczba posiłków bez składników: " + emptyMeals);
        }

        if (totalIngredients == 0) {
            warnings.add("Dieta nie zawiera żadnych składników");
        }
    }

    private List<String> validateIngredient(ParsedProduct ingredient, int dayNumber, int mealNumber, int ingredientNumber) {
        List<String> errors = new ArrayList<>();
        String location = "składnik " + ingredientNumber + " w posiłku " + mealNumber + " dnia " + dayNumber;

        if (ingredient == null) {
            errors.add("Pusty " + location);
            return errors;
        }

        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            errors.add("Brak nazwy dla " + location);
        }

        if (ingredient.getQuantity() == null || ingredient.getQuantity() <= 0) {
            errors.add("Nieprawidłowa ilość dla " + location + " (musi być większa od 0)");
        }

        if (ingredient.getUnit() == null || ingredient.getUnit().trim().isEmpty()) {
            errors.add("Brak jednostki dla " + location);
        }

        return errors;
    }

    private Map<String, Object> createValidationSummary(ManualDietRequest request, List<String> errors, List<String> warnings) {
        Map<String, Object> summary = new HashMap<>();

        if (request != null) {
            summary.put("totalDays", request.getDays() != null ? request.getDays().size() : 0);
            summary.put("mealsPerDay", request.getMealsPerDay());
            summary.put("duration", request.getDuration());

            int totalMeals = 0;
            int totalIngredients = 0;

            if (request.getDays() != null) {
                for (ParsedDay day : request.getDays()) {
                    if (day.getMeals() != null) {
                        totalMeals += day.getMeals().size();
                        for (ParsedMeal meal : day.getMeals()) {
                            if (meal.getIngredients() != null) {
                                totalIngredients += meal.getIngredients().size();
                            }
                        }
                    }
                }
            }

            summary.put("totalMeals", totalMeals);
            summary.put("totalIngredients", totalIngredients);
        }

        summary.put("errorCount", errors.size());
        summary.put("warningCount", warnings.size());

        return summary;
    }
}