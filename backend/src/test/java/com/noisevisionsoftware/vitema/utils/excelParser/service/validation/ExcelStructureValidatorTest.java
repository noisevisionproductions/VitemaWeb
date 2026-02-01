package com.noisevisionsoftware.vitema.utils.excelParser.service.validation;

import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import com.noisevisionsoftware.vitema.utils.excelParser.service.ExcelReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelStructureValidatorTest {

    @Mock
    private ExcelReaderService excelReaderService;

    @InjectMocks
    private ExcelStructureValidator validator;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test data".getBytes()
        );
    }

    @Test
    @DisplayName("Powinien zwrócić błąd gdy plik Excel jest pusty")
    void validateExcelStructure_shouldReturnErrorForEmptyFile() throws IOException {
        // given
        when(excelReaderService.readExcelFile(any(MultipartFile.class)))
                .thenReturn(Collections.emptyList());

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        ValidationResult result = results.getFirst();
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("pusty"));
    }

    @Test
    @DisplayName("Powinien zwrócić sukces dla poprawnej struktury pliku")
    void validateExcelStructure_shouldReturnSuccessForValidStructure() throws IOException {
        // given
        List<List<String>> validRows = new ArrayList<>();
        validRows.add(Arrays.asList("ID", "Nazwa", "Opis", "Składniki", "Wartości"));
        // Dane
        validRows.add(Arrays.asList("1", "Owsianka", "Przepis", "Płatki, mleko", "300kcal"));
        validRows.add(Arrays.asList("2", "Kanapka", "Przepis", "Chleb, masło", "250kcal"));

        when(excelReaderService.readExcelFile(any(MultipartFile.class))).thenReturn(validRows);

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        ValidationResult result = results.getFirst();
        assertTrue(result.isValid());
        assertEquals(ValidationSeverity.SUCCESS, result.severity());
        assertTrue(result.message().contains("poprawna"));
        assertTrue(result.message().contains("2 posiłków"));
    }

    @Test
    @DisplayName("Powinien zgłosić błąd dla niepoprawnej liczby kolumn")
    void validateExcelStructure_shouldReturnErrorForInvalidColumnCount() throws IOException {
        // given
        List<List<String>> invalidRows = new ArrayList<>();
        invalidRows.add(Arrays.asList("ID", "Nazwa", "Opis", "Składniki", "Wartości"));
        invalidRows.add(Arrays.asList("1", "Owsianka", "Przepis"));

        when(excelReaderService.readExcelFile(any(MultipartFile.class))).thenReturn(invalidRows);

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        ValidationResult result = results.getFirst();
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("Wiersz 2"));
        assertTrue(result.message().contains("liczba kolumn"));
    }

    @Test
    @DisplayName("Powinien zgłosić błąd dla braku nazwy posiłku")
    void validateExcelStructure_shouldReturnErrorForMissingMealName() throws IOException {
        // given
        List<List<String>> invalidRows = new ArrayList<>();
        invalidRows.add(Arrays.asList("ID", "Nazwa", "Opis", "Składniki", "Wartości"));
        invalidRows.add(Arrays.asList("1", "", "Przepis", "Składniki", "300kcal"));

        when(excelReaderService.readExcelFile(any(MultipartFile.class))).thenReturn(invalidRows);

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        ValidationResult result = results.getFirst();
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("Wiersz 2"));
        assertTrue(result.message().contains("Brak nazwy posiłku"));
    }

    @Test
    @DisplayName("Powinien zgłosić wiele błędów dla różnych wierszy")
    void validateExcelStructure_shouldReturnMultipleErrorsForDifferentRows() throws IOException {
        // given
        List<List<String>> invalidRows = new ArrayList<>();
        // Wiersz nagłówkowy
        invalidRows.add(Arrays.asList("ID", "Nazwa", "Opis", "Składniki", "Wartości"));
        invalidRows.add(Arrays.asList("1", "", "Przepis", "Składniki", "300kcal")); // Brak nazwy
        invalidRows.add(Arrays.asList("2", "Kanapka", "Przepis")); // Za mało kolumn

        when(excelReaderService.readExcelFile(any(MultipartFile.class))).thenReturn(invalidRows);

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());

        ValidationResult result1 = results.getFirst();
        assertFalse(result1.isValid());
        assertEquals(ValidationSeverity.ERROR, result1.severity());
        assertTrue(result1.message().contains("Wiersz 2"));
        assertTrue(result1.message().contains("Brak nazwy posiłku"));

        ValidationResult result2 = results.get(1);
        assertFalse(result2.isValid());
        assertEquals(ValidationSeverity.ERROR, result2.severity());
        assertTrue(result2.message().contains("Wiersz 3"));
        assertTrue(result2.message().contains("liczba kolumn"));
    }

    @Test
    @DisplayName("Powinien obsłużyć wyjątek podczas czytania pliku")
    void validateExcelStructure_shouldHandleExceptionDuringFileReading() throws IOException {
        // given
        when(excelReaderService.readExcelFile(any(MultipartFile.class)))
                .thenThrow(new IOException("Błąd odczytu pliku"));

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        ValidationResult result = results.getFirst();
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("Błąd podczas przetwarzania pliku"));
        assertTrue(result.message().contains("Błąd odczytu pliku"));
    }

    @Test
    @DisplayName("Powinien obsłużyć wyjątek podczas walidacji wierszy")
    void validateExcelStructure_shouldHandleExceptionDuringRowValidation() throws IOException {
        // given
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("ID", "Nazwa", "Opis", "Składniki", "Wartości"));
        rows.add(null);

        when(excelReaderService.readExcelFile(any(MultipartFile.class))).thenReturn(rows);

        // when
        List<ValidationResult> results = validator.validateExcelStructure(mockFile);

        // then
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());

        ValidationResult result = results.getFirst();
        assertFalse(result.isValid());
        assertEquals(ValidationSeverity.ERROR, result.severity());
        assertTrue(result.message().contains("Błąd podczas przetwarzania pliku"));
    }

    @Test
    @DisplayName("ErrorObject powinien poprawnie przechowywać informacje o błędach")
    void errorObject_shouldCorrectlyStoreErrorInformation() {
        // given
        int rowNumber = 3;
        List<String> errorMessages = Arrays.asList("Błąd 1", "Błąd 2");

        // when
        ExcelStructureValidator.ErrorObject errorObject = new ExcelStructureValidator.ErrorObject(rowNumber, errorMessages);

        // then
        assertEquals(rowNumber, errorObject.row());
        assertEquals(errorMessages, errorObject.errors());
        assertEquals(2, errorObject.errors().size());
        assertEquals("Błąd 1", errorObject.errors().get(0));
        assertEquals("Błąd 2", errorObject.errors().get(1));
    }
}