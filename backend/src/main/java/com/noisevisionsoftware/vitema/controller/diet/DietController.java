package com.noisevisionsoftware.vitema.controller.diet;

import com.noisevisionsoftware.vitema.dto.request.diet.DietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.DietInfo;
import com.noisevisionsoftware.vitema.dto.response.diet.DietResponse;
import com.noisevisionsoftware.vitema.exception.DietOverlapException;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.diet.DietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.service.diet.DietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diets")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DietController {
    private final DietService dietService;
    private final DietMapper dietMapper;

    @GetMapping
    public ResponseEntity<List<DietResponse>> getAllDiets(
            @RequestParam(required = false) String userId) {
        List<Diet> diets = userId != null ?
                dietService.getDietsByUserId(userId) :
                dietService.getAllDiets();
        return ResponseEntity.ok(diets.stream()
                .map(dietMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DietResponse> getDietById(@PathVariable String id) {
        Diet diet = dietService.getDietById(id);
        return ResponseEntity.ok(dietMapper.toResponse(diet));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, DietInfo>> getDietsInfo(
            @RequestParam String userIds
    ) {
        List<String> userIdList = Arrays.asList(userIds.split(","));
        Map<String, DietInfo> dietInfo = dietService.getDietsInfoForUsers(userIdList);
        return ResponseEntity.ok(dietInfo);
    }

    @PostMapping
    public ResponseEntity<DietResponse> createDiet(
            @Valid @RequestBody DietRequest request) {
        Diet diet = dietMapper.toDomain(request);
        Diet savedDiet = dietService.createDiet(diet);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dietMapper.toResponse(savedDiet));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietResponse> updateDiet(
            @PathVariable String id,
            @Valid @RequestBody DietRequest request) {
        try {
            Diet diet = dietMapper.toDomain(request);
            diet.setId(id);

            Diet updatedDiet = dietService.updateDiet(diet);

            return ResponseEntity.ok(dietMapper.toResponse(updatedDiet));
        } catch (Exception e) {
            log.error("Error updating diet with ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiet(@PathVariable String id) {
        dietService.deleteDiet(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(DietOverlapException.class)
    public ResponseEntity<ProblemDetail> handleDietOverlapException(DietOverlapException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Konflikt terminów diet");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem);
    }
}