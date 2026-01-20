package com.noisevisionsoftware.vitema.utils.excelParser.service;

import com.noisevisionsoftware.vitema.dto.request.diet.CalorieValidationRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DietTemplateExcelRequest;
import com.noisevisionsoftware.vitema.dto.response.ValidationResponse;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import com.noisevisionsoftware.vitema.utils.excelParser.service.validation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DietExcelTemplateService {

    private final ExcelStructureValidator excelStructureValidator;
    private final MealsPerDayValidator mealsPerDayValidator;
    private final DateValidator dateValidator;
    private final MealsConfigValidator mealsConfigValidator;
    private final ExcelParserService excelParserService;
    private final ValidationCacheService cacheService;
    private final DietOverlapValidator dietOverlapValidator;
    private final CalorieValidator calorieValidator;

    /**
     * Waliduje szablon diety na podstawie przesłanych parametrów.
     */
    public ValidationResponse validateDietTemplate(DietTemplateExcelRequest request, String userId) {
        String cacheKey = cacheService.generateCacheKey(request, userId);

        // Sprawdź cache
        Optional<ValidationResponse> cachedResponse = cacheService.getFromCache(cacheKey);
        if (cachedResponse.isPresent()) {
            return cachedResponse.get();
        }

        // Wstępna walidacja parametrów
        ValidationResult basicValidation = validateBasicParameters(request);
        if (basicValidation != null && !basicValidation.isValid()) {
            return createErrorResponse(Collections.singletonList(basicValidation));
        }

        List<ValidationResult> allValidations = new ArrayList<>();
        Map<String, Object> additionalData = new HashMap<>();

        try {
            // Walidacja struktury Excel
            List<ValidationResult> excelValidation = excelStructureValidator.validateExcelStructure(request.getFile());
            allValidations.addAll(excelValidation);
            if (containsErrors(excelValidation)) {
                return createErrorResponse(allValidations);
            }

            // Parsowanie Excel
            ExcelParserService.ParsedExcelResult parseResult = parseExcelFile(request, allValidations, additionalData);
            if (parseResult == null) {
                return createErrorResponse(allValidations);
            }

            // Walidacja liczby posiłków
            ValidationResult mealsValidation = validateMealsCount(request, parseResult, allValidations);
            if (mealsValidation != null && !mealsValidation.isValid()) {
                return createErrorResponse(allValidations);
            }

            // Walidacja dat
            ValidationResult dateValidation = validateDates(request, parseResult, allValidations);
            if (dateValidation != null && !dateValidation.isValid()) {
                return createErrorResponse(allValidations);
            }

            // Walidacja konfiguracji posiłków
            List<ValidationResult> mealsConfigValidation = validateMealsConfig(request, allValidations);
            if (containsErrors(mealsConfigValidation)) {
                return createErrorResponse(allValidations);
            }

            // Walidacja nakładania się diet
            if (userId != null && !userId.isEmpty()) {
                try {
                    LocalDate startDate = LocalDate.parse(request.getStartDate());
                    ValidationResult overlapValidation = dietOverlapValidator.validateDietOverlap(
                            userId, startDate, request.getDuration());

                    allValidations.add(overlapValidation);

                    if (!overlapValidation.isValid()) {
                        return createErrorResponse(allValidations);
                    }
                } catch (Exception e) {
                    log.error("Error during diet overlap validation", e);
                    allValidations.add(new ValidationResult(
                            false,
                            "Błąd podczas sprawdzania nakładania się diet: " +
                                    (e.getMessage() != null ? e.getMessage() : "sprawdź format daty"),
                            ValidationSeverity.ERROR
                    ));
                    return createErrorResponse(allValidations);
                }
            }

            // Walidacja kalorii
            if (request.isCalorieValidationRequired()) {
                try {
                    CalorieValidationRequest calorieReq = new CalorieValidationRequest(
                            request.getCalorieValidationEnabled(),
                            request.getTargetCalories(),
                            request.getCalorieErrorMargin() != null ? request.getCalorieErrorMargin() : 5
                    );

                    ValidationResult calorieValidation = calorieValidator.validateCalories(
                            parseResult.meals(),
                            calorieReq,
                            request.getMealsPerDay()
                    );

                    allValidations.add(calorieValidation);

                    CalorieValidator.CalorieAnalysisResult calorieAnalysis = calorieValidator.analyzeCalories(parseResult.meals(), request.getMealsPerDay());

                    if (calorieAnalysis != null) {
                        additionalData.put("calorieAnalysis", calorieAnalysis);
                    }

                    if (!calorieValidation.isValid()) {
                        return createErrorResponse(allValidations);
                    }
                } catch (Exception e) {
                    log.error("Error during calorie validation", e);
                    allValidations.add(new ValidationResult(
                            false,
                            "Błąd podczas walidacji kalorii: " + (e.getMessage() != null ? e.getMessage() : "Sprawdź wartości kaloryczne"),
                            ValidationSeverity.ERROR
                    ));

                    return createErrorResponse(allValidations);
                }
            }

            // Dodaj podsumowanie
            allValidations.add(new ValidationResult(
                    true,
                    "Wszystkie walidacje zakończone pomyślnie. Dieta gotowa do zapisu.",
                    ValidationSeverity.SUCCESS
            ));

            ValidationResponse response = createSuccessResponse(allValidations, additionalData);
            cacheService.putInCache(cacheKey, response);
            return response;

        } catch (Exception e) {
            log.error("Błąd podczas walidacji szablonu diety", e);
            allValidations.add(new ValidationResult(
                    false,
                    "Wystąpił nieoczekiwany błąd: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
            return createErrorResponse(allValidations);
        }
    }

    public ValidationResponse validateDietTemplate(DietTemplateExcelRequest request) {
        return validateDietTemplate(request, null);
    }

    private ValidationResult validateBasicParameters(DietTemplateExcelRequest request) {
        if (request.getFile() == null || request.getFile().isEmpty()) {
            return new ValidationResult(false, "Plik jest wymagany", ValidationSeverity.ERROR);
        }

        if (request.getMealsPerDay() <= 0) {
            return new ValidationResult(false, "Liczba posiłków musi być większa od 0", ValidationSeverity.ERROR);
        }

        if (request.getMealsPerDay() > 10) {
            return new ValidationResult(false, "Zbyt duża liczba posiłków dziennie (maksymalnie 10)", ValidationSeverity.ERROR);
        }

        if (request.getDuration() <= 0) {
            return new ValidationResult(false, "Długość diety musi być większa od 0", ValidationSeverity.ERROR);
        }

        if (request.getDuration() > 90) {
            return new ValidationResult(false, "Zbyt długi okres diety (maksymalnie 90 dni)", ValidationSeverity.ERROR);
        }

        if (request.getStartDate() == null) {
            return new ValidationResult(false, "Data rozpoczęcia diety jest wymagana", ValidationSeverity.ERROR);
        }

        // Walidacja typów i czasów posiłków
        if (request.getMealTypes() == null || request.getMealTypes().isEmpty()) {
            return new ValidationResult(false, "Typy posiłków są wymagane", ValidationSeverity.ERROR);
        }

        if (request.getMealTimes() == null || request.getMealTimes().isEmpty()) {
            return new ValidationResult(false, "Czasy posiłków są wymagane", ValidationSeverity.ERROR);
        }

        if (request.getMealTypes().size() != request.getMealsPerDay()) {
            return new ValidationResult(false,
                    "Liczba typów posiłków musi być równa liczbie posiłków dziennie",
                    ValidationSeverity.ERROR);
        }

        for (int i = 0; i < request.getMealsPerDay(); i++) {
            String timeKey = "meal_" + i;
            if (!request.getMealTimes().containsKey(timeKey) ||
                    request.getMealTimes().get(timeKey) == null ||
                    request.getMealTimes().get(timeKey).trim().isEmpty()) {
                return new ValidationResult(false,
                        "Czas dla posiłku " + (i + 1) + " jest wymagany",
                        ValidationSeverity.ERROR);
            }
        }

        // Walidacja kalorii
        if (request.isCalorieValidationRequired()) {
            if (request.getTargetCalories() <= 0) {
                return new ValidationResult(false,
                        "Wartość docelowa kalorii musi być większa od 0",
                        ValidationSeverity.ERROR);
            }

            Integer marginPercent = request.getCalorieErrorMargin();
            if (marginPercent != null && (marginPercent < 1 || marginPercent > 20)) {
                return new ValidationResult(false,
                        "Margines błędu musi być z zakresu 1-20%",
                        ValidationSeverity.ERROR);
            }
        }

        return null;
    }

    private ExcelParserService.ParsedExcelResult parseExcelFile(
            DietTemplateExcelRequest request,
            List<ValidationResult> allValidations,
            Map<String, Object> additionalData) {

        try {
            ExcelParserService.ParsedExcelResult parseResult;

            if (request.getSkipColumnsCount() != null) {
                parseResult = excelParserService.parseDietExcel(request.getFile(), request.getSkipColumnsCount());
            } else {
                parseResult = excelParserService.parseDietExcel(request.getFile());
            }

            // Dodanie danych do response
            additionalData.put("totalMeals", parseResult.totalMeals());
            additionalData.put("meals", parseResult.meals());
            additionalData.put("shoppingList", parseResult.shoppingList());

            return parseResult;
        } catch (Exception e) {
            log.error("Błąd podczas parsowania pliku Excel", e);
            allValidations.add(new ValidationResult(
                    false,
                    "Błąd podczas parsowania pliku: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
            return null;
        }
    }

    private ValidationResult validateMealsCount(
            DietTemplateExcelRequest request,
            ExcelParserService.ParsedExcelResult parseResult,
            List<ValidationResult> allValidations) {

        ValidationResult validation = mealsPerDayValidator.validateMealsCount(
                parseResult.totalMeals(),
                request.getMealsPerDay()
        );

        allValidations.add(validation);
        return validation.isValid() ? null : validation;
    }

    private ValidationResult validateDates(
            DietTemplateExcelRequest request,
            ExcelParserService.ParsedExcelResult parseResult,
            List<ValidationResult> allValidations) {

        ValidationResult validation = dateValidator.validateDate(
                request.getStartDate(),
                request.getMealsPerDay(),
                parseResult.totalMeals(),
                request.getDuration()
        );

        allValidations.add(validation);
        return validation.isValid() ? null : validation;
    }

    private List<ValidationResult> validateMealsConfig(
            DietTemplateExcelRequest request,
            List<ValidationResult> allValidations) {

        List<ValidationResult> validations = mealsConfigValidator.validateMealConfig(
                request.getMealTimes(),
                request.getMealTypes()
        );

        allValidations.addAll(validations);
        return validations;
    }

    private boolean containsErrors(List<ValidationResult> validations) {
        return validations.stream()
                .anyMatch(v -> v.severity() == ValidationSeverity.ERROR && !v.isValid());
    }

    private ValidationResponse createErrorResponse(List<ValidationResult> validations) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(false);
        response.setValidationResults(validations);
        response.setAdditionalData(new HashMap<>());
        return response;
    }

    private ValidationResponse createSuccessResponse(
            List<ValidationResult> validations,
            Map<String, Object> additionalData) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(true);
        response.setValidationResults(validations);
        response.setAdditionalData(additionalData);
        return response;
    }
}