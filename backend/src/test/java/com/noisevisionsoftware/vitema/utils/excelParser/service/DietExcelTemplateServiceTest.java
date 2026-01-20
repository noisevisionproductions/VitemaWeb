package com.noisevisionsoftware.vitema.utils.excelParser.service;

import com.noisevisionsoftware.vitema.dto.request.diet.DietTemplateExcelRequest;
import com.noisevisionsoftware.vitema.dto.response.ValidationResponse;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import com.noisevisionsoftware.vitema.utils.excelParser.service.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DietExcelTemplateServiceTest {

    @Mock
    private ExcelStructureValidator excelStructureValidator;

    @Mock
    private MealsPerDayValidator mealsPerDayValidator;

    @Mock
    private DateValidator dateValidator;

    @Mock
    private MealsConfigValidator mealsConfigValidator;

    @Mock
    private ExcelParserService excelParserService;

    @Mock
    private ValidationCacheService cacheService;

    @InjectMocks
    private DietExcelTemplateService dietExcelTemplateService;

    private DietTemplateExcelRequest validRequest;

    @BeforeEach
    void setUp() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "12:00");
        mealTimes.put("meal_2", "15:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER
        );

        validRequest = new DietTemplateExcelRequest();
        validRequest.setFile(mockFile);
        validRequest.setMealsPerDay(3);
        validRequest.setDuration(30);
        validRequest.setStartDate("2025-03-08");
        validRequest.setMealTypes(mealTypes);
        validRequest.setMealTimes(mealTimes);
    }

    @Test
    void shouldReturnCachedResponseWhenAvailable() {
        // given
        String cacheKey = "testKey";
        ValidationResponse cachedResponse = new ValidationResponse();
        cachedResponse.setValid(true);

        // Zmień "" na null, aby dopasować do faktycznego wywołania
        when(cacheService.generateCacheKey(validRequest, null)).thenReturn(cacheKey);
        when(cacheService.getFromCache(cacheKey)).thenReturn(Optional.of(cachedResponse));

        // when
        ValidationResponse result = dietExcelTemplateService.validateDietTemplate(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldFailValidationWhenFileIsEmpty() {
        // given
        validRequest.setFile(new MockMultipartFile("file", new byte[0]));

        // when
        ValidationResponse result = dietExcelTemplateService.validateDietTemplate(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationResults())
                .anyMatch(vr -> vr.message().contains("Plik jest wymagany"));
    }

    @Test
    void shouldFailValidationWhenMealsPerDayIsInvalid() {
        // given
        validRequest.setMealsPerDay(11); // przekroczony limit 10 posiłków

        // when
        ValidationResponse result = dietExcelTemplateService.validateDietTemplate(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationResults())
                .anyMatch(vr -> vr.message().contains("Zbyt duża liczba posiłków"));
    }

    @Test
    void shouldSucceedWithValidRequest() throws IOException {
        // given
        when(excelStructureValidator.validateExcelStructure(any()))
                .thenReturn(Collections.singletonList(new ValidationResult(true, "OK", ValidationSeverity.SUCCESS)));

        // Tworzenie przykładowej listy posiłków
        List<ParsedMeal> meals = new ArrayList<>();

        // Tworzenie przykładowej listy zakupów
        List<Map.Entry<String, ParsedProduct>> shoppingList = new ArrayList<>();

        when(excelParserService.parseDietExcel(any()))
                .thenReturn(new ExcelParserService.ParsedExcelResult(
                        meals,
                        3,
                        shoppingList
                ));

        when(mealsPerDayValidator.validateMealsCount(anyInt(), anyInt()))
                .thenReturn(new ValidationResult(true, "OK", ValidationSeverity.SUCCESS));

        when(dateValidator.validateDate(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn(new ValidationResult(true, "OK", ValidationSeverity.SUCCESS));

        when(mealsConfigValidator.validateMealConfig(anyMap(), anyList()))
                .thenReturn(Collections.singletonList(new ValidationResult(true, "OK", ValidationSeverity.SUCCESS)));

        // when
        ValidationResponse result = dietExcelTemplateService.validateDietTemplate(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValidationResults())
                .anyMatch(vr -> vr.message().contains("Wszystkie walidacje zakończone pomyślnie"));
    }

    @Test
    void shouldFailWhenExcelStructureValidationFails() {
        // given
        when(excelStructureValidator.validateExcelStructure(any()))
                .thenReturn(Collections.singletonList(
                        new ValidationResult(false, "Niepoprawna struktura Excel", ValidationSeverity.ERROR)
                ));

        // when
        ValidationResponse result = dietExcelTemplateService.validateDietTemplate(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationResults())
                .anyMatch(vr -> vr.message().contains("Niepoprawna struktura Excel"));
    }

    @Test
    void shouldHandleExceptionDuringValidation() {
        // given
        when(excelStructureValidator.validateExcelStructure(any()))
                .thenThrow(new RuntimeException("Nieoczekiwany błąd"));

        // when
        ValidationResponse result = dietExcelTemplateService.validateDietTemplate(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationResults())
                .anyMatch(vr -> vr.message().contains("Wystąpił nieoczekiwany błąd"));
    }
}