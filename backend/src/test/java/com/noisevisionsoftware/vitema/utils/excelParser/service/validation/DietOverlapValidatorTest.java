package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.service.diet.DietService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DietOverlapValidatorTest {

    @Mock
    private DietService dietService;

    @InjectMocks
    private DietOverlapValidator dietOverlapValidator;

    private final String userId = "user123";
    private final LocalDate startDate = LocalDate.of(2025, 4, 3);
    private final int duration = 7;

    @Test
    public void testValidateEmptyUserId() {
        // Arrange
        String emptyUserId = "";

        // Act
        ValidationResult result = dietOverlapValidator.validateDietOverlap(emptyUserId, startDate, duration);

        // Assert
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.WARNING, result.severity());
        assertEquals("Brak wybranego użytkownika, pomijam sprawdzanie nakładania się diet", result.message());

        // Verify that dietService was not called
        verifyNoInteractions(dietService);
    }

    @Test
    public void testValidateNullUserId() {
        // Act
        ValidationResult result = dietOverlapValidator.validateDietOverlap(null, startDate, duration);

        // Assert
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.WARNING, result.severity());
        assertEquals("Brak wybranego użytkownika, pomijam sprawdzanie nakładania się diet", result.message());

        // Verify that dietService was not called
        verifyNoInteractions(dietService);
    }

    @Test
    public void testNoOverlap() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 4, 3);
        int dietDuration = 7;

        // Mock the service response for no overlap
        when(dietService.hasDietOverlapForUser(eq(userId), any(Timestamp.class), any(Timestamp.class), isNull()))
                .thenReturn(false);

        // Act
        ValidationResult result = dietOverlapValidator.validateDietOverlap(userId, start, dietDuration);

        // Assert
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());
        assertEquals("Brak konfliktów z istniejącymi dietami", result.message());

        // Verify service was called with correct parameters
        verify(dietService).hasDietOverlapForUser(
                eq(userId),
                argThat(ts -> dateEquals(ts, start)),
                argThat(ts -> dateEquals(ts, start.plusDays(dietDuration - 1))),
                isNull()
        );
    }

    @Test
    public void testWithOverlap() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 4, 10);
        int dietDuration = 14;

        // Mock the service response for an overlap
        when(dietService.hasDietOverlapForUser(eq(userId), any(Timestamp.class), any(Timestamp.class), isNull()))
                .thenReturn(true);

        // Act
        ValidationResult result = dietOverlapValidator.validateDietOverlap(userId, start, dietDuration);

        // Assert
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertEquals("Użytkownik posiada już dietę w wybranym okresie. Usuń istniejącą dietę lub zmień datę rozpoczęcia.",
                result.message());

        // Verify service was called with correct parameters
        verify(dietService).hasDietOverlapForUser(
                eq(userId),
                argThat(ts -> dateEquals(ts, start)),
                argThat(ts -> dateEquals(ts, start.plusDays(dietDuration - 1))),
                isNull()
        );
    }

    @Test
    public void testExceptionHandling() {
        // Arrange
        when(dietService.hasDietOverlapForUser(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act
        ValidationResult result = dietOverlapValidator.validateDietOverlap(userId, startDate, duration);

        // Assert
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("Wystąpił błąd podczas sprawdzania nakładania się diet"));
        assertTrue(result.message().contains("Test exception"));
    }

    @Test
    public void testNullStartDate() {
        // Act
        ValidationResult result = dietOverlapValidator.validateDietOverlap(userId, null, duration);

        // Assert
        assertFalse(result.isValid());
        assertEquals("Błąd konwersji daty rozpoczęcia", result.message());
        assertEquals(ValidationSeverity.ERROR, result.severity());
    }

    @Test
    public void testNegativeDuration() {
        // Arrange
        int negativeDuration = -1;

        // Act & Assert
        // Here we're testing that the endDate calculation works correctly with negative duration
        // The endDate will be before startDate, but the validation logic should still work
        ValidationResult result = dietOverlapValidator.validateDietOverlap(userId, startDate, negativeDuration);

        // Since we've mocked dietService to return false, we expect a successful validation
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());
    }

    // Helper method to compare dates from Timestamps
    private boolean dateEquals(Timestamp timestamp, LocalDate localDate) {
        if (timestamp == null || localDate == null) {
            return false;
        }

        LocalDate timestampDate = timestamp.toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return timestampDate.equals(localDate);
    }
}