package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MealsPerDayValidatorTest {

    private MealsPerDayValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MealsPerDayValidator();
    }

    @Test
    @DisplayName("Powinien zgłosić błąd gdy nie ma żadnych posiłków")
    void validateMealsCount_shouldRejectZeroMeals() {
        // given
        int totalMeals = 0;
        int mealsPerDay = 3;

        // when
        ValidationResult result = validator.validateMealsCount(totalMeals, mealsPerDay);

        // then
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("Nie wczytano"));
    }

    @ParameterizedTest
    @DisplayName("Powinien zaakceptować liczbę posiłków podzielną przez mealsPerDay")
    @CsvSource({
            "15, 3",
            "20, 4",
            "25, 5",
            "30, 3",
            "40, 4",
            "50, 5"
    })
    void validateMealsCount_shouldAcceptDivisibleMealCount(int totalMeals, int mealsPerDay) {
        // when
        ValidationResult result = validator.validateMealsCount(totalMeals, mealsPerDay);

        // then
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());

        int expectedDays = totalMeals / mealsPerDay;
        assertTrue(result.message().contains(String.format("%d pełnych dni", expectedDays)));
        assertTrue(result.message().contains(String.format("%d posiłków", mealsPerDay)));
    }

    @ParameterizedTest
    @DisplayName("Powinien odrzucić liczbę posiłków niepodzielną przez mealsPerDay")
    @CsvSource({
            "16, 3",
            "22, 4",
            "27, 5",
            "31, 3",
            "43, 4",
            "52, 5"
    })
    void validateMealsCount_shouldRejectNonDivisibleMealCount(int totalMeals, int mealsPerDay) {
        // when
        ValidationResult result = validator.validateMealsCount(totalMeals, mealsPerDay);

        // then
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());

        int remainder = totalMeals % mealsPerDay;
        int missingMeals = mealsPerDay - remainder;
        assertTrue(result.message().contains(String.format("Brakuje %d posiłków", missingMeals)));
    }

    @Test
    @DisplayName("Powinien poprawnie obliczać liczbę dni")
    void validateMealsCount_shouldCorrectlyCalculateDays() {
        // given
        int totalMeals = 21;
        int mealsPerDay = 3;
        int expectedDays = 7;  // 21 / 3 = 7

        // when
        ValidationResult result = validator.validateMealsCount(totalMeals, mealsPerDay);

        // then
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());
        assertTrue(result.message().contains(String.format("%d pełnych dni", expectedDays)));
    }

    @Test
    @DisplayName("Powinien poprawnie obliczać brakujące posiłki")
    void validateMealsCount_shouldCorrectlyCalculateMissingMeals() {
        // given
        int totalMeals = 22;
        int mealsPerDay = 4;
        int missingMeals = 2;

        // when
        ValidationResult result = validator.validateMealsCount(totalMeals, mealsPerDay);

        // then
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains(String.format("Brakuje %d posiłków", missingMeals)));
    }

    @Test
    @DisplayName("Powinien obsłużyć przypadek graniczny - jeden posiłek")
    void validateMealsCount_shouldHandleOneMeal() {
        // given
        int totalMeals = 1;
        int mealsPerDay = 1;

        // when
        ValidationResult result = validator.validateMealsCount(totalMeals, mealsPerDay);

        // then
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());
        assertTrue(result.message().contains("1 pełnych dni"));
    }
}