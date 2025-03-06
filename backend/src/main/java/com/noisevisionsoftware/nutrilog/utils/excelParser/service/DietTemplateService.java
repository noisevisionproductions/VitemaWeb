package com.noisevisionsoftware.nutrilog.utils.excelParser.service;

import com.noisevisionsoftware.nutrilog.dto.request.DietTemplateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.ValidationResponse;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationSeverity;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation.DateValidator;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation.ExcelStructureValidator;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation.MealsConfigValidator;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation.MealsPerDayValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@AllArgsConstructor
public class DietTemplateService {
    private final ExcelStructureValidator excelStructureValidator;
    private final MealsPerDayValidator mealsPerDayValidator;
    private final DateValidator dateValidator;
    private final MealsConfigValidator mealsConfigValidator;
    private final ExcelParserService excelParserService;

    private final Map<String, ValidationResponse> validationCache = new ConcurrentHashMap<>(100);
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>(100);
    private static final long CACHE_TTL_MS = 10 * 60 * 1000;

    /**
     * Waliduje szablon diety na podstawie przesłanych parametrów.
     * Wykonuje kompleksową walidację pliku Excel, liczby posiłków, dat i konfiguracji posiłków.
     *
     * @param request obiekt zawierający dane szablonu diety do walidacji
     * @return obiekt zawierający wyniki walidacji
     */
    public ValidationResponse validateDietTemplate(DietTemplateRequest request) {
        String cacheKey = generateCacheKey(request);

        // Czyszczenie przeterminowanych wpisów
        cleanExpiredCacheEntries();

        // Sprawdzamy, czy mamy już zwalidowany ten plik z tymi parametrami
        if (validationCache.containsKey(cacheKey)) {
            log.debug("Cache hit for validation request, returning cached response");
            return validationCache.get(cacheKey);
        }

        List<ValidationResult> allValidations = new ArrayList<>();
        Map<String, Object> additionalData = new HashMap<>();
        boolean isValid = true;

        try {
            // Wstępna walidacja parametrów wejściowych
            if (request.getFile() == null || request.getFile().isEmpty()) {
                return createResponse(false,
                        Collections.singletonList(
                                new ValidationResult(false, "Plik jest wymagany", ValidationSeverity.ERROR)
                        ),
                        additionalData, cacheKey);
            }

            if (request.getMealsPerDay() <= 0) {
                return createResponse(false,
                        Collections.singletonList(
                                new ValidationResult(false, "Liczba posiłków musi być większa od 0", ValidationSeverity.ERROR)
                        ),
                        additionalData, cacheKey);
            }

            if (request.getMealsPerDay() > 10) { // Górny limit posiłków dziennie
                return createResponse(false,
                        Collections.singletonList(
                                new ValidationResult(false, "Zbyt duża liczba posiłków dziennie (maksymalnie 10)", ValidationSeverity.ERROR)
                        ),
                        additionalData, cacheKey);
            }

            if (request.getDuration() <= 0) {
                return createResponse(false,
                        Collections.singletonList(
                                new ValidationResult(false, "Długość diety musi być większa od 0", ValidationSeverity.ERROR)
                        ),
                        additionalData, cacheKey);
            }

            if (request.getDuration() > 90) { // Górny limit długości diety
                return createResponse(false,
                        Collections.singletonList(
                                new ValidationResult(false, "Zbyt długi okres diety (maksymalnie 90 dni)", ValidationSeverity.ERROR)
                        ),
                        additionalData, cacheKey);
            }

            if (request.getStartDate() == null) {
                return createResponse(false,
                        Collections.singletonList(
                                new ValidationResult(false, "Data rozpoczęcia diety jest wymagana", ValidationSeverity.ERROR)
                        ),
                        additionalData, cacheKey);
            }

            // 1. Walidacja struktury pliku Excel
            List<ValidationResult> excelValidation = excelStructureValidator.validateExcelStructure(request.getFile());
            allValidations.addAll(excelValidation);
            if (containsErrors(excelValidation)) {
                return createResponse(false, allValidations, additionalData, cacheKey);
            }

            // 2. Parsowanie Excel i łączenie podobnych produktów
            ExcelParserService.ParsedExcelResult parseResult;
            try {
                parseResult = excelParserService.parseDietExcel(request.getFile());

                // 3. Dodanie danych do response
                additionalData.put("totalMeals", parseResult.totalMeals());
                additionalData.put("meals", parseResult.meals());
                additionalData.put("shoppingList", parseResult.shoppingList());
            } catch (Exception e) {
                log.error("Błąd podczas parsowania pliku Excel", e);
                allValidations.add(new ValidationResult(
                        false,
                        "Błąd podczas parsowania pliku: " + e.getMessage(),
                        ValidationSeverity.ERROR
                ));
                return createResponse(false, allValidations, additionalData, cacheKey);
            }

            // 4. Walidacja liczby posiłków
            ValidationResult mealsPerDayValidation = mealsPerDayValidator.validateMealsCount(
                    parseResult.totalMeals(),
                    request.getMealsPerDay()
            );
            allValidations.add(mealsPerDayValidation);
            if (!mealsPerDayValidation.isValid()) {
                return createResponse(false, allValidations, additionalData, cacheKey);
            }

            // 5. Walidacja dat
            ValidationResult dateValidation = dateValidator.validateDate(
                    request.getStartDate(),
                    request.getMealsPerDay(),
                    parseResult.totalMeals(),
                    request.getDuration()
            );
            allValidations.add(dateValidation);
            if (!dateValidation.isValid()) {
                return createResponse(false, allValidations, additionalData, cacheKey);
            }

            // 6. Walidacja konfiguracji posiłków
            List<ValidationResult> mealsConfigValidation = mealsConfigValidator.validateMealConfig(
                    request.getMealTimes(),
                    request.getMealTypes()
            );
            allValidations.addAll(mealsConfigValidation);
            if (containsErrors(mealsConfigValidation)) {
                return createResponse(false, allValidations, additionalData, cacheKey);
            }

            // Dodaj podsumowanie dla użytkownika, jeśli wszystkie walidacje przeszły pomyślnie
            allValidations.add(new ValidationResult(
                    true,
                    "Wszystkie walidacje zakończone pomyślnie. Dieta gotowa do zapisu.",
                    ValidationSeverity.SUCCESS
            ));

            return createResponse(isValid, allValidations, additionalData, cacheKey);

        } catch (Exception e) {
            log.error("Błąd podczas walidacji szablonu diety", e);
            allValidations.add(new ValidationResult(
                    false,
                    "Wystąpił nieoczekiwany błąd: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
            return createResponse(false, allValidations, additionalData, cacheKey);
        }
    }

    /**
     * Sprawdza, czy lista wyników walidacji zawiera błędy.
     *
     * @param validations lista wyników walidacji do sprawdzenia
     * @return true, jeśli lista zawiera błędy, false w przeciwnym razie
     */
    private boolean containsErrors(List<ValidationResult> validations) {
        return validations.stream()
                .anyMatch(v -> v.severity() == ValidationSeverity.ERROR && !v.isValid());
    }

    /**
     * Tworzy odpowiedź walidacyjną na podstawie podanych parametrów.
     *
     * @param isValid        flaga określająca, czy walidacja się powiodła
     * @param validations    lista wyników walidacji
     * @param additionalData dodatkowe dane do dołączenia do odpowiedzi
     * @return kompletna odpowiedź walidacyjna
     */
    private ValidationResponse createResponse(
            boolean isValid,
            List<ValidationResult> validations,
            Map<String, Object> additionalData,
            String cacheKey) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(isValid);
        response.setValidationResults(validations);
        response.setAdditionalData(additionalData);

        // Zapisujemy do cache tylko poprawne walidacje
        if (isValid) {
            validationCache.put(cacheKey, response);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }

        return response;
    }

    private void cleanExpiredCacheEntries() {
        long now = System.currentTimeMillis();

        // Usuwamy wpisy starsze niż CACHE_TTL_MS
        cacheTimestamps.entrySet().removeIf(entry -> {
            boolean isExpired = (now - entry.getValue()) > CACHE_TTL_MS;
            if (isExpired) {
                validationCache.remove(entry.getKey());
            }
            return isExpired;
        });

        // Jeśli cache jest za duży, usuń najstarsze wpisy
        if (validationCache.size() > 100) {
            List<Map.Entry<String, Long>> entries = new ArrayList<>(cacheTimestamps.entrySet());
            entries.sort(Map.Entry.comparingByValue());

            // Usuń 20% najstarszych wpisów
            int toRemove = Math.max(1, validationCache.size() / 5);
            for (int i = 0; i < toRemove && i < entries.size(); i++) {
                String key = entries.get(i).getKey();
                validationCache.remove(key);
                cacheTimestamps.remove(key);
            }
        }
    }

    private String generateCacheKey(DietTemplateRequest request) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Dodajemy nazwę pliku i jego rozmiar
            String fileName = request.getFile().getOriginalFilename();
            long fileSize = request.getFile().getSize();
            md.update((fileName + fileSize).getBytes());

            // Dodajemy inne parametry
            md.update(String.valueOf(request.getMealsPerDay()).getBytes());
            md.update(String.valueOf(request.getDuration()).getBytes());
            md.update(request.getStartDate().getBytes());

            // Konwertujemy hash do string
            byte[] digest = md.digest();
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // Fallback, jeśli nie można użyć MD5
            return request.getFile().getOriginalFilename() + request.getFile().getSize() +
                    request.getMealsPerDay() + request.getDuration() + request.getStartDate();
        }
    }

}