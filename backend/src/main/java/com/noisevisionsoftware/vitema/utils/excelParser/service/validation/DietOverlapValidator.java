package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.service.diet.DietService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietOverlapValidator {

    private final DietService dietService;

    public ValidationResult validateDietOverlap(String userId, LocalDate startDate, int duration) {
        try {
            if (userId == null || userId.isEmpty()) {
                return new ValidationResult(true,
                        "Brak wybranego użytkownika, pomijam sprawdzanie nakładania się diet",
                        ValidationSeverity.WARNING);
            }

            if (startDate == null) {
                return new ValidationResult(
                        false,
                        "Błąd konwersji daty rozpoczęcia",
                        ValidationSeverity.ERROR
                );
            }

            // Obliczamy datę zakończenia
            LocalDate endDate = startDate.plusDays(duration - 1);

            Timestamp startTimestamp = convertToTimestamp(startDate);
            if (startTimestamp == null) {
                return new ValidationResult(
                        false,
                        "Błąd konwersji daty rozpoczęcia",
                        ValidationSeverity.ERROR
                );
            }

            Timestamp endTimestamp = convertToTimestamp(endDate);
            if (endTimestamp == null) {
                return new ValidationResult(
                        false,
                        "Błąd konwersji daty zakończenia",
                        ValidationSeverity.ERROR
                );
            }

            boolean hasOverlap = dietService.hasDietOverlapForUser(userId, startTimestamp, endTimestamp, null);

            if (hasOverlap) {
                return new ValidationResult(
                        false,
                        "Użytkownik posiada już dietę w wybranym okresie. Usuń istniejącą dietę lub zmień datę rozpoczęcia.",
                        ValidationSeverity.ERROR
                );
            }

            return new ValidationResult(
                    true,
                    "Brak konfliktów z istniejącymi dietami",
                    ValidationSeverity.SUCCESS
            );
        } catch (Exception e) {
            log.error("Error validating diet overlap", e);
            return new ValidationResult(
                    false,
                    "Wystąpił błąd podczas sprawdzania nakładania się diet: " +
                            (e.getMessage() != null ? e.getMessage() : "nieznany błąd"),
                    ValidationSeverity.ERROR
            );
        }
    }

    private Timestamp convertToTimestamp(LocalDate localDate) {
        try {
            if (localDate == null) {
                return null;
            }
            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            return Timestamp.of(date);
        } catch (Exception e) {
            log.error("Error converting LocalDate to Timestamp", e);
            return null;
        }
    }
}