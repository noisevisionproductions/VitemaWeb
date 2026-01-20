package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MealsConfigValidatorTest {

    private MealsConfigValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MealsConfigValidator();
    }

    @Test
    @DisplayName("Powinien zgłosić błąd gdy przekazane są puste parametry")
    void validateMealConfig_shouldRejectNullParameters() {
        // when
        List<ValidationResult> results = validator.validateMealConfig(null, null);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertFalse(results.getFirst().isValid());
        assertEquals(ValidationSeverity.ERROR, results.getFirst().severity());
        assertTrue(results.getFirst().message().contains("Brak konfiguracji"));
    }

    @Test
    @DisplayName("Powinien zgłosić błąd gdy czasy posiłków są w złej kolejności")
    void validateMealConfig_shouldRejectIncorrectMealOrder() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "07:00"); // Wcześniejszy czas niż pierwszy posiłek
        mealTimes.put("meal_2", "13:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.SECOND_BREAKFAST,
                MealType.LUNCH
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> !r.isValid() && r.message().contains("nie może być wcześniej")));
    }

    @Test
    @DisplayName("Powinien zgłosić ostrzeżenie gdy odstęp między posiłkami jest za krótki")
    void validateMealConfig_shouldWarnAboutShortTimeBetweenMeals() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "09:30"); // Tylko 1.5h różnicy
        mealTimes.put("meal_2", "13:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.SECOND_BREAKFAST,
                MealType.LUNCH
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.isValid() && r.severity() == ValidationSeverity.WARNING && r.message().contains("mniejszy niż 2 godziny")));
    }

    @Test
    @DisplayName("Powinien ostrzegać o nietypowej porze dla śniadania")
    void validateMealConfig_shouldWarnAboutUnusualBreakfastTime() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "03:00"); // Bardzo wczesne śniadanie
        mealTimes.put("meal_1", "10:00");
        mealTimes.put("meal_2", "15:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.SECOND_BREAKFAST,
                MealType.LUNCH
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.severity() == ValidationSeverity.WARNING && r.message().contains("Nietypowa pora na")));
    }

    @Test
    @DisplayName("Powinien zgłosić błąd dla posiłków wykraczających poza porządek dnia")
    void validateMealConfig_shouldRejectMealsOutOfDailyOrder() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "13:00");
        mealTimes.put("meal_2", "00:30");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream()
                .anyMatch(r -> !r.isValid() && r.message().contains("nie może być wcześniej")));
    }

    @Test
    @DisplayName("Powinien zgłosić błąd gdy typy posiłków się powtarzają")
    void validateMealConfig_shouldRejectDuplicateMealTypes() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "12:00");
        mealTimes.put("meal_2", "16:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.BREAKFAST, // Duplikat
                MealType.LUNCH
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> !r.isValid() && r.message().contains("występuje 2 razy")));
    }

    @Test
    @DisplayName("Powinien zaakceptować poprawną konfigurację posiłków")
    void validateMealConfig_shouldAcceptValidMealConfig() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "11:00");
        mealTimes.put("meal_2", "14:00");
        mealTimes.put("meal_3", "17:00");
        mealTimes.put("meal_4", "20:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.SECOND_BREAKFAST,
                MealType.LUNCH,
                MealType.SNACK,
                MealType.DINNER
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertTrue(results.isEmpty()); // Brak błędów lub ostrzeżeń
    }

    @Test
    @DisplayName("Powinien obsłużyć nieprawidłowy format czasu")
    void validateMealConfig_shouldHandleInvalidTimeFormat() {
        // given
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "nieprawidłowy format"); // Błędny format
        mealTimes.put("meal_2", "14:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.SECOND_BREAKFAST,
                MealType.LUNCH
        );

        // when, then
        assertThrows(IllegalArgumentException.class, () -> validator.validateMealConfig(mealTimes, mealTypes));
    }

    @Test
    @DisplayName("Powinien poprawnie walidować format czasu")
    void isValidTimeFormat_shouldValidateTimeFormat() {
        MealsConfigValidator validator = new MealsConfigValidator();

        // Używamy refleksji, aby uzyskać dostęp do prywatnej metody
        java.lang.reflect.Method isValidTimeFormatMethod;
        try {
            isValidTimeFormatMethod = MealsConfigValidator.class.getDeclaredMethod("isValidTimeFormat", String.class);
            isValidTimeFormatMethod.setAccessible(true);

            // Poprawne formaty
            assertTrue((Boolean) isValidTimeFormatMethod.invoke(validator, "00:00"));
            assertTrue((Boolean) isValidTimeFormatMethod.invoke(validator, "12:30"));
            assertTrue((Boolean) isValidTimeFormatMethod.invoke(validator, "23:59"));
            assertTrue((Boolean) isValidTimeFormatMethod.invoke(validator, "8:45"));

            // Niepoprawne formaty
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, (Object) null));
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, ""));
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, "24:00"));  // godzina poza zakresem
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, "12:60"));  // minuta poza zakresem
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, "12:3"));   // niepełny format minut
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, "12-30"));  // zły separator
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, "12.30"));  // zły separator
            assertFalse((Boolean) isValidTimeFormatMethod.invoke(validator, "abcd"));   // nie jest czasem
        } catch (Exception e) {
            fail("Test nie powiódł się: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Powinien poprawnie konwertować czas na minuty")
    void getTimeInMinutes_shouldConvertTimeToMinutes() {
        MealsConfigValidator validator = new MealsConfigValidator();

        // Używamy refleksji, aby uzyskać dostęp do prywatnej metody
        java.lang.reflect.Method getTimeInMinutesMethod;
        try {
            getTimeInMinutesMethod = MealsConfigValidator.class.getDeclaredMethod("getTimeInMinutes", String.class);
            getTimeInMinutesMethod.setAccessible(true);

            // Testy konwersji
            assertEquals(0, (Integer) getTimeInMinutesMethod.invoke(validator, "00:00"));
            assertEquals(60, (Integer) getTimeInMinutesMethod.invoke(validator, "01:00"));
            assertEquals(90, (Integer) getTimeInMinutesMethod.invoke(validator, "01:30"));
            assertEquals(750, (Integer) getTimeInMinutesMethod.invoke(validator, "12:30"));
            assertEquals(1439, (Integer) getTimeInMinutesMethod.invoke(validator, "23:59"));

            // Test wyjątku dla niepoprawnego formatu
            assertThrows(InvocationTargetException.class, () -> getTimeInMinutesMethod.invoke(validator, "24:00"));
        } catch (Exception e) {
            fail("Test nie powiódł się: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Powinien obsłużyć konfigurację z ostrzeżeniami o porach posiłków")
    void validateMealConfig_shouldHandleMultipleWarnings() {
        // given
        List<ValidationResult> results = getResults();

        // then
        assertFalse(results.isEmpty());

        long warningCount = results.stream()
                .filter(r -> r.severity() == ValidationSeverity.WARNING)
                .count();
        assertEquals(2, warningCount);

        // Sprawdzenie konkretnych ostrzeżeń
        assertTrue(results.stream().anyMatch(r -> r.message().contains("Nietypowa pora na")));
        assertTrue(results.stream().anyMatch(r -> r.message().contains("mniejszy niż 2 godziny")));
    }

    private static List<ValidationResult> getResults() {
        MealsConfigValidator validator = new MealsConfigValidator();

        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "03:00"); // Nietypowa pora śniadania (< 4:00)
        mealTimes.put("meal_1", "04:30"); // Zbyt krótki odstęp (< 2h)
        mealTimes.put("meal_2", "13:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.SECOND_BREAKFAST,
                MealType.LUNCH
        );

        // when
        return validator.validateMealConfig(mealTimes, mealTypes);
    }

    @Test
    @DisplayName("Powinien obsłużyć krawędziowy przypadek - północ")
    void validateMealConfig_shouldHandleMidnight() {
        // given
        MealsConfigValidator validator = new MealsConfigValidator();

        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "22:00");
        mealTimes.put("meal_1", "00:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.DINNER,
                MealType.SNACK
        );

        // when
        List<ValidationResult> results = validator.validateMealConfig(mealTimes, mealTypes);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> !r.isValid() && r.message().contains("nie może być wcześniej")));
    }
}