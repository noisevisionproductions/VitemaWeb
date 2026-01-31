package com.noisevisionsoftware.vitema.controller.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.diet.CalorieValidationRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.DietTemplateExcelRequest;
import com.noisevisionsoftware.vitema.dto.response.DietPreviewResponse;
import com.noisevisionsoftware.vitema.dto.response.ErrorResponse;
import com.noisevisionsoftware.vitema.dto.response.ValidationResponse;
import com.noisevisionsoftware.vitema.exception.DietValidationException;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationSeverity;
import com.noisevisionsoftware.vitema.utils.excelParser.service.DietExcelTemplateService;
import com.noisevisionsoftware.vitema.utils.excelParser.service.ExcelParserService;
import com.noisevisionsoftware.vitema.utils.excelParser.service.validation.CalorieValidator;
import com.noisevisionsoftware.vitema.utils.excelParser.service.validation.ExcelStructureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietUploadControllerTest {

    @Mock
    private ExcelStructureValidator excelStructureValidator;

    @Mock
    private ExcelParserService excelParserService;

    @Mock
    private DietExcelTemplateService dietExcelTemplateService;

    @Mock
    private CalorieValidator calorieValidator;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private DietUploadController dietUploadController;

    private static final String TEST_USER_ID = "user123";
    private static final String TEST_START_DATE = "2024-01-01";
    private static final int TEST_MEALS_PER_DAY = 3;
    private static final int TEST_DURATION = 7;

    private List<ValidationResult> successValidationResults;
    private List<ValidationResult> errorValidationResults;
    private ValidationResponse successValidationResponse;
    private ExcelParserService.ParsedExcelResult parsedExcelResult;

    @BeforeEach
    void setUp() {
        successValidationResults = Arrays.asList(
                new ValidationResult(true, "Znaleziono 21 posiłków", ValidationSeverity.SUCCESS),
                new ValidationResult(true, "Struktura pliku poprawna", ValidationSeverity.SUCCESS)
        );

        errorValidationResults = Arrays.asList(
                new ValidationResult(false, "Nieprawidłowa struktura pliku", ValidationSeverity.ERROR),
                new ValidationResult(false, "Brak wymaganych kolumn", ValidationSeverity.ERROR)
        );

        successValidationResponse = new ValidationResponse(
                true,
                successValidationResults,
                Map.of("totalMeals", 21)
        );

        parsedExcelResult = new ExcelParserService.ParsedExcelResult(
                createMockMeals(),
                21,
                Collections.emptyList()
        );
    }

    // validateFile tests

    @Test
    void validateFile_WithValidFile_ShouldReturnSuccessResponse() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelStructureValidator.validateExcelStructure(mockFile))
                .thenReturn(successValidationResults);

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateFile(mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertEquals(2, response.getBody().getValidationResults().size());
        verify(excelStructureValidator).validateExcelStructure(mockFile);
    }

    @Test
    void validateFile_WithEmptyFile_ShouldReturnBadRequest() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateFile(mockFile);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertEquals(1, response.getBody().getValidationResults().size());
        assertEquals("Przesłany plik jest pusty", response.getBody().getValidationResults().get(0).message());
        verify(excelStructureValidator, never()).validateExcelStructure(any());
    }

    @Test
    void validateFile_WithValidationErrors_ShouldReturnValidationResponse() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelStructureValidator.validateExcelStructure(mockFile))
                .thenReturn(errorValidationResults);

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateFile(mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertEquals(2, response.getBody().getValidationResults().size());
        verify(excelStructureValidator).validateExcelStructure(mockFile);
    }

    @Test
    void validateFile_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelStructureValidator.validateExcelStructure(mockFile))
                .thenThrow(new RuntimeException("Validation error"));

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateFile(mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertTrue(response.getBody().getValidationResults().get(0).message().contains("Validation error"));
        verify(excelStructureValidator).validateExcelStructure(mockFile);
    }

    @Test
    void validateFile_WithMealCountInSuccessMessage_ShouldExtractTotalMeals() {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelStructureValidator.validateExcelStructure(mockFile))
                .thenReturn(successValidationResults);

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateFile(mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getAdditionalData().containsKey("totalMeals"));
        assertEquals(21, response.getBody().getAdditionalData().get("totalMeals"));
    }

    // validateDietTemplate tests

    @Test
    void validateDietTemplate_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        DietTemplateExcelRequest request = createMockDietTemplateRequest();
        when(dietExcelTemplateService.validateDietTemplate(request))
                .thenReturn(successValidationResponse);

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateDietTemplate(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        verify(dietExcelTemplateService).validateDietTemplate(request);
    }

    @Test
    void validateDietTemplate_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        DietTemplateExcelRequest request = createMockDietTemplateRequest();
        when(dietExcelTemplateService.validateDietTemplate(request))
                .thenThrow(new RuntimeException("Template validation error"));

        // Act
        ResponseEntity<ValidationResponse> response = dietUploadController.validateDietTemplate(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertTrue(response.getBody().getValidationResults().get(0).message().contains("Template validation error"));
        verify(dietExcelTemplateService).validateDietTemplate(request);
    }

    // validateDietTemplateWithUser tests

    @Test
    void validateDietTemplateWithUser_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        DietTemplateExcelRequest request = createMockDietTemplateRequest();
        when(dietExcelTemplateService.validateDietTemplate(request, TEST_USER_ID))
                .thenReturn(successValidationResponse);

        // Act
        ResponseEntity<ValidationResponse> response = 
                dietUploadController.validateDietTemplateWithUser(request, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        verify(dietExcelTemplateService).validateDietTemplate(request, TEST_USER_ID);
    }

    @Test
    void validateDietTemplateWithUser_WithNullUserId_ShouldCallServiceWithNull() {
        // Arrange
        DietTemplateExcelRequest request = createMockDietTemplateRequest();
        when(dietExcelTemplateService.validateDietTemplate(request, null))
                .thenReturn(successValidationResponse);

        // Act
        ResponseEntity<ValidationResponse> response = 
                dietUploadController.validateDietTemplateWithUser(request, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dietExcelTemplateService).validateDietTemplate(request, null);
    }

    @Test
    void validateDietTemplateWithUser_WhenExceptionThrown_ShouldReturnInternalServerError() {
        // Arrange
        DietTemplateExcelRequest request = createMockDietTemplateRequest();
        when(dietExcelTemplateService.validateDietTemplate(request, TEST_USER_ID))
                .thenThrow(new RuntimeException("Template validation error"));

        // Act
        ResponseEntity<ValidationResponse> response = 
                dietUploadController.validateDietTemplateWithUser(request, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        verify(dietExcelTemplateService).validateDietTemplate(request, TEST_USER_ID);
    }

    // previewDiet tests

    @Test
    void previewDiet_WithValidInput_ShouldReturnPreviewResponse() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelParserService.parseDietExcel(mockFile))
                .thenReturn(parsedExcelResult);

        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof DietPreviewResponse);
        verify(excelParserService).parseDietExcel(mockFile);
    }

    @Test
    void previewDiet_WithEmptyFile_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);
        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Plik jest wymagany"));
        verify(excelParserService, never()).parseDietExcel(any());
    }

    @Test
    void previewDiet_WithInvalidMealsPerDay_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                0,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Liczba posiłków"));
    }

    @Test
    void previewDiet_WithInvalidDuration_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                100,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Długość diety"));
    }

    @Test
    void previewDiet_WithInvalidStartDate_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                "invalid-date",
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Nieprawidłowy format daty"));
    }

    @Test
    void previewDiet_WithMismatchedMealTypesCount_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH"); // Only 2 meal types

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY, // 3 meals per day
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Liczba typów posiłków"));
    }

    @Test
    void previewDiet_WithInvalidMealTime_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = new HashMap<>();
        allParams.put("mealTimes[meal_0]", "25:00"); // Invalid time
        allParams.put("mealTimes[meal_1]", "12:00");
        allParams.put("mealTimes[meal_2]", "18:00");
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Nieprawidłowy format godziny"));
    }

    @Test
    void previewDiet_WithSkipColumnsCount_ShouldUseSkipColumnsParameter() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelParserService.parseDietExcel(mockFile, 2))
                .thenReturn(parsedExcelResult);

        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                2,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(excelParserService).parseDietExcel(mockFile, 2);
        verify(excelParserService, never()).parseDietExcel(mockFile);
    }

    @Test
    void previewDiet_WithCalorieValidationEnabled_ShouldValidateCalories() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelParserService.parseDietExcel(mockFile))
                .thenReturn(parsedExcelResult);

        ValidationResult calorieValidationResult = new ValidationResult(
                true,
                "Calories within target range",
                ValidationSeverity.SUCCESS
        );
        when(calorieValidator.validateCalories(any(), any(), anyInt()))
                .thenReturn(calorieValidationResult);

        CalorieValidator.CalorieAnalysisResult analysisResult = 
                new CalorieValidator.CalorieAnalysisResult(2000, Arrays.asList(1900, 2000, 2100), true);
        when(calorieValidator.analyzeCalories(any(), anyInt()))
                .thenReturn(analysisResult);

        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                true,
                2000,
                5
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(calorieValidator).validateCalories(any(), any(), eq(TEST_MEALS_PER_DAY));
        verify(calorieValidator).analyzeCalories(any(), eq(TEST_MEALS_PER_DAY));
    }

    @Test
    void previewDiet_WithCalorieValidationEnabledButNoTargetCalories_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                true,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Walidacja kalorii wymaga poprawnej wartości"));
    }

    @Test
    void previewDiet_WhenExceptionThrown_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(excelParserService.parseDietExcel(mockFile))
                .thenThrow(new RuntimeException("Parsing error"));

        Map<String, String> allParams = createMealTimeParams();
        List<String> mealTypes = Arrays.asList("BREAKFAST", "LUNCH", "DINNER");

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                mealTypes,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Parsing error"));
    }

    @Test
    void previewDiet_WithNullMealTypes_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                null,
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertTrue(errorResponse.getMessage().contains("Typy posiłków są wymagane"));
    }

    @Test
    void previewDiet_WithEmptyMealTypes_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        Map<String, String> allParams = createMealTimeParams();

        // Act
        ResponseEntity<?> response = dietUploadController.previewDiet(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                allParams,
                Collections.emptyList(),
                null,
                false,
                null,
                null
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    // Exception handler tests

    @Test
    void handleDietValidationException_ShouldReturnBadRequest() {
        // Arrange
        List<ValidationResult> validationResults = Arrays.asList(
                new ValidationResult(false, "Invalid diet structure", ValidationSeverity.ERROR)
        );
        DietValidationException exception = new DietValidationException("Diet validation failed", validationResults);

        // Act
        ResponseEntity<ValidationResponse> response = 
                dietUploadController.handleDietValidationException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertEquals(validationResults, response.getBody().getValidationResults());
        assertTrue(response.getBody().getAdditionalData().isEmpty());
    }

    @Test
    void handleDietValidationException_WithMultipleErrors_ShouldReturnAllErrors() {
        // Arrange
        List<ValidationResult> validationResults = Arrays.asList(
                new ValidationResult(false, "Error 1", ValidationSeverity.ERROR),
                new ValidationResult(false, "Error 2", ValidationSeverity.ERROR),
                new ValidationResult(false, "Error 3", ValidationSeverity.WARNING)
        );
        DietValidationException exception = new DietValidationException("Multiple validation errors", validationResults);

        // Act
        ResponseEntity<ValidationResponse> response = 
                dietUploadController.handleDietValidationException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getValidationResults().size());
    }

    // Helper methods

    private DietTemplateExcelRequest createMockDietTemplateRequest() {
        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("meal_0", "08:00");
        mealTimes.put("meal_1", "12:00");
        mealTimes.put("meal_2", "18:00");

        List<MealType> mealTypes = Arrays.asList(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER
        );

        return new DietTemplateExcelRequest(
                mockFile,
                TEST_MEALS_PER_DAY,
                TEST_START_DATE,
                TEST_DURATION,
                mealTimes,
                mealTypes,
                null,
                false,
                null,
                null
        );
    }

    private Map<String, String> createMealTimeParams() {
        Map<String, String> params = new HashMap<>();
        params.put("mealTimes[meal_0]", "08:00");
        params.put("mealTimes[meal_1]", "12:00");
        params.put("mealTimes[meal_2]", "18:00");
        return params;
    }

    private List<ParsedMeal> createMockMeals() {
        List<ParsedMeal> meals = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            ParsedMeal meal = ParsedMeal.builder()
                    .name("Meal " + i)
                    .ingredients(Collections.emptyList())
                    .build();
            meals.add(meal);
        }
        return meals;
    }
}
