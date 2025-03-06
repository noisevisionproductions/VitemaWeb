package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.controller.diet.DietController;
import com.noisevisionsoftware.nutrilog.dto.request.diet.DietRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.DietResponse;
import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.mapper.diet.DietMapper;
import com.noisevisionsoftware.nutrilog.model.diet.Diet;
import com.noisevisionsoftware.nutrilog.service.DietService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietControllerTest {

    @Mock
    private DietService dietService;

    @Mock
    private DietMapper dietMapper;

    @InjectMocks
    private DietController dietController;

    private Diet testDiet;
    private DietRequest testDietRequest;
    private DietResponse testDietResponse;
    private static final String TEST_ID = "test123";
    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        testDiet = Diet.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .build();

        testDietRequest = DietRequest.builder()
                .userId(TEST_USER_ID)
                .build();

        testDietResponse = DietResponse.builder()
                .id(TEST_ID)
                .userId(TEST_USER_ID)
                .build();
    }

    @Test
    void getAllDiets_WhenNoUserIdProvided_ShouldReturnAllDiets() {
        // Arrange
        List<Diet> diets = Collections.singletonList(testDiet);
        when(dietService.getAllDiets()).thenReturn(diets);
        when(dietMapper.toResponse(testDiet)).thenReturn(testDietResponse);

        // Act
        ResponseEntity<List<DietResponse>> response = dietController.getAllDiets(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testDietResponse, response.getBody().getFirst());
        verify(dietService).getAllDiets();
        verify(dietService, never()).getDietsByUserId(any());
    }

    @Test
    void getAllDiets_WhenUserIdProvided_ShouldReturnUserDiets() {
        // Arrange
        List<Diet> diets = Collections.singletonList(testDiet);
        when(dietService.getDietsByUserId(TEST_USER_ID)).thenReturn(diets);
        when(dietMapper.toResponse(testDiet)).thenReturn(testDietResponse);

        // Act
        ResponseEntity<List<DietResponse>> response = dietController.getAllDiets(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testDietResponse, response.getBody().getFirst());
        verify(dietService).getDietsByUserId(TEST_USER_ID);
        verify(dietService, never()).getAllDiets();
    }

    @Test
    void getDietById_WhenDietExists_ShouldReturnDiet() {
        // Arrange
        when(dietService.getDietById(TEST_ID)).thenReturn(testDiet);
        when(dietMapper.toResponse(testDiet)).thenReturn(testDietResponse);

        // Act
        ResponseEntity<DietResponse> response = dietController.getDietById(TEST_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testDietResponse, response.getBody());
        verify(dietService).getDietById(TEST_ID);
    }

    @Test
    void createDiet_ShouldReturnCreatedDiet() {
        // Arrange
        when(dietMapper.toDomain(testDietRequest)).thenReturn(testDiet);
        when(dietService.createDiet(testDiet)).thenReturn(testDiet);
        when(dietMapper.toResponse(testDiet)).thenReturn(testDietResponse);

        // Act
        ResponseEntity<DietResponse> response = dietController.createDiet(testDietRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testDietResponse, response.getBody());
        verify(dietService).createDiet(testDiet);
    }

    @Test
    void updateDiet_WhenDietExists_ShouldReturnUpdatedDiet() {
        // Arrange
        when(dietMapper.toDomain(testDietRequest)).thenReturn(testDiet);
        when(dietService.updateDiet(testDiet)).thenReturn(testDiet);
        when(dietMapper.toResponse(testDiet)).thenReturn(testDietResponse);

        // Act
        ResponseEntity<DietResponse> response = dietController.updateDiet(TEST_ID, testDietRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testDietResponse, response.getBody());
        assertEquals(TEST_ID, testDiet.getId());
        verify(dietService).updateDiet(testDiet);
    }

    @Test
    void deleteDiet_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(dietService).deleteDiet(TEST_ID);

        // Act
        ResponseEntity<Void> response = dietController.deleteDiet(TEST_ID);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(dietService).deleteDiet(TEST_ID);
    }

    @Test
    void getAllDiets_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(dietService.getAllDiets()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> dietController.getAllDiets(null));
        verify(dietService).getAllDiets();
    }

    @Test
    void getAllDiets_WhenServiceThrowsNotFoundException_ShouldPropagateException() {
        // Arrange
        when(dietService.getAllDiets())
                .thenThrow(new NotFoundException("Diets not found"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> dietController.getAllDiets(null));
        verify(dietService).getAllDiets();
    }

    @Test
    void handleNotFoundException_ShouldReturnNotFoundResponse() {
        // Arrange
        NotFoundException ex = new NotFoundException("Diet not found");

        // Act
        ResponseEntity<ProblemDetail> response = dietController.handleNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Diet not found", response.getBody().getDetail());
    }

    @Test
    void getAllDiets_WhenNoDietsFound_ShouldReturnEmptyList() {
        // Arrange
        when(dietService.getAllDiets()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<DietResponse>> response = dietController.getAllDiets(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(dietService).getAllDiets();
    }

    @Test
    void getDietById_WhenDietNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(dietService.getDietById(TEST_ID))
                .thenThrow(new NotFoundException("Diet not found"));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> dietController.getDietById(TEST_ID));
        verify(dietService).getDietById(TEST_ID);
    }
}