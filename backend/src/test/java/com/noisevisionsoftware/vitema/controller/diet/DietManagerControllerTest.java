package com.noisevisionsoftware.vitema.controller.diet;

import com.noisevisionsoftware.vitema.dto.request.diet.SaveDietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.SaveDietResponse;
import com.noisevisionsoftware.vitema.model.diet.DietFileInfo;
import com.noisevisionsoftware.vitema.service.diet.DietManagerService;
import com.noisevisionsoftware.vitema.service.firebase.FileStorageService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietManagerControllerTest {

    @Mock
    private DietManagerService dietManagerService;

    @Mock
    private FileStorageService storageService;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private DietManagerController dietManagerController;

    private static final String TEST_USER_ID = "user123";
    private static final String TEST_FILE_URL = "https://storage.example.com/file123.xlsx";
    private static final String TEST_FILE_NAME = "diet.xlsx";
    private static final String TEST_DIET_ID = "diet123";

    private SaveDietRequest testSaveDietRequest;
    private ParsedDietData testParsedData;
    private DietFileInfo testFileInfo;

    @BeforeEach
    void setUp() {
        testParsedData = new ParsedDietData();
        testFileInfo = new DietFileInfo();
        testSaveDietRequest = new SaveDietRequest(testParsedData, TEST_USER_ID, testFileInfo);
    }

    // uploadFile tests

    @Test
    void uploadFile_WithValidFile_ShouldReturnSuccessResponse() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        when(storageService.uploadFile(mockFile, TEST_USER_ID)).thenReturn(TEST_FILE_URL);

        // Act
        ResponseEntity<Map<String, String>> response = dietManagerController.uploadFile(mockFile, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_FILE_URL, response.getBody().get("fileUrl"));
        assertEquals(TEST_FILE_NAME, response.getBody().get("fileName"));
        assertEquals("Plik został pomyślnie przesłany", response.getBody().get("message"));
        verify(storageService).uploadFile(mockFile, TEST_USER_ID);
    }

    @Test
    void uploadFile_WithEmptyFile_ShouldReturnBadRequest() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = dietManagerController.uploadFile(mockFile, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nie można przesłać pustego pliku", response.getBody().get("message"));
        verify(storageService, never()).uploadFile(any(), any());
    }

    @Test
    void uploadFile_WhenStorageServiceThrowsException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(storageService.uploadFile(mockFile, TEST_USER_ID))
                .thenThrow(new RuntimeException("Storage error"));

        // Act
        ResponseEntity<Map<String, String>> response = dietManagerController.uploadFile(mockFile, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("Wystąpił błąd podczas uploadowania pliku"));
        assertTrue(response.getBody().get("message").contains("Storage error"));
        verify(storageService).uploadFile(mockFile, TEST_USER_ID);
    }

    @Test
    void uploadFile_WithNullOriginalFilename_ShouldHandleNullPointerException() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(storageService.uploadFile(mockFile, TEST_USER_ID)).thenReturn(TEST_FILE_URL);

        // Act
        ResponseEntity<Map<String, String>> response = dietManagerController.uploadFile(mockFile, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("Wystąpił błąd podczas uploadowania pliku"));
        verify(storageService).uploadFile(mockFile, TEST_USER_ID);
    }

    @Test
    void uploadFile_WithIOException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(false);
        when(storageService.uploadFile(mockFile, TEST_USER_ID))
                .thenThrow(new RuntimeException("IO error during upload"));

        // Act
        ResponseEntity<Map<String, String>> response = dietManagerController.uploadFile(mockFile, TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("IO error during upload"));
        verify(storageService).uploadFile(mockFile, TEST_USER_ID);
    }

    // saveDiet tests

    @Test
    void saveDiet_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        when(dietManagerService.saveDietWithShoppingList(
                testParsedData,
                TEST_USER_ID,
                testFileInfo
        )).thenReturn(TEST_DIET_ID);

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(testSaveDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_DIET_ID, response.getBody().getDietId());
        assertEquals("Dieta została pomyślnie zapisana", response.getBody().getMessage());
        verify(dietManagerService).saveDietWithShoppingList(testParsedData, TEST_USER_ID, testFileInfo);
    }

    @Test
    void saveDiet_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(dietManagerService.saveDietWithShoppingList(
                testParsedData,
                TEST_USER_ID,
                testFileInfo
        )).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(testSaveDietRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDietId());
        assertTrue(response.getBody().getMessage().contains("Wystąpił błąd podczas zapisywania diety"));
        assertTrue(response.getBody().getMessage().contains("Database error"));
        verify(dietManagerService).saveDietWithShoppingList(testParsedData, TEST_USER_ID, testFileInfo);
    }

    @Test
    void saveDiet_WithNullParsedData_ShouldCallServiceWithNull() {
        // Arrange
        testSaveDietRequest.setParsedData(null);
        when(dietManagerService.saveDietWithShoppingList(
                null,
                TEST_USER_ID,
                testFileInfo
        )).thenReturn(TEST_DIET_ID);

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(testSaveDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dietManagerService).saveDietWithShoppingList(null, TEST_USER_ID, testFileInfo);
    }

    @Test
    void saveDiet_WithNullFileInfo_ShouldCallServiceWithNull() {
        // Arrange
        testSaveDietRequest.setFileInfo(null);
        when(dietManagerService.saveDietWithShoppingList(
                testParsedData,
                TEST_USER_ID,
                null
        )).thenReturn(TEST_DIET_ID);

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(testSaveDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dietManagerService).saveDietWithShoppingList(testParsedData, TEST_USER_ID, null);
    }

    @Test
    void saveDiet_WhenServiceReturnsNull_ShouldReturnResponseWithNullId() {
        // Arrange
        when(dietManagerService.saveDietWithShoppingList(
                testParsedData,
                TEST_USER_ID,
                testFileInfo
        )).thenReturn(null);

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(testSaveDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDietId());
        assertEquals("Dieta została pomyślnie zapisana", response.getBody().getMessage());
        verify(dietManagerService).saveDietWithShoppingList(testParsedData, TEST_USER_ID, testFileInfo);
    }

    @Test
    void saveDiet_WithValidationException_ShouldReturnInternalServerError() {
        // Arrange
        when(dietManagerService.saveDietWithShoppingList(
                testParsedData,
                TEST_USER_ID,
                testFileInfo
        )).thenThrow(new IllegalArgumentException("Invalid diet data"));

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(testSaveDietRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getDietId());
        assertTrue(response.getBody().getMessage().contains("Invalid diet data"));
        verify(dietManagerService).saveDietWithShoppingList(testParsedData, TEST_USER_ID, testFileInfo);
    }

    @Test
    void uploadFile_WithDifferentUserId_ShouldUploadForCorrectUser() throws IOException {
        // Arrange
        String differentUserId = "user456";
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        when(storageService.uploadFile(mockFile, differentUserId)).thenReturn(TEST_FILE_URL);

        // Act
        ResponseEntity<Map<String, String>> response = dietManagerController.uploadFile(mockFile, differentUserId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(storageService).uploadFile(mockFile, differentUserId);
    }

    @Test
    void saveDiet_WithComplexRequest_ShouldPassAllParametersCorrectly() {
        // Arrange
        ParsedDietData complexParsedData = new ParsedDietData();
        DietFileInfo complexFileInfo = new DietFileInfo();
        SaveDietRequest complexRequest = new SaveDietRequest(complexParsedData, TEST_USER_ID, complexFileInfo);

        when(dietManagerService.saveDietWithShoppingList(
                eq(complexParsedData),
                eq(TEST_USER_ID),
                eq(complexFileInfo)
        )).thenReturn(TEST_DIET_ID);

        // Act
        ResponseEntity<SaveDietResponse> response = dietManagerController.saveDiet(complexRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dietManagerService).saveDietWithShoppingList(complexParsedData, TEST_USER_ID, complexFileInfo);
    }
}
