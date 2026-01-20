package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DateValidatorTest {

    private DateValidator dateValidator;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @BeforeEach
    void setUp() {
        dateValidator = new DateValidator();
    }

    @Test
    @DisplayName("Powinien odrzucić datę w przeszłości")
    void validateDate_shouldRejectPastDate() {
        // given
        String pastDate = LocalDate.now().minusDays(1).format(formatter);
        int mealsPerDay = 3;
        int totalMeals = 30;
        int duration = 10;

        // when
        ValidationResult result = dateValidator.validateDate(pastDate, mealsPerDay, totalMeals, duration);

        // then
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("przeszłości"));
    }

    @Test
    @DisplayName("Powinien odrzucić nieprawidłowy format daty")
    void validateDate_shouldRejectInvalidDateFormat() {
        // given
        String invalidDate = "2023/05/15"; // nieprawidłowy format ISO
        int mealsPerDay = 3;
        int totalMeals = 30;
        int duration = 10;

        // when
        ValidationResult result = dateValidator.validateDate(invalidDate, mealsPerDay, totalMeals, duration);

        // then
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("format"));
    }

    @Test
    @DisplayName("Powinien odrzucić dietę dłuższą niż pozwala liczba posiłków")
    void validateDate_shouldRejectDurationExceedingMealCount() {
        // given
        String futureDate = LocalDate.now().plusDays(1).format(formatter);
        int mealsPerDay = 3;
        int totalMeals = 15; // tylko na 5 dni
        int duration = 10;   // chcemy na 10 dni

        // when
        ValidationResult result = dateValidator.validateDate(futureDate, mealsPerDay, totalMeals, duration);

        // then
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("przekracza"));
    }

    @Test
    @DisplayName("Powinien ostrzegać o diecie dłuższej niż 30 dni")
    void validateDate_shouldWarnAboutLongDiet() {
        // given
        String futureDate = LocalDate.now().plusDays(1).format(formatter);
        int mealsPerDay = 3;
        int totalMeals = 120; // wystarczy na 40 dni
        int duration = 35;   // chcemy na 35 dni

        // when
        ValidationResult result = dateValidator.validateDate(futureDate, mealsPerDay, totalMeals, duration);

        // then
        assertTrue(result.isValid()); // walidacja przechodzi, ale z ostrzeżeniem
        assertEquals(ValidationSeverity.WARNING, result.severity());
        assertTrue(result.message().contains("30 dni"));
    }

    @Test
    @DisplayName("Powinien zaakceptować poprawną datę i długość diety")
    void validateDate_shouldAcceptValidDateAndDuration() {
        // given
        String futureDate = LocalDate.now().plusDays(2).format(formatter);
        int mealsPerDay = 3;
        int totalMeals = 60; // wystarczy na 20 dni
        int duration = 15;   // chcemy na 15 dni

        // when
        ValidationResult result = dateValidator.validateDate(futureDate, mealsPerDay, totalMeals, duration);

        // then
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());
        assertTrue(result.message().contains("15 dni"));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 30, 10, true",
            "4, 40, 10, true",
            "5, 50, 10, true",
            "3, 25, 10, false",
            "4, 39, 10, false",
            "5, 40, 10, false"
    })
    @DisplayName("Powinien prawidłowo zwalidować różne konfiguracje posiłków i dni")
    void validateDate_shouldCorrectlyValidateVariousConfigurations(int mealsPerDay, int totalMeals, int duration, boolean expectedValid) {
        // given
        String futureDate = LocalDate.now().plusDays(1).format(formatter);

        // when
        ValidationResult result = dateValidator.validateDate(futureDate, mealsPerDay, totalMeals, duration);

        // then
        assertEquals(expectedValid, result.isValid());
    }
}