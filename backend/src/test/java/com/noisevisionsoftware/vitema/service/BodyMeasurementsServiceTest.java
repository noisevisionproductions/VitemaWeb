package com.noisevisionsoftware.vitema.service;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.repository.BodyMeasurementsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BodyMeasurementsServiceTest {

    @Mock
    private BodyMeasurementsRepository measurementsRepository;

    @InjectMocks
    private BodyMeasurementsService measurementsService;

    private String testUserId;
    private String testMeasurementId;
    private BodyMeasurements testMeasurement;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
        testMeasurementId = "measurement123";

        testMeasurement = new BodyMeasurements();
        testMeasurement.setId(testMeasurementId);
        testMeasurement.setUserId(testUserId);
        testMeasurement.setWeight(75.5);
        testMeasurement.setHeight(180.0);
        testMeasurement.setDate(Timestamp.now());
    }

    @Test
    void getMeasurementsByUserId_ShouldReturnListOfMeasurements() {
        // given
        List<BodyMeasurements> expectedMeasurements = Collections.singletonList(testMeasurement);
        when(measurementsRepository.findByUserId(testUserId)).thenReturn(expectedMeasurements);

        // when
        List<BodyMeasurements> result = measurementsService.getMeasurementsByUserId(testUserId);

        // then
        assertThat(result).isEqualTo(expectedMeasurements);
        verify(measurementsRepository).findByUserId(testUserId);
    }

    @Test
    void getMeasurementById_WhenMeasurementExists_ShouldReturnMeasurement() {
        // given
        when(measurementsRepository.findById(testMeasurementId)).thenReturn(Optional.of(testMeasurement));

        // when
        BodyMeasurements result = measurementsService.getMeasurementById(testMeasurementId);

        // then
        assertThat(result).isEqualTo(testMeasurement);
        verify(measurementsRepository).findById(testMeasurementId);
    }

    @Test
    void getMeasurementById_WhenMeasurementDoesNotExist_ShouldThrowNotFoundException() {
        // given
        when(measurementsRepository.findById(testMeasurementId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> measurementsService.getMeasurementById(testMeasurementId));
        verify(measurementsRepository).findById(testMeasurementId);
    }

    @Test
    void createMeasurement_ShouldSetUserIdAndSaveMeasurement() {
        // given
        BodyMeasurements newMeasurement = new BodyMeasurements();
        newMeasurement.setWeight(80.0);
        newMeasurement.setHeight(185.0);

        // when
        BodyMeasurements result = measurementsService.createMeasurement(newMeasurement, testUserId);

        // then
        assertThat(result).isEqualTo(newMeasurement);
        assertThat(result.getUserId()).isEqualTo(testUserId);
        verify(measurementsRepository).save(newMeasurement);
    }

    @Test
    void updateMeasurement_WhenMeasurementExistsAndUserMatch_ShouldUpdateMeasurement() {
        // given
        BodyMeasurements updatedMeasurement = new BodyMeasurements();
        updatedMeasurement.setWeight(82.0);
        updatedMeasurement.setHeight(185.0);

        when(measurementsRepository.findById(testMeasurementId)).thenReturn(Optional.of(testMeasurement));

        // when
        BodyMeasurements result = measurementsService.updateMeasurement(testMeasurementId, updatedMeasurement, testUserId);

        // then
        assertThat(result).isEqualTo(updatedMeasurement);
        assertThat(result.getId()).isEqualTo(testMeasurementId);
        assertThat(result.getUserId()).isEqualTo(testUserId);
        verify(measurementsRepository).findById(testMeasurementId);
        verify(measurementsRepository).save(updatedMeasurement);
    }

    @Test
    void updateMeasurement_WhenUserDoesNotMatch_ShouldThrowIllegalArgumentException() {
        // given
        String differentUserId = "differentUser";
        BodyMeasurements updatedMeasurement = new BodyMeasurements();

        when(measurementsRepository.findById(testMeasurementId)).thenReturn(Optional.of(testMeasurement));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> measurementsService.updateMeasurement(testMeasurementId, updatedMeasurement, differentUserId));

        verify(measurementsRepository).findById(testMeasurementId);
        verify(measurementsRepository, never()).save(any());
    }

    @Test
    void deleteMeasurement_WhenMeasurementExistsAndUserMatch_ShouldDeleteMeasurement() {
        // given
        when(measurementsRepository.findById(testMeasurementId)).thenReturn(Optional.of(testMeasurement));

        // when
        measurementsService.deleteMeasurement(testMeasurementId, testUserId);

        // then
        verify(measurementsRepository).findById(testMeasurementId);
        verify(measurementsRepository).delete(testMeasurementId);
    }

    @Test
    void deleteMeasurement_WhenUserDoesNotMatch_ShouldThrowIllegalArgumentException() {
        // given
        String differentUserId = "differentUser";
        when(measurementsRepository.findById(testMeasurementId)).thenReturn(Optional.of(testMeasurement));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> measurementsService.deleteMeasurement(testMeasurementId, differentUserId));

        verify(measurementsRepository).findById(testMeasurementId);
        verify(measurementsRepository, never()).delete(anyString());
    }
}