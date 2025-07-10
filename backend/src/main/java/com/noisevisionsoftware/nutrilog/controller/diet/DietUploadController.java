package com.noisevisionsoftware.nutrilog.controller.diet;

import com.noisevisionsoftware.nutrilog.dto.request.diet.CalorieValidationRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.DietTemplateExcelRequest;
import com.noisevisionsoftware.nutrilog.dto.response.DietPreviewResponse;
import com.noisevisionsoftware.nutrilog.dto.response.ErrorResponse;
import com.noisevisionsoftware.nutrilog.dto.response.ValidationResponse;
import com.noisevisionsoftware.nutrilog.exception.DietValidationException;
import com.noisevisionsoftware.nutrilog.model.meal.MealType;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationSeverity;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.DietExcelTemplateService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.ExcelParserService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation.CalorieValidator;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation.ExcelStructureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diets/upload")
@RequiredArgsConstructor
@Slf4j
public class DietUploadController {

    private final ExcelStructureValidator excelStructureValidator;
    private final ExcelParserService excelParserService;
    private final DietExcelTemplateService dietExcelTemplateService;
    private final CalorieValidator calorieValidator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @ExceptionHandler(DietValidationException.class)
    public ResponseEntity<ValidationResponse> handleDietValidationException(DietValidationException ex) {
        return ResponseEntity.badRequest()
                .body(new ValidationResponse(
                        false,
                        ex.getValidationResults(),
                        Collections.emptyMap()
                ));
    }

