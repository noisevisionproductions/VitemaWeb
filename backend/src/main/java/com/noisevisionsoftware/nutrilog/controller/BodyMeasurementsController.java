package com.noisevisionsoftware.nutrilog.controller;

import com.noisevisionsoftware.nutrilog.dto.request.BodyMeasurementsRequest;
import com.noisevisionsoftware.nutrilog.dto.response.BodyMeasurementsResponse;
import com.noisevisionsoftware.nutrilog.mapper.measurements.BodyMeasurementsMapper;
import com.noisevisionsoftware.nutrilog.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.nutrilog.security.model.FirebaseUser;
import com.noisevisionsoftware.nutrilog.service.BodyMeasurementsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class BodyMeasurementsController {
    private final BodyMeasurementsService measurementsService;
    private final BodyMeasurementsMapper measurementsMapper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BodyMeasurementsResponse>> getUserMeasurements(
            @PathVariable String userId,
            @AuthenticationPrincipal FirebaseUser currentUser) {

        if (!currentUser.getUid().equals(userId) && !currentUser.getRole().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<BodyMeasurements> measurements = measurementsService.getMeasurementsByUserId(userId);
        return ResponseEntity.ok(
                measurements.stream()
                        .map(measurementsMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public ResponseEntity<BodyMeasurementsResponse> createMeasurement(
            @Valid @RequestBody BodyMeasurementsRequest request,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        BodyMeasurements measurements = measurementsMapper.toModel(request, currentUser.getUid());
        BodyMeasurements created = measurementsService.createMeasurement(measurements, currentUser.getUid());
        return ResponseEntity.ok(measurementsMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BodyMeasurementsResponse> updateMeasurement(
            @PathVariable String id,
            @Valid @RequestBody BodyMeasurementsRequest request,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        BodyMeasurements measurements = measurementsMapper.toModel(request, currentUser.getUid());
        BodyMeasurements updated = measurementsService.updateMeasurement(id, measurements, currentUser.getUid());
        return ResponseEntity.ok(measurementsMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeasurement(
            @PathVariable String id,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        measurementsService.deleteMeasurement(id, currentUser.getUid());
        return ResponseEntity.noContent().build();
    }
}