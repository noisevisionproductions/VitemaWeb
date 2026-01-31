package com.noisevisionsoftware.vitema.controller;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.BodyMeasurementsRequest;
import com.noisevisionsoftware.vitema.dto.response.BodyMeasurementsResponse;
import com.noisevisionsoftware.vitema.dto.response.ErrorResponse;
import com.noisevisionsoftware.vitema.exception.MeasurementNotFoundException;
import com.noisevisionsoftware.vitema.mapper.measurements.BodyMeasurementsMapper;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementSourceType;
import com.noisevisionsoftware.vitema.model.measurements.MeasurementType;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.BodyMeasurementsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BodyMeasurementsControllerTest {

    @Mock
    private BodyMeasurementsService measurementsService;

    @Mock
    private BodyMeasurementsMapper measurementsMapper;

    @InjectMocks
    private BodyMeasurementsController bodyMeasurementsController;

    private FirebaseUser adminUser;
    private FirebaseUser trainerUser;
    private FirebaseUser regularUser;
    private BodyMeasurementsRequest measurementRequest;
    private BodyMeasurements measurementModel;
    private BodyMeasurementsResponse measurementResponse;
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_ADMIN_ID = "admin123";
    private static final String TEST_TRAINER_ID = "trainer123";
    private static final String TEST_MEASUREMENT_ID = "measurement123";
    private static final Timestamp TEST_DATE = Timestamp.now();

    @BeforeEach
    void setUp() {
        adminUser = FirebaseUser.builder()
                .uid(TEST_ADMIN_ID)
                .email("admin@example.com")
                .role("ADMIN")
                .build();

        trainerUser = FirebaseUser.builder()
                .uid(TEST_TRAINER_ID)
                .email("trainer@example.com")
                .role("TRAINER")
                .build();

        regularUser = FirebaseUser.builder()
                .uid(TEST_USER_ID)
                .email("user@example.com")
                .role("USER")
                .build();

        measurementRequest = new BodyMeasurementsRequest();
        measurementRequest.setDate(TEST_DATE);
        measurementRequest.setHeight(180.0);
        measurementRequest.setWeight(75.0);
        measurementRequest.setNeck(38.0);
        measurementRequest.setBiceps(35.0);
        measurementRequest.setChest(100.0);
        measurementRequest.setWaist(80.0);
        measurementRequest.setBelt(82.0);
        measurementRequest.setHips(95.0);
        measurementRequest.setThigh(60.0);
        measurementRequest.setCalf(38.0);
        measurementRequest.setNote("Test note");
        measurementRequest.setMeasurementType(MeasurementType.FULL_BODY);

        measurementModel = BodyMeasurements.builder()
                .id(TEST_MEASUREMENT_ID)
                .userId(TEST_USER_ID)
                .date(TEST_DATE)
                .height(180.0)
                .weight(75.0)
                .neck(38.0)
                .biceps(35.0)
                .chest(100.0)
                .waist(80.0)
                .belt(82.0)
                .hips(95.0)
                .thigh(60.0)
                .calf(38.0)
                .note("Test note")
                .weekNumber(1)
                .measurementType(MeasurementType.FULL_BODY)
                .sourceType(MeasurementSourceType.APP)
                .build();

        measurementResponse = BodyMeasurementsResponse.builder()
                .id(TEST_MEASUREMENT_ID)
                .userId(TEST_USER_ID)
                .date(TEST_DATE)
                .height(180.0)
                .weight(75.0)
                .neck(38.0)
                .biceps(35.0)
                .chest(100.0)
                .waist(80.0)
                .belt(82.0)
                .hips(95.0)
                .thigh(60.0)
                .calf(38.0)
                .note("Test note")
                .weekNumber(1)
                .measurementType(MeasurementType.FULL_BODY)
                .sourceType(MeasurementSourceType.APP)
                .build();
    }

    // GET /api/measurements/user/{userId} - getUserMeasurements tests

    @Test
    void getUserMeasurements_WhenUserAccessesOwnMeasurements_ShouldReturnOk() {
        // Arrange
        List<BodyMeasurements> measurements = Collections.singletonList(measurementModel);
        when(measurementsService.getMeasurementsByUserId(TEST_USER_ID)).thenReturn(measurements);
        when(measurementsMapper.toResponse(measurementModel)).thenReturn(measurementResponse);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(List.class, response.getBody());
        
        @SuppressWarnings("unchecked")
        List<BodyMeasurementsResponse> responseList = (List<BodyMeasurementsResponse>) response.getBody();
        assertEquals(1, responseList.size());
        assertEquals(measurementResponse, responseList.get(0));
        
        verify(measurementsService).getMeasurementsByUserId(TEST_USER_ID);
        verify(measurementsMapper).toResponse(measurementModel);
    }

    @Test
    void getUserMeasurements_WhenAdminAccessesUserMeasurements_ShouldReturnOk() {
        // Arrange
        List<BodyMeasurements> measurements = Collections.singletonList(measurementModel);
        when(measurementsService.getMeasurementsByUserId(TEST_USER_ID)).thenReturn(measurements);
        when(measurementsMapper.toResponse(measurementModel)).thenReturn(measurementResponse);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, adminUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(measurementsService).getMeasurementsByUserId(TEST_USER_ID);
    }

    @Test
    void getUserMeasurements_WhenOwnerAccessesUserMeasurements_ShouldReturnOk() {
        // Arrange
        FirebaseUser ownerUser = FirebaseUser.builder()
                .uid("owner123")
                .role("OWNER")
                .build();
        List<BodyMeasurements> measurements = Collections.singletonList(measurementModel);
        when(measurementsService.getMeasurementsByUserId(TEST_USER_ID)).thenReturn(measurements);
        when(measurementsMapper.toResponse(measurementModel)).thenReturn(measurementResponse);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, ownerUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(measurementsService).getMeasurementsByUserId(TEST_USER_ID);
    }

    @Test
    void getUserMeasurements_WhenTrainerAccessesDifferentUserMeasurements_ShouldReturnForbidden() {
        // Arrange
        // Trainer trying to access another user's measurements

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, trainerUser);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.getStatus());
        assertEquals("Nie masz uprawnień do wyświetlania pomiarów tego użytkownika", errorResponse.getMessage());
        
        verify(measurementsService, never()).getMeasurementsByUserId(any());
    }

    @Test
    void getUserMeasurements_WhenUserAccessesDifferentUserMeasurements_ShouldReturnForbidden() {
        // Arrange
        FirebaseUser otherUser = FirebaseUser.builder()
                .uid("other123")
                .role("USER")
                .build();

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, otherUser);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        verify(measurementsService, never()).getMeasurementsByUserId(any());
    }

    @Test
    void getUserMeasurements_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(measurementsService.getMeasurementsByUserId(TEST_USER_ID))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Wystąpił błąd podczas pobierania pomiarów"));
        
        verify(measurementsService).getMeasurementsByUserId(TEST_USER_ID);
    }

    @Test
    void getUserMeasurements_WhenNoMeasurementsFound_ShouldReturnEmptyList() {
        // Arrange
        when(measurementsService.getMeasurementsByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(List.class, response.getBody());
        
        @SuppressWarnings("unchecked")
        List<BodyMeasurementsResponse> responseList = (List<BodyMeasurementsResponse>) response.getBody();
        assertTrue(responseList.isEmpty());
        
        verify(measurementsService).getMeasurementsByUserId(TEST_USER_ID);
        verify(measurementsMapper, never()).toResponse(any());
    }

    @Test
    void getUserMeasurements_WhenMultipleMeasurements_ShouldReturnAll() {
        // Arrange
        BodyMeasurements measurement2 = BodyMeasurements.builder()
                .id("measurement456")
                .userId(TEST_USER_ID)
                .date(TEST_DATE)
                .height(181.0)
                .weight(76.0)
                .measurementType(MeasurementType.FULL_BODY)
                .sourceType(MeasurementSourceType.APP)
                .build();
        
        BodyMeasurementsResponse response2 = BodyMeasurementsResponse.builder()
                .id("measurement456")
                .userId(TEST_USER_ID)
                .date(TEST_DATE)
                .height(181.0)
                .weight(76.0)
                .measurementType(MeasurementType.FULL_BODY)
                .sourceType(MeasurementSourceType.APP)
                .build();

        List<BodyMeasurements> measurements = Arrays.asList(measurementModel, measurement2);
        when(measurementsService.getMeasurementsByUserId(TEST_USER_ID)).thenReturn(measurements);
        when(measurementsMapper.toResponse(measurementModel)).thenReturn(measurementResponse);
        when(measurementsMapper.toResponse(measurement2)).thenReturn(response2);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.getUserMeasurements(TEST_USER_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        List<BodyMeasurementsResponse> responseList = (List<BodyMeasurementsResponse>) response.getBody();
        assertEquals(2, responseList.size());
        
        verify(measurementsService).getMeasurementsByUserId(TEST_USER_ID);
        verify(measurementsMapper, times(2)).toResponse(any(BodyMeasurements.class));
    }

    // POST /api/measurements - createMeasurement tests

    @Test
    void createMeasurement_WithValidRequest_ShouldReturnOk() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsService.createMeasurement(measurementModel, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsMapper.toResponse(measurementModel)).thenReturn(measurementResponse);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.createMeasurement(measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(BodyMeasurementsResponse.class, response.getBody());
        assertEquals(measurementResponse, response.getBody());
        
        verify(measurementsMapper).toModel(measurementRequest, TEST_USER_ID);
        verify(measurementsService).createMeasurement(measurementModel, TEST_USER_ID);
        verify(measurementsMapper).toResponse(measurementModel);
    }

    @Test
    void createMeasurement_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsService.createMeasurement(measurementModel, TEST_USER_ID))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.createMeasurement(measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Wystąpił błąd podczas tworzenia pomiaru"));
        
        verify(measurementsMapper).toModel(measurementRequest, TEST_USER_ID);
        verify(measurementsService).createMeasurement(measurementModel, TEST_USER_ID);
        verify(measurementsMapper, never()).toResponse(any());
    }

    @Test
    void createMeasurement_WhenMapperThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID))
                .thenThrow(new RuntimeException("Mapping error"));

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.createMeasurement(measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        verify(measurementsMapper).toModel(measurementRequest, TEST_USER_ID);
        verify(measurementsService, never()).createMeasurement(any(), any());
    }

    // PUT /api/measurements/{id} - updateMeasurement tests

    @Test
    void updateMeasurement_WithValidRequest_ShouldReturnOk() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsService.updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID))
                .thenReturn(measurementModel);
        when(measurementsMapper.toResponse(measurementModel)).thenReturn(measurementResponse);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.updateMeasurement(
                TEST_MEASUREMENT_ID, measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(BodyMeasurementsResponse.class, response.getBody());
        assertEquals(measurementResponse, response.getBody());
        
        verify(measurementsMapper).toModel(measurementRequest, TEST_USER_ID);
        verify(measurementsService).updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID);
        verify(measurementsMapper).toResponse(measurementModel);
    }

    @Test
    void updateMeasurement_WhenMeasurementNotFound_ShouldReturnNotFound() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsService.updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID))
                .thenThrow(new MeasurementNotFoundException(TEST_MEASUREMENT_ID));

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.updateMeasurement(
                TEST_MEASUREMENT_ID, measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Nie znaleziono pomiaru o ID"));
        
        verify(measurementsService).updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID);
        verify(measurementsMapper, never()).toResponse(any());
    }

    @Test
    void updateMeasurement_WhenAccessDenied_ShouldReturnForbidden() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsService.updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID))
                .thenThrow(new AccessDeniedException("Access denied"));

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.updateMeasurement(
                TEST_MEASUREMENT_ID, measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.getStatus());
        assertEquals("Access denied", errorResponse.getMessage());
        
        verify(measurementsService).updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID);
        verify(measurementsMapper, never()).toResponse(any());
    }

    @Test
    void updateMeasurement_WhenServiceThrowsGenericException_ShouldReturnInternalServerError() {
        // Arrange
        when(measurementsMapper.toModel(measurementRequest, TEST_USER_ID)).thenReturn(measurementModel);
        when(measurementsService.updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.updateMeasurement(
                TEST_MEASUREMENT_ID, measurementRequest, regularUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Wystąpił błąd podczas aktualizacji pomiaru"));
        
        verify(measurementsService).updateMeasurement(TEST_MEASUREMENT_ID, measurementModel, TEST_USER_ID);
    }

    // DELETE /api/measurements/{id} - deleteMeasurement tests

    @Test
    void deleteMeasurement_WithValidId_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.deleteMeasurement(TEST_MEASUREMENT_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);
    }

    @Test
    void deleteMeasurement_WhenMeasurementNotFound_ShouldReturnNotFound() {
        // Arrange
        doThrow(new MeasurementNotFoundException(TEST_MEASUREMENT_ID))
                .when(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.deleteMeasurement(TEST_MEASUREMENT_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Nie znaleziono pomiaru o ID"));
        
        verify(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);
    }

    @Test
    void deleteMeasurement_WhenAccessDenied_ShouldReturnForbidden() {
        // Arrange
        doThrow(new AccessDeniedException("Access denied"))
                .when(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.deleteMeasurement(TEST_MEASUREMENT_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.getStatus());
        assertEquals("Access denied", errorResponse.getMessage());
        
        verify(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);
    }

    @Test
    void deleteMeasurement_WhenServiceThrowsGenericException_ShouldReturnInternalServerError() {
        // Arrange
        doThrow(new RuntimeException("Unexpected error"))
                .when(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);

        // Act
        ResponseEntity<?> response = bodyMeasurementsController.deleteMeasurement(TEST_MEASUREMENT_ID, regularUser);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("Wystąpił błąd podczas usuwania pomiaru"));
        
        verify(measurementsService).deleteMeasurement(TEST_MEASUREMENT_ID, TEST_USER_ID);
    }

    // Exception handler tests

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new Exception("Global exception");

        // Act
        ResponseEntity<ErrorResponse> response = bodyMeasurementsController.handleGlobalException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Wystąpił nieoczekiwany błąd"));
    }
}
