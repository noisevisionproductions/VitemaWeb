package com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationSeverity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Service
public class DateValidator {

    public ValidationResult validateDate(String startDateStr, int mealsPerDay, int totalMeals, int duration) {
        LocalDate startLocalDate;
        try {
            startLocalDate = LocalDate.parse(startDateStr);
        } catch (DateTimeParseException e) {
            return new ValidationResult(
                    false,
                    "Nieprawidłowy format daty rozpoczęcia",
                    ValidationSeverity.ERROR
            );
        }

        LocalDate today = LocalDate.now();

        if (startLocalDate.isBefore(today)) {
            return new ValidationResult(
                    false,
                    "Data rozpoczęcia diety nie może być w przeszłości",
                    ValidationSeverity.ERROR
            );
        }

        // Rest of your validation logic...
        int maxPossibleDays = totalMeals / mealsPerDay;
        if (duration > maxPossibleDays) {
            return new ValidationResult(
                    false,
                    String.format("Wybrana długość diety (%d dni) przekracza możliwą długość na podstawie liczby posiłków (%d dni)",
                            duration, maxPossibleDays),
                    ValidationSeverity.ERROR
            );
        }

        if (duration > 30) {
            return new ValidationResult(
                    true,
                    "Uwaga: Wybrano dietę dłuższą niż 30 dni",
                    ValidationSeverity.WARNING
            );
        }

        return new ValidationResult(
                true,
                String.format("Plan diety na %d dni jest poprawny", duration),
                ValidationSeverity.SUCCESS
        );
    }
}