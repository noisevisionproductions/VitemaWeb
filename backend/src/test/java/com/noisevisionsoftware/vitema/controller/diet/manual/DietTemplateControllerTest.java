package com.noisevisionsoftware.vitema.controller.diet.manual;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.DietTemplateRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.DietTemplateResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.DietTemplateStatsResponse;
import com.noisevisionsoftware.vitema.service.diet.manual.dietTemplate.DietTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietTemplateControllerTest {

    @Mock
    private DietTemplateService dietTemplateService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DietTemplateController dietTemplateController;

    private DietTemplateResponse mockTemplateResponse;
    private DietTemplateRequest mockTemplateRequest;
    private DietTemplateStatsResponse mockStatsResponse;

    private static final String USER_ID = "test-user-id";
    private static final String TEMPLATE_ID = "template-123";
    private static final String CATEGORY = "high-protein";

    @BeforeEach
    void setUp() {
        mockTemplateResponse = new DietTemplateResponse();
        mockTemplateRequest = new DietTemplateRequest();
        mockStatsResponse = new DietTemplateStatsResponse();
    }

    // GET /api/diet-templates - getAllTemplates tests

    @Test
    void getAllTemplates_WithValidUser_ShouldReturnOkWithTemplates() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        List<DietTemplateResponse> templates = Arrays.asList(mockTemplateResponse, new DietTemplateResponse());
        when(dietTemplateService.getAllTemplatesForUser(USER_ID)).thenReturn(templates);

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = dietTemplateController.getAllTemplates(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(dietTemplateService).getAllTemplatesForUser(USER_ID);
    }

    @Test
    void getAllTemplates_WithEmptyList_ShouldReturnOkWithEmptyList() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getAllTemplatesForUser(USER_ID)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = dietTemplateController.getAllTemplates(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(dietTemplateService).getAllTemplatesForUser(USER_ID);
    }

    @Test
    void getAllTemplates_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getAllTemplatesForUser(USER_ID))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = dietTemplateController.getAllTemplates(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).getAllTemplatesForUser(USER_ID);
    }

    // GET /api/diet-templates/{id} - getTemplate tests

    @Test
    void getTemplate_WithValidId_ShouldReturnOkWithTemplate() {
        // Arrange
        when(dietTemplateService.getTemplateById(TEMPLATE_ID)).thenReturn(mockTemplateResponse);

        // Act
        ResponseEntity<DietTemplateResponse> response = dietTemplateController.getTemplate(TEMPLATE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTemplateResponse, response.getBody());
        verify(dietTemplateService).getTemplateById(TEMPLATE_ID);
    }

    @Test
    void getTemplate_WithNonExistentId_ShouldReturnNotFound() {
        // Arrange
        when(dietTemplateService.getTemplateById(TEMPLATE_ID))
                .thenThrow(new RuntimeException("Template not found"));

        // Act
        ResponseEntity<DietTemplateResponse> response = dietTemplateController.getTemplate(TEMPLATE_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dietTemplateService).getTemplateById(TEMPLATE_ID);
    }

    // GET /api/diet-templates/category/{category} - getTemplatesByCategory tests

    @Test
    void getTemplatesByCategory_WithValidCategory_ShouldReturnOkWithTemplates() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        List<DietTemplateResponse> templates = Arrays.asList(mockTemplateResponse);
        when(dietTemplateService.getTemplatesByCategory(CATEGORY, USER_ID)).thenReturn(templates);

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.getTemplatesByCategory(CATEGORY, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dietTemplateService).getTemplatesByCategory(CATEGORY, USER_ID);
    }

    @Test
    void getTemplatesByCategory_WithNonExistentCategory_ShouldReturnEmptyList() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getTemplatesByCategory(CATEGORY, USER_ID))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.getTemplatesByCategory(CATEGORY, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(dietTemplateService).getTemplatesByCategory(CATEGORY, USER_ID);
    }

    @Test
    void getTemplatesByCategory_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getTemplatesByCategory(CATEGORY, USER_ID))
                .thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.getTemplatesByCategory(CATEGORY, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).getTemplatesByCategory(CATEGORY, USER_ID);
    }

    // GET /api/diet-templates/popular - getPopularTemplates tests

    @Test
    void getPopularTemplates_WithDefaultLimit_ShouldReturnOkWithTemplates() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        List<DietTemplateResponse> templates = Arrays.asList(mockTemplateResponse);
        when(dietTemplateService.getMostUsedTemplates(USER_ID, 10)).thenReturn(templates);

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.getPopularTemplates(10, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dietTemplateService).getMostUsedTemplates(USER_ID, 10);
    }

    @Test
    void getPopularTemplates_WithCustomLimit_ShouldReturnOkWithLimitedTemplates() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        int customLimit = 5;
        List<DietTemplateResponse> templates = Arrays.asList(mockTemplateResponse);
        when(dietTemplateService.getMostUsedTemplates(USER_ID, customLimit)).thenReturn(templates);

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.getPopularTemplates(customLimit, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(dietTemplateService).getMostUsedTemplates(USER_ID, customLimit);
    }

    @Test
    void getPopularTemplates_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getMostUsedTemplates(USER_ID, 10))
                .thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.getPopularTemplates(10, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).getMostUsedTemplates(USER_ID, 10);
    }

    // GET /api/diet-templates/search - searchTemplates tests

    @Test
    void searchTemplates_WithValidQuery_ShouldReturnOkWithResults() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        String query = "high protein";
        List<DietTemplateResponse> templates = Arrays.asList(mockTemplateResponse);
        when(dietTemplateService.searchTemplates(query, USER_ID, 20)).thenReturn(templates);

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.searchTemplates(query, 20, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dietTemplateService).searchTemplates(query, USER_ID, 20);
    }

    @Test
    void searchTemplates_WithCustomLimit_ShouldReturnOkWithLimitedResults() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        String query = "vegan";
        int customLimit = 15;
        List<DietTemplateResponse> templates = Arrays.asList(mockTemplateResponse);
        when(dietTemplateService.searchTemplates(query, USER_ID, customLimit)).thenReturn(templates);

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.searchTemplates(query, customLimit, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(dietTemplateService).searchTemplates(query, USER_ID, customLimit);
    }

    @Test
    void searchTemplates_WithNoResults_ShouldReturnEmptyList() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        String query = "nonexistent";
        when(dietTemplateService.searchTemplates(query, USER_ID, 20))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.searchTemplates(query, 20, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(dietTemplateService).searchTemplates(query, USER_ID, 20);
    }

    @Test
    void searchTemplates_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        String query = "test";
        when(dietTemplateService.searchTemplates(query, USER_ID, 20))
                .thenThrow(new RuntimeException("Search error"));

        // Act
        ResponseEntity<List<DietTemplateResponse>> response = 
                dietTemplateController.searchTemplates(query, 20, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).searchTemplates(query, USER_ID, 20);
    }

    // POST /api/diet-templates - createTemplate tests

    @Test
    void createTemplate_WithValidRequest_ShouldReturnOkWithCreatedTemplate() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.createTemplate(mockTemplateRequest, USER_ID))
                .thenReturn(mockTemplateResponse);

        // Act
        ResponseEntity<DietTemplateResponse> response = 
                dietTemplateController.createTemplate(mockTemplateRequest, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTemplateResponse, response.getBody());
        verify(dietTemplateService).createTemplate(mockTemplateRequest, USER_ID);
    }

    @Test
    void createTemplate_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.createTemplate(mockTemplateRequest, USER_ID))
                .thenThrow(new RuntimeException("Creation failed"));

        // Act
        ResponseEntity<DietTemplateResponse> response = 
                dietTemplateController.createTemplate(mockTemplateRequest, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).createTemplate(mockTemplateRequest, USER_ID);
    }

    // POST /api/diet-templates/from-diet - createTemplateFromDiet tests

    @Test
    void createTemplateFromDiet_WithValidRequest_ShouldReturnOkWithCreatedTemplate() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.createTemplateFromManualDiet(mockTemplateRequest, USER_ID))
                .thenReturn(mockTemplateResponse);

        // Act
        ResponseEntity<DietTemplateResponse> response = 
                dietTemplateController.createTemplateFromDiet(mockTemplateRequest, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTemplateResponse, response.getBody());
        verify(dietTemplateService).createTemplateFromManualDiet(mockTemplateRequest, USER_ID);
    }

    @Test
    void createTemplateFromDiet_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.createTemplateFromManualDiet(mockTemplateRequest, USER_ID))
                .thenThrow(new RuntimeException("Creation failed"));

        // Act
        ResponseEntity<DietTemplateResponse> response = 
                dietTemplateController.createTemplateFromDiet(mockTemplateRequest, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).createTemplateFromManualDiet(mockTemplateRequest, USER_ID);
    }

    // PUT /api/diet-templates/{id} - updateTemplate tests

    @Test
    void updateTemplate_WithValidRequest_ShouldReturnOkWithUpdatedTemplate() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.updateTemplate(TEMPLATE_ID, mockTemplateRequest, USER_ID))
                .thenReturn(mockTemplateResponse);

        // Act
        ResponseEntity<DietTemplateResponse> response = 
                dietTemplateController.updateTemplate(TEMPLATE_ID, mockTemplateRequest, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockTemplateResponse, response.getBody());
        verify(dietTemplateService).updateTemplate(TEMPLATE_ID, mockTemplateRequest, USER_ID);
    }

    @Test
    void updateTemplate_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.updateTemplate(TEMPLATE_ID, mockTemplateRequest, USER_ID))
                .thenThrow(new RuntimeException("Update failed"));

        // Act
        ResponseEntity<DietTemplateResponse> response = 
                dietTemplateController.updateTemplate(TEMPLATE_ID, mockTemplateRequest, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).updateTemplate(TEMPLATE_ID, mockTemplateRequest, USER_ID);
    }

    // DELETE /api/diet-templates/{id} - deleteTemplate tests

    @Test
    void deleteTemplate_WithValidId_ShouldReturnOk() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        doNothing().when(dietTemplateService).deleteTemplate(TEMPLATE_ID, USER_ID);

        // Act
        ResponseEntity<Void> response = dietTemplateController.deleteTemplate(TEMPLATE_ID, authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dietTemplateService).deleteTemplate(TEMPLATE_ID, USER_ID);
    }

    @Test
    void deleteTemplate_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        doThrow(new RuntimeException("Deletion failed"))
                .when(dietTemplateService).deleteTemplate(TEMPLATE_ID, USER_ID);

        // Act
        ResponseEntity<Void> response = dietTemplateController.deleteTemplate(TEMPLATE_ID, authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).deleteTemplate(TEMPLATE_ID, USER_ID);
    }

    // POST /api/diet-templates/{id}/use - incrementUsage tests

    @Test
    void incrementUsage_WithValidId_ShouldReturnOk() {
        // Arrange
        doNothing().when(dietTemplateService).incrementUsageCount(TEMPLATE_ID);

        // Act
        ResponseEntity<Void> response = dietTemplateController.incrementUsage(TEMPLATE_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dietTemplateService).incrementUsageCount(TEMPLATE_ID);
    }

    @Test
    void incrementUsage_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        doThrow(new RuntimeException("Update failed"))
                .when(dietTemplateService).incrementUsageCount(TEMPLATE_ID);

        // Act
        ResponseEntity<Void> response = dietTemplateController.incrementUsage(TEMPLATE_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).incrementUsageCount(TEMPLATE_ID);
    }

    // GET /api/diet-templates/stats - getTemplateStats tests

    @Test
    void getTemplateStats_WithValidUser_ShouldReturnOkWithStats() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getTemplateStats(USER_ID)).thenReturn(mockStatsResponse);

        // Act
        ResponseEntity<DietTemplateStatsResponse> response = 
                dietTemplateController.getTemplateStats(authentication);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockStatsResponse, response.getBody());
        verify(dietTemplateService).getTemplateStats(USER_ID);
    }

    @Test
    void getTemplateStats_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(authentication.getName()).thenReturn(USER_ID);
        when(dietTemplateService.getTemplateStats(USER_ID))
                .thenThrow(new RuntimeException("Stats retrieval failed"));

        // Act
        ResponseEntity<DietTemplateStatsResponse> response = 
                dietTemplateController.getTemplateStats(authentication);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dietTemplateService).getTemplateStats(USER_ID);
    }
}
