package com.noisevisionsoftware.vitema.controller.diet.manual;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.PreviewMealSaveRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.SaveMealTemplateRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.*;
import com.noisevisionsoftware.vitema.service.diet.manual.ManualDietService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManualDietControllerTest {

    @Mock
    private ManualDietService manualDietService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ManualDietController manualDietController;

    private ManualDietRequest mockDietRequest;
    private ParsedProduct mockParsedProduct;
    private MealTemplateResponse mockMealTemplateResponse;
    private SaveMealTemplateRequest mockSaveMealTemplateRequest;
    private PreviewMealSaveRequest mockPreviewMealSaveRequest;
    private MealSavePreviewResponse mockMealSavePreviewResponse;
    private MealSuggestionResponse mockMealSuggestionResponse;

    private static final String DIET_ID = "diet-123";
    private static final String MEAL_ID = "meal-123";
    private static final String TEMPLATE_ID = "template-123";
    private static final String IMAGE_URL = "https://storage.example.com/meal.jpg";
    private static final String QUERY = "chicken breast";

    @BeforeEach
    void setUp() {
        mockDietRequest = new ManualDietRequest();
        mockParsedProduct = new ParsedProduct();
        mockMealTemplateResponse = new MealTemplateResponse();
        mockSaveMealTemplateRequest = new SaveMealTemplateRequest();
        mockPreviewMealSaveRequest = new PreviewMealSaveRequest();
        mockMealSavePreviewResponse = new MealSavePreviewResponse();
        mockMealSuggestionResponse = new MealSuggestionResponse();
    }

    // POST /api/diets/manual/save - saveManualDiet tests

    @Test
    void saveManualDiet_WithValidRequest_ShouldReturnOkWithDietId() {
        // Arrange
        when(manualDietService.saveManualDiet(mockDietRequest)).thenReturn(DIET_ID);

        // Act
        ResponseEntity<ManualDietResponse> response = manualDietController.saveManualDiet(mockDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(DIET_ID, response.getBody().getDietId());
        assertEquals("Dieta została pomyślnie zapisana", response.getBody().getMessage());
        verify(manualDietService).saveManualDiet(mockDietRequest);
    }

    @Test
    void saveManualDiet_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Database error";
        when(manualDietService.saveManualDiet(mockDietRequest))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        ResponseEntity<ManualDietResponse> response = manualDietController.saveManualDiet(mockDietRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDietId());
        assertTrue(response.getBody().getMessage().contains(errorMessage));
        verify(manualDietService).saveManualDiet(mockDietRequest);
    }

    // GET /api/diets/manual/ingredients/search - searchIngredients tests

    @Test
    void searchIngredients_WithValidQuery_ShouldReturnOkWithIngredients() {
        // Arrange
        List<ParsedProduct> ingredients = Arrays.asList(mockParsedProduct, new ParsedProduct());
        when(manualDietService.searchIngredients(QUERY, 10)).thenReturn(ingredients);

        // Act
        ResponseEntity<List<ParsedProduct>> response =
                manualDietController.searchIngredients(QUERY, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(manualDietService).searchIngredients(QUERY, 10);
    }

    @Test
    void searchIngredients_WithCustomLimit_ShouldReturnOkWithLimitedResults() {
        // Arrange
        int customLimit = 5;
        List<ParsedProduct> ingredients = Collections.singletonList(mockParsedProduct);
        when(manualDietService.searchIngredients(QUERY, customLimit)).thenReturn(ingredients);

        // Act
        ResponseEntity<List<ParsedProduct>> response =
                manualDietController.searchIngredients(QUERY, customLimit);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(manualDietService).searchIngredients(QUERY, customLimit);
    }

    @Test
    void searchIngredients_WithNoResults_ShouldReturnEmptyList() {
        // Arrange
        when(manualDietService.searchIngredients(QUERY, 10)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ParsedProduct>> response =
                manualDietController.searchIngredients(QUERY, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(manualDietService).searchIngredients(QUERY, 10);
    }

    @Test
    void searchIngredients_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.searchIngredients(QUERY, 10))
                .thenThrow(new RuntimeException("Search error"));

        // Act
        ResponseEntity<List<ParsedProduct>> response =
                manualDietController.searchIngredients(QUERY, 10);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).searchIngredients(QUERY, 10);
    }

    // POST /api/diets/manual/ingredients - createIngredient tests

    @Test
    void createIngredient_WithValidIngredient_ShouldReturnOkWithCreatedIngredient() {
        // Arrange
        when(manualDietService.createIngredient(mockParsedProduct)).thenReturn(mockParsedProduct);

        // Act
        ResponseEntity<ParsedProduct> response = manualDietController.createIngredient(mockParsedProduct);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockParsedProduct, response.getBody());
        verify(manualDietService).createIngredient(mockParsedProduct);
    }

    @Test
    void createIngredient_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.createIngredient(mockParsedProduct))
                .thenThrow(new RuntimeException("Creation failed"));

        // Act
        ResponseEntity<ParsedProduct> response = manualDietController.createIngredient(mockParsedProduct);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).createIngredient(mockParsedProduct);
    }

    // POST /api/diets/manual/validate - validateDiet tests

    @Test
    void validateDiet_WithValidRequest_ShouldReturnOkWithValidationResult() {
        // Arrange
        Map<String, Object> validationResult = new HashMap<>();
        validationResult.put("valid", true);
        validationResult.put("warnings", Collections.emptyList());
        when(manualDietService.validateManualDiet(mockDietRequest)).thenReturn(validationResult);

        // Act
        ResponseEntity<Map<String, Object>> response = manualDietController.validateDiet(mockDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("valid"));
        verify(manualDietService).validateManualDiet(mockDietRequest);
    }

    @Test
    void validateDiet_WithInvalidDiet_ShouldReturnOkWithValidationErrors() {
        // Arrange
        Map<String, Object> validationResult = new HashMap<>();
        validationResult.put("valid", false);
        validationResult.put("errors", List.of("Missing required field"));
        when(manualDietService.validateManualDiet(mockDietRequest)).thenReturn(validationResult);

        // Act
        ResponseEntity<Map<String, Object>> response = manualDietController.validateDiet(mockDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("valid"));
        verify(manualDietService).validateManualDiet(mockDietRequest);
    }

    @Test
    void validateDiet_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.validateManualDiet(mockDietRequest))
                .thenThrow(new RuntimeException("Validation error"));

        // Act
        ResponseEntity<Map<String, Object>> response = manualDietController.validateDiet(mockDietRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).validateManualDiet(mockDietRequest);
    }

    // GET /api/diets/manual/meals/{id} - getMealTemplate tests

    @Test
    void getMealTemplate_WithValidId_ShouldReturnOkWithTemplate() {
        // Arrange
        when(manualDietService.getMealTemplate(MEAL_ID)).thenReturn(mockMealTemplateResponse);

        // Act
        ResponseEntity<MealTemplateResponse> response = manualDietController.getMealTemplate(MEAL_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockMealTemplateResponse, response.getBody());
        verify(manualDietService).getMealTemplate(MEAL_ID);
    }

    @Test
    void getMealTemplate_WithNonExistentId_ShouldReturnNotFound() {
        // Arrange
        when(manualDietService.getMealTemplate(MEAL_ID))
                .thenThrow(new RuntimeException("Meal not found"));

        // Act
        ResponseEntity<MealTemplateResponse> response = manualDietController.getMealTemplate(MEAL_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(manualDietService).getMealTemplate(MEAL_ID);
    }

    // POST /api/diets/manual/meals/save-template - saveMealTemplate tests

    @Test
    void saveMealTemplate_WithValidRequest_ShouldReturnOkWithSavedTemplate() {
        // Arrange
        when(manualDietService.saveMealTemplate(mockSaveMealTemplateRequest))
                .thenReturn(mockMealTemplateResponse);

        // Act
        ResponseEntity<MealTemplateResponse> response =
                manualDietController.saveMealTemplate(mockSaveMealTemplateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockMealTemplateResponse, response.getBody());
        verify(manualDietService).saveMealTemplate(mockSaveMealTemplateRequest);
    }

    @Test
    void saveMealTemplate_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.saveMealTemplate(mockSaveMealTemplateRequest))
                .thenThrow(new RuntimeException("Save failed"));

        // Act
        ResponseEntity<MealTemplateResponse> response =
                manualDietController.saveMealTemplate(mockSaveMealTemplateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).saveMealTemplate(mockSaveMealTemplateRequest);
    }

    // POST /api/diets/manual/meals/preview-save - previewMealSave tests

    @Test
    void previewMealSave_WithValidRequest_ShouldReturnOkWithPreview() {
        // Arrange
        when(manualDietService.previewMealSave(mockPreviewMealSaveRequest))
                .thenReturn(mockMealSavePreviewResponse);

        // Act
        ResponseEntity<MealSavePreviewResponse> response =
                manualDietController.previewMealSave(mockPreviewMealSaveRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockMealSavePreviewResponse, response.getBody());
        verify(manualDietService).previewMealSave(mockPreviewMealSaveRequest);
    }

    @Test
    void previewMealSave_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.previewMealSave(mockPreviewMealSaveRequest))
                .thenThrow(new RuntimeException("Preview failed"));

        // Act
        ResponseEntity<MealSavePreviewResponse> response =
                manualDietController.previewMealSave(mockPreviewMealSaveRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).previewMealSave(mockPreviewMealSaveRequest);
    }

    // POST /api/diets/manual/meals/upload-image - uploadMealImage tests

    @Test
    void uploadMealImage_WithValidImage_ShouldReturnOkWithImageUrl() {
        // Arrange
        when(manualDietService.uploadMealImage(multipartFile, MEAL_ID)).thenReturn(IMAGE_URL);

        // Act
        ResponseEntity<MealImageResponse> response =
                manualDietController.uploadMealImage(multipartFile, MEAL_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(IMAGE_URL, response.getBody().getImageUrl());
        verify(manualDietService).uploadMealImage(multipartFile, MEAL_ID);
    }

    @Test
    void uploadMealImage_WithoutMealId_ShouldReturnOkWithImageUrl() {
        // Arrange
        when(manualDietService.uploadMealImage(multipartFile, null)).thenReturn(IMAGE_URL);

        // Act
        ResponseEntity<MealImageResponse> response =
                manualDietController.uploadMealImage(multipartFile, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(IMAGE_URL, response.getBody().getImageUrl());
        verify(manualDietService).uploadMealImage(multipartFile, null);
    }

    @Test
    void uploadMealImage_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.uploadMealImage(multipartFile, MEAL_ID))
                .thenThrow(new RuntimeException("Upload failed"));

        // Act
        ResponseEntity<MealImageResponse> response =
                manualDietController.uploadMealImage(multipartFile, MEAL_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).uploadMealImage(multipartFile, MEAL_ID);
    }

    // POST /api/diets/manual/meals/upload-base64-image - uploadBase64MealImage tests

    @Test
    void uploadBase64MealImage_WithValidImageData_ShouldReturnOkWithImageUrl() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        String base64Image = "data:image/png;base64,iVBORw0KGgo...";
        request.put("imageData", base64Image);
        request.put("mealId", MEAL_ID);
        when(manualDietService.uploadBase64MealImage(base64Image, MEAL_ID)).thenReturn(IMAGE_URL);

        // Act
        ResponseEntity<MealImageResponse> response = manualDietController.uploadBase64MealImage(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(IMAGE_URL, response.getBody().getImageUrl());
        verify(manualDietService).uploadBase64MealImage(base64Image, MEAL_ID);
    }

    @Test
    void uploadBase64MealImage_WithoutMealId_ShouldReturnOkWithImageUrl() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        String base64Image = "data:image/png;base64,iVBORw0KGgo...";
        request.put("imageData", base64Image);
        when(manualDietService.uploadBase64MealImage(base64Image, null)).thenReturn(IMAGE_URL);

        // Act
        ResponseEntity<MealImageResponse> response = manualDietController.uploadBase64MealImage(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(IMAGE_URL, response.getBody().getImageUrl());
        verify(manualDietService).uploadBase64MealImage(base64Image, null);
    }

    @Test
    void uploadBase64MealImage_WithNullImageData_ShouldReturnBadRequest() {
        // Arrange
        Map<String, String> request = new HashMap<>();

        // Act
        ResponseEntity<MealImageResponse> response = manualDietController.uploadBase64MealImage(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(manualDietService);
    }

    @Test
    void uploadBase64MealImage_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        String base64Image = "data:image/png;base64,iVBORw0KGgo...";
        request.put("imageData", base64Image);
        request.put("mealId", MEAL_ID);
        when(manualDietService.uploadBase64MealImage(base64Image, MEAL_ID))
                .thenThrow(new RuntimeException("Upload failed"));

        // Act
        ResponseEntity<MealImageResponse> response = manualDietController.uploadBase64MealImage(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).uploadBase64MealImage(base64Image, MEAL_ID);
    }

    // GET /api/diets/manual/meals/search - searchMealSuggestions tests

    @Test
    void searchMealSuggestions_WithValidQuery_ShouldReturnOkWithSuggestions() {
        // Arrange
        List<MealSuggestionResponse> suggestions = Collections.singletonList(mockMealSuggestionResponse);
        when(manualDietService.searchMealSuggestions(QUERY, 10)).thenReturn(suggestions);

        // Act
        ResponseEntity<List<MealSuggestionResponse>> response =
                manualDietController.searchMealSuggestions(QUERY, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(manualDietService).searchMealSuggestions(QUERY, 10);
    }

    @Test
    void searchMealSuggestions_WithCustomLimit_ShouldReturnOkWithLimitedResults() {
        // Arrange
        int customLimit = 5;
        List<MealSuggestionResponse> suggestions = Collections.singletonList(mockMealSuggestionResponse);
        when(manualDietService.searchMealSuggestions(QUERY, customLimit)).thenReturn(suggestions);

        // Act
        ResponseEntity<List<MealSuggestionResponse>> response =
                manualDietController.searchMealSuggestions(QUERY, customLimit);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(manualDietService).searchMealSuggestions(QUERY, customLimit);
    }

    @Test
    void searchMealSuggestions_WithNoResults_ShouldReturnEmptyList() {
        // Arrange
        when(manualDietService.searchMealSuggestions(QUERY, 10)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<MealSuggestionResponse>> response =
                manualDietController.searchMealSuggestions(QUERY, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(manualDietService).searchMealSuggestions(QUERY, 10);
    }

    @Test
    void searchMealSuggestions_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.searchMealSuggestions(QUERY, 10))
                .thenThrow(new RuntimeException("Search failed"));

        // Act
        ResponseEntity<List<MealSuggestionResponse>> response =
                manualDietController.searchMealSuggestions(QUERY, 10);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).searchMealSuggestions(QUERY, 10);
    }

    // PUT /api/diets/manual/meals/templates/{templateId} - updateMealTemplate tests

    @Test
    void updateMealTemplate_WithValidRequest_ShouldReturnOkWithUpdatedTemplate() {
        // Arrange
        when(manualDietService.updateMealTemplate(TEMPLATE_ID, mockSaveMealTemplateRequest))
                .thenReturn(mockMealTemplateResponse);

        // Act
        ResponseEntity<MealTemplateResponse> response =
                manualDietController.updateMealTemplate(TEMPLATE_ID, mockSaveMealTemplateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockMealTemplateResponse, response.getBody());
        verify(manualDietService).updateMealTemplate(TEMPLATE_ID, mockSaveMealTemplateRequest);
    }

    @Test
    void updateMealTemplate_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(manualDietService.updateMealTemplate(TEMPLATE_ID, mockSaveMealTemplateRequest))
                .thenThrow(new RuntimeException("Update failed"));

        // Act
        ResponseEntity<MealTemplateResponse> response =
                manualDietController.updateMealTemplate(TEMPLATE_ID, mockSaveMealTemplateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(manualDietService).updateMealTemplate(TEMPLATE_ID, mockSaveMealTemplateRequest);
    }
}
