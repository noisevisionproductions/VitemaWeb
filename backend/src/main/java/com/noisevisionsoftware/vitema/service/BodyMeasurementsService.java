package com.noisevisionsoftware.vitema.service;

import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.repository.BodyMeasurementsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BodyMeasurementsService {
    private final BodyMeasurementsRepository measurementsRepository;

    @Cacheable(value = "measurementsCache", key = "#userId")
    public List<BodyMeasurements> getMeasurementsByUserId(String userId) {
        return measurementsRepository.findByUserId(userId);
    }

    public BodyMeasurements getMeasurementById(String id) {
        return measurementsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Measurement not found: " + id));
    }

    @CacheEvict(value = "measurementsCache", key = "#userId")
    public BodyMeasurements createMeasurement(BodyMeasurements measurements, String userId) {
        measurements.setUserId(userId);
        measurementsRepository.save(measurements);
        return measurements;
    }

    @CacheEvict(value = "measurementsCache", key = "#userId")
    public BodyMeasurements updateMeasurement(String id, BodyMeasurements measurements, String userId) {
        BodyMeasurements existing = getMeasurementById(id);
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cannot update measurements for different user");
        }

        measurements.setId(id);
        measurements.setUserId(userId);
        measurementsRepository.save(measurements);
        return measurements;
    }

    @CacheEvict(value = "measurementsCache", key = "#userId")
    public void deleteMeasurement(String id, String userId) {
        BodyMeasurements existing = getMeasurementById(id);
        if (!existing.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete measurements for different user");
        }

        measurementsRepository.delete(id);
    }
}