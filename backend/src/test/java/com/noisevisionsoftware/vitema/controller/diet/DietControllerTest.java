package com.noisevisionsoftware.vitema.controller.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.diet.DietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.DietInfo;
import com.noisevisionsoftware.vitema.dto.response.diet.DietResponse;
import com.noisevisionsoftware.vitema.exception.DietOverlapException;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.diet.DietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.service.diet.DietService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.*;

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

    // GET /api/diets/info - getDietsInfo tests

    @Test
    void getDietsInfo_WithSingleUserId_ShouldReturnDietInfo() {
        // Arrange
        List<String> userIdList = Collections.singletonList(TEST_USER_ID);
        Timestamp startDate = Timestamp.now();
        Timestamp endDate = Timestamp.now();
        DietInfo dietInfo = DietInfo.builder()
                .hasDiet(true)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        Map<String, DietInfo> dietInfoMap = new HashMap<>();
        dietInfoMap.put(TEST_USER_ID, dietInfo);

        when(dietService.getDietsInfoForUsers(userIdList)).thenReturn(dietInfoMap);

        // Act
        ResponseEntity<Map<String, DietInfo>> response = dietController.getDietsInfo(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().containsKey(TEST_USER_ID));
        assertEquals(dietInfo, response.getBody().get(TEST_USER_ID));
        verify(dietService).getDietsInfoForUsers(userIdList);
    }

    @Test
    void getDietsInfo_WithMultipleUserIds_ShouldReturnMultipleDietInfos() {
        // Arrange
        String user2Id = "user456";
        String userIds = TEST_USER_ID + "," + user2Id;
        List<String> userIdList = Arrays.asList(TEST_USER_ID, user2Id);

        DietInfo dietInfo1 = DietInfo.builder()
                .hasDiet(true)
                .startDate(Timestamp.now())
                .endDate(Timestamp.now())
                .build();
        DietInfo dietInfo2 = DietInfo.builder()
                .hasDiet(false)
                .startDate(null)
                .endDate(null)
                .build();

        Map<String, DietInfo> dietInfoMap = new HashMap<>();
        dietInfoMap.put(TEST_USER_ID, dietInfo1);
        dietInfoMap.put(user2Id, dietInfo2);

        when(dietService.getDietsInfoForUsers(userIdList)).thenReturn(dietInfoMap);

        // Act
        ResponseEntity<Map<String, DietInfo>> response = dietController.getDietsInfo(userIds);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(TEST_USER_ID).isHasDiet());
        assertFalse(response.getBody().get(user2Id).isHasDiet());
        verify(dietService).getDietsInfoForUsers(userIdList);
    }

    @Test
    void getDietsInfo_WithUserIdsContainingSpaces_ShouldHandleCorrectly() {
        // Arrange
        String user2Id = "user456";
        String userIds = TEST_USER_ID + ", " + user2Id;
        List<String> userIdList = Arrays.asList(TEST_USER_ID, " " + user2Id);

        Map<String, DietInfo> dietInfoMap = new HashMap<>();
        when(dietService.getDietsInfoForUsers(userIdList)).thenReturn(dietInfoMap);

        // Act
        ResponseEntity<Map<String, DietInfo>> response = dietController.getDietsInfo(userIds);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(dietService).getDietsInfoForUsers(userIdList);
    }

    @Test
    void getDietsInfo_WhenServiceReturnsEmptyMap_ShouldReturnEmptyMap() {
        // Arrange
        List<String> userIdList = Collections.singletonList(TEST_USER_ID);
        when(dietService.getDietsInfoForUsers(userIdList)).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<Map<String, DietInfo>> response = dietController.getDietsInfo(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(dietService).getDietsInfoForUsers(userIdList);
    }

    // Exception Handler tests

    @Test
    void handleDietOverlapException_ShouldReturnConflictResponse() {
        // Arrange
        DietOverlapException ex = new DietOverlapException("Diety nakładają się na siebie");

        // Act
        ResponseEntity<ProblemDetail> response = dietController.handleDietOverlapException(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Diety nakładają się na siebie", response.getBody().getDetail());
        assertEquals("Konflikt terminów diet", response.getBody().getTitle());
    }

    @Test
    void handleDietOverlapException_WithDifferentMessage_ShouldReturnCorrectMessage() {
        // Arrange
        String customMessage = "Diet overlaps with existing diet from 2024-01-01 to 2024-01-31";
        DietOverlapException ex = new DietOverlapException(customMessage);

        // Act
        ResponseEntity<ProblemDetail> response = dietController.handleDietOverlapException(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(customMessage, response.getBody().getDetail());
        assertEquals("Konflikt terminów diet", response.getBody().getTitle());
    }

    // Additional edge case tests

    @Test
    void updateDiet_WhenUpdateThrowsException_ShouldPropagateException() {
        // Arrange
        when(dietMapper.toDomain(testDietRequest)).thenReturn(testDiet);
        when(dietService.updateDiet(testDiet))
                .thenThrow(new RuntimeException("Update failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> dietController.updateDiet(TEST_ID, testDietRequest));
        verify(dietService).updateDiet(testDiet);
    }

    @Test
    void updateDiet_WhenDietNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(dietMapper.toDomain(testDietRequest)).thenReturn(testDiet);
        when(dietService.updateDiet(testDiet))
                .thenThrow(new NotFoundException("Diet not found"));

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> dietController.updateDiet(TEST_ID, testDietRequest));
        verify(dietService).updateDiet(testDiet);
    }

    @Test
    void createDiet_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(dietMapper.toDomain(testDietRequest)).thenReturn(testDiet);
        when(dietService.createDiet(testDiet))
                .thenThrow(new RuntimeException("Creation failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> dietController.createDiet(testDietRequest));
        verify(dietService).createDiet(testDiet);
    }

    @Test
    void deleteDiet_WhenDietNotFound_ShouldThrowNotFoundException() {
        // Arrange
        doThrow(new NotFoundException("Diet not found"))
                .when(dietService).deleteDiet(TEST_ID);

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> dietController.deleteDiet(TEST_ID));
        verify(dietService).deleteDiet(TEST_ID);
    }

    @Test
    void getAllDiets_WithUserIdAndMultipleDiets_ShouldReturnAllUserDiets() {
        // Arrange
        Diet diet2 = Diet.builder()
                .id("test456")
                .userId(TEST_USER_ID)
                .build();
        DietResponse response2 = DietResponse.builder()
                .id("test456")
                .userId(TEST_USER_ID)
                .build();

        List<Diet> diets = Arrays.asList(testDiet, diet2);
        when(dietService.getDietsByUserId(TEST_USER_ID)).thenReturn(diets);
        when(dietMapper.toResponse(testDiet)).thenReturn(testDietResponse);
        when(dietMapper.toResponse(diet2)).thenReturn(response2);

        // Act
        ResponseEntity<List<DietResponse>> response = dietController.getAllDiets(TEST_USER_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(dietService).getDietsByUserId(TEST_USER_ID);
        verify(dietMapper, times(2)).toResponse(any(Diet.class));
    }
}