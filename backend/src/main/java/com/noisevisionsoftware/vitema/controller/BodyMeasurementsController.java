package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.BodyMeasurementsRequest;
import com.noisevisionsoftware.vitema.dto.response.ErrorResponse;
import com.noisevisionsoftware.vitema.exception.MeasurementNotFoundException;
import com.noisevisionsoftware.vitema.mapper.measurements.BodyMeasurementsMapper;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import com.noisevisionsoftware.vitema.security.model.FirebaseUser;
import com.noisevisionsoftware.vitema.service.BodyMeasurementsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
public class BodyMeasurementsController {
    private final BodyMeasurementsService measurementsService;
    private final BodyMeasurementsMapper measurementsMapper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMeasurements(
            @PathVariable String userId,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        try {
            if (!currentUser.getUid().equals(userId) &&
                    !currentUser.getRole().equals("ADMIN") &&
                    !currentUser.getRole().equals("OWNER")) {
                throw new AccessDeniedException("Nie masz uprawnień do wyświetlania pomiarów tego użytkownika");
            }

            List<BodyMeasurements> measurements = measurementsService.getMeasurementsByUserId(userId);

            return ResponseEntity.ok(
                    measurements.stream()
                            .map(measurementsMapper::toResponse)
                            .collect(Collectors.toList())
            );
        } catch (AccessDeniedException e) {
            log.error("Błąd dostępu: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas pobierania pomiarów dla użytkownika {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Wystąpił błąd podczas pobierania pomiarów: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createMeasurement(
            @Valid @RequestBody BodyMeasurementsRequest request,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        try {
            BodyMeasurements measurements = measurementsMapper.toModel(request, currentUser.getUid());
            BodyMeasurements created = measurementsService.createMeasurement(measurements, currentUser.getUid());

            return ResponseEntity.ok(measurementsMapper.toResponse(created));
        } catch (Exception e) {
            log.error("Błąd podczas tworzenia pomiaru dla użytkownika {}: {}", currentUser.getUid(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Wystąpił błąd podczas tworzenia pomiaru: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeasurement(
            @PathVariable String id,
            @Valid @RequestBody BodyMeasurementsRequest request,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        try {
            BodyMeasurements measurements = measurementsMapper.toModel(request, currentUser.getUid());
            BodyMeasurements updated = measurementsService.updateMeasurement(id, measurements, currentUser.getUid());

            return ResponseEntity.ok(measurementsMapper.toResponse(updated));
        } catch (MeasurementNotFoundException e) {
            log.error("Nie znaleziono pomiaru o ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Odmowa dostępu podczas aktualizacji pomiaru o ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas aktualizacji pomiaru o ID: {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Wystąpił błąd podczas aktualizacji pomiaru: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeasurement(
            @PathVariable String id,
            @AuthenticationPrincipal FirebaseUser currentUser) {
        try {
            measurementsService.deleteMeasurement(id, currentUser.getUid());
            return ResponseEntity.noContent().build();
        } catch (MeasurementNotFoundException e) {
            log.error("Nie znaleziono pomiaru o ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Odmowa dostępu podczas usuwania pomiaru o ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage()));
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas usuwania pomiaru o ID: {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Wystąpił błąd podczas usuwania pomiaru: " + e.getMessage()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Wystąpił nieoczekiwany błąd: " + e.getMessage()));
    }
}