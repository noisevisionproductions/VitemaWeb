package com.noisevisionsoftware.nutrilog.utils.excelParser.service.validation;

import com.noisevisionsoftware.nutrilog.utils.excelParser.service.ExcelReaderService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation.ValidationSeverity;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExcelStructureValidator {

    private static final Logger log = LoggerFactory.getLogger(ExcelStructureValidator.class);
    private final ExcelReaderService excelReaderService;

    public record ErrorObject(int row, List<String> errors) {
    }

    public List<ValidationResult> validateExcelStructure(MultipartFile file) {
        try {
            // Użyj EasyExcel do walidacji struktury
            List<List<String>> rows = excelReaderService.readExcelFile(file);

            if (rows.isEmpty()) {
                return Collections.singletonList(
                        new ValidationResult(false, "Plik jest pusty", ValidationSeverity.ERROR)
                );
            }

            List<ErrorObject> errors = validateRows(rows);
            if (errors.isEmpty()) {
                return Collections.singletonList(
                        new ValidationResult(
                                true,
                                String.format("Struktura pliku jest poprawna. Znaleziono %d posiłków.", rows.size() - 1),
                                ValidationSeverity.SUCCESS
                        )
                );
            }

            return errors.stream()
                    .map(error -> new ValidationResult(
                            false,
                            String.format("Wiersz %d: %s", error.row(), String.join(", ", error.errors())),
                            ValidationSeverity.ERROR
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Błąd podczas walidacji pliku Excel", e);
            return Collections.singletonList(
                    new ValidationResult(
                            false,
                            "Błąd podczas przetwarzania pliku: " + e.getMessage(),
                            ValidationSeverity.ERROR
                    )
            );
        }
    }

    private List<ErrorObject> validateRows(List<List<String>> rows) {
        List<ErrorObject> errors = new ArrayList<>();

        // Pomiń wiersz nagłówkowy
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            List<String> rowErrors = new ArrayList<>();

            // Walidacja struktury wiersza
            if (row.size() < 4) {
                rowErrors.add("Nieprawidłowa liczba kolumn");
            } else {
                if (row.get(1).trim().isEmpty()) {
                    rowErrors.add("Brak nazwy posiłku");
                }
                // Dodaj więcej walidacji według potrzeb
            }

            if (!rowErrors.isEmpty()) {
                errors.add(new ErrorObject(i + 1, rowErrors));
            }
        }

        return errors;
    }
}