    @PostMapping(value = "/validate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationResponse> validateFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ValidationResponse(
                                false,
                                Collections.singletonList(
                                        new ValidationResult(false, "Przesłany plik jest pusty", ValidationSeverity.ERROR)
                                ),
                                Collections.emptyMap()
                        ));
            }

            List<ValidationResult> results = excelStructureValidator.validateExcelStructure(file);
            boolean hasErrors = results.stream()
                    .anyMatch(result -> result.severity() == ValidationSeverity.ERROR);

            // Dodaj dodatkowe dane (np. liczba posiłków)
            Map<String, Object> additionalData = new HashMap<>();
            String firstSuccessMsg = results.stream()
                    .filter(r -> r.isValid() && r.severity() == ValidationSeverity.SUCCESS)
                    .map(ValidationResult::message)
                    .findFirst().orElse("");

            // Wyciągnij liczbę posiłków z komunikatu
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Znaleziono (\\d+) posiłków");
            java.util.regex.Matcher matcher = pattern.matcher(firstSuccessMsg);
            if (matcher.find()) {
                additionalData.put("totalMeals", Integer.parseInt(matcher.group(1)));
            }

            return ResponseEntity.ok(new ValidationResponse(
                    !hasErrors,
                    results,
                    additionalData
            ));
        } catch (Exception e) {
            log.error("Błąd podczas walidacji pliku", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ValidationResponse(
                            false,
                            Collections.singletonList(
                                    new ValidationResult(false,
                                            "Wystąpił błąd podczas przetwarzania pliku: " + e.getMessage(),
                                            ValidationSeverity.ERROR)
                            ),
                            Collections.emptyMap()
                    ));
        }
    }

    @PostMapping(value = "/validate-template",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationResponse> validateDietTemplate(
            @ModelAttribute DietTemplateExcelRequest request) {
        try {
            ValidationResponse response = dietExcelTemplateService.validateDietTemplate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Błąd podczas walidacji szablonu diety", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ValidationResponse(
                            false,
                            Collections.singletonList(new ValidationResult(
                                    false,
                                    "Wystąpił nieoczekiwany błąd: " + e.getMessage(),
                                    ValidationSeverity.ERROR
                            )),
                            new HashMap<>()
                    ));
        }
    }

    @PostMapping(value = "/validate-template-with-user",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationResponse> validateDietTemplateWithUser(
            @ModelAttribute DietTemplateExcelRequest request,
            @RequestParam(required = false) String userId) {
        try {
            ValidationResponse response = dietExcelTemplateService.validateDietTemplate(request, userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Błąd podczas walidacji szablonu diety", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ValidationResponse(
                            false,
                            Collections.singletonList(new ValidationResult(
                                    false,
                                    "Wystąpił nieoczekiwany błąd: " + e.getMessage(),
                                    ValidationSeverity.ERROR
                            )),
                            new HashMap<>()
                    ));
        }
    }

    @PostMapping(value = "/preview",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> previewDiet(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mealsPerDay") int mealsPerDay,
            @RequestParam("startDate") String startDate,
            @RequestParam("duration") int duration,
            @RequestParam Map<String, String> allParams,
            @RequestParam("mealTypes") List<String> mealTypes,
            @RequestParam(value = "skipColumnsCount", required = false) Integer skipColumnsCount,
            @RequestParam(value = "calorieValidationEnabled", required = false) Boolean calorieValidationEnabled,
            @RequestParam(value = "targetCalories", required = false) Integer targetCalories,
            @RequestParam(value = "calorieErrorMargin", required = false) Integer calorieErrorMargin
    ) {
        Map<String, String> mealTimes = new HashMap<>();
        for (int i = 0; i < mealsPerDay; i++) {
            String key = "meal_" + i;
            String timeKey = "mealTimes[" + key + "]";
            if (allParams.containsKey(timeKey)) {
                mealTimes.put(key, allParams.get(timeKey));
            }
        }

        try {
            validateInput(file, mealsPerDay, startDate, duration, mealTimes, mealTypes);

            // Walidacja parametrów kalorii
            if (Boolean.TRUE.equals(calorieValidationEnabled) && (targetCalories == null || targetCalories <= 0)) {
                return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("Walidacja kalorii wymaga poprawnej wartości docelowej"));
            }

            ExcelParserService.ParsedExcelResult parseResult;
            if (skipColumnsCount != null) {
                parseResult = excelParserService.parseDietExcel(file, skipColumnsCount);
            } else {
                parseResult = excelParserService.parseDietExcel(file);
            }

            List<ParsedDay> days = generateDietDays(
                    parseResult.meals(),
                    mealsPerDay,
                    startDate,
                    duration,
                    extractMealTimes(mealTimes),
                    mealTypes
            );

            Map<String, Object> additionalData = new HashMap<>();

            if (Boolean.TRUE.equals(calorieValidationEnabled) && targetCalories != null) {
                DietPreviewResponse response = validateAndProcessCalories(
                        parseResult.meals(),
                        days,
                        parseResult.shoppingList(),
                        mealsPerDay,
                        targetCalories,
                        calorieErrorMargin
                );

                if (response != null) {
                    return ResponseEntity.ok(response);
                }

                additionalData.put("validCalories", true);
            }

            return ResponseEntity.ok(DietPreviewResponse.builder()
                    .days(days)
                    .shoppingList(parseResult.shoppingList())
                    .valid(true)
                    .additionalData(additionalData)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Błąd walidacji: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Błąd podczas generowania podglądu diety", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Błąd podczas generowania podglądu diety: " + e.getMessage()));
        }
    }

    private Map<String, String> extractMealTimes(Map<String, String> allParams) {
        return allParams.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("mealTimes["))
                .collect(Collectors.toMap(
                        entry -> entry.getKey().replace("mealTimes[", "").replace("]", ""),
                        Map.Entry::getValue
                ));
    }

    private void validateInput(MultipartFile file, int mealsPerDay, String startDate,
                               int duration, Map<String, String> mealTimes, List<String> mealTypes) {
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("Plik jest wymagany");
        }

        if (mealsPerDay <= 0 || mealsPerDay > 10) {
            errors.add("Liczba posiłków musi być większa od 0 i nie większa niż 10");
        }

        if (duration <= 0 || duration > 90) {
            errors.add("Długość diety musi być większa od 0 i nie większa niż 90 dni");
        }

        if (mealTypes == null || mealTypes.isEmpty()) {
            errors.add("Typy posiłków są wymagane");
        } else if (mealTypes.size() != mealsPerDay) {
            errors.add("Liczba typów posiłków musi być równa liczbie posiłków dziennie");
        }

        try {
            LocalDate.parse(startDate);
        } catch (DateTimeParseException e) {
            errors.add("Nieprawidłowy format daty rozpoczęcia");
        }

        for (int i = 0; i < mealsPerDay; i++) {
            String timeKey = "meal_" + i;
            String time = mealTimes.get(timeKey);
            if (time == null || !time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                errors.add("Nieprawidłowy format godziny dla posiłku " + (i + 1));
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    /*
     * Waliduje i przetwarza informacje o kaloriach w diecie
     * */
    private DietPreviewResponse validateAndProcessCalories(
            List<ParsedMeal> meals,
            List<ParsedDay> days,
            List<Map.Entry<String, ParsedProduct>> shoppingList,
            int mealsPerDay,
            int targetCalories,
            Integer errorMargin
    ) {
        Map<String, Object> additionalData = new HashMap<>();

        CalorieValidationRequest calorieValidation = new CalorieValidationRequest(
                true,
                targetCalories,
                errorMargin != null ? errorMargin : 5
        );

        ValidationResult validationResult = calorieValidator.validateCalories(
                meals,
                calorieValidation,
                mealsPerDay
        );

        CalorieValidator.CalorieAnalysisResult analysis = calorieValidator.analyzeCalories(meals, mealsPerDay);

        if (analysis != null) {
            additionalData.put("calorieAnalysis", analysis);
        }

        additionalData.put("calorieValidation", validationResult);

        if (!validationResult.isValid()) {
            return DietPreviewResponse.builder()
                    .days(days)
                    .shoppingList(shoppingList)
                    .valid(false)
                    .validationMessage(validationResult.message())
                    .additionalData(additionalData)
                    .build();
        }

        return null;
    }

    private List<ParsedDay> generateDietDays(
            List<ParsedMeal> meals,
            int mealsPerDay,
            String startDate,
            int duration,
            Map<String, String> mealTimes,
            List<String> mealTypes
    ) {
        List<ParsedDay> days = new ArrayList<>();
        LocalDate currentDate = LocalDate.parse(startDate);

        int mealIndex = 0;
        for (int dayIndex = 0; dayIndex < duration; dayIndex++) {
            List<ParsedMeal> dayMeals = new ArrayList<>();

            for (int i = 0; i < mealsPerDay; i++) {
                if (mealIndex >= meals.size()) {
                    log.warn("Brak wystarczającej liczby posiłków");
                    break;
                }

                ParsedMeal meal = meals.get(mealIndex++);
                meal.setMealType(MealType.valueOf(mealTypes.get(i)));
                meal.setTime(mealTimes.get("meal_" + i));
                dayMeals.add(meal);
            }

            ParsedDay day = ParsedDay.builder()
                    .date(com.google.cloud.Timestamp.of(Timestamp.valueOf(currentDate.atStartOfDay())))
                    .meals(dayMeals)
                    .build();

            days.add(day);
            currentDate = currentDate.plusDays(1);
        }

        return days;
    }
}
