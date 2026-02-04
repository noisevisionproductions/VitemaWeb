package com.noisevisionsoftware.vitema.controller.diet;

import com.noisevisionsoftware.vitema.dto.diet.DietDraftDto;
import com.noisevisionsoftware.vitema.dto.diet.DietHistorySummaryDto;
import com.noisevisionsoftware.vitema.dto.request.diet.SaveDietRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.UpdateDietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.SaveDietResponse;
import com.noisevisionsoftware.vitema.dto.search.UnifiedSearchDto;
import com.noisevisionsoftware.vitema.service.diet.DietManagerService;
import com.noisevisionsoftware.vitema.service.firebase.FileStorageService;
import com.noisevisionsoftware.vitema.service.search.UnifiedSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/diets/manager")
@RequiredArgsConstructor
@Slf4j
public class DietManagerController {

    private final DietManagerService dietManagerService;
    private final FileStorageService storageService;
    private final UnifiedSearchService unifiedSearchService;

    /**
     * Unified search: returns both recipes and products in one response.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UnifiedSearchDto>> search(
            @RequestParam String query,
            @RequestParam(required = false) String trainerId
    ) {
        List<UnifiedSearchDto> results = unifiedSearchService.search(query, trainerId);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Nie można przesłać pustego pliku"));
            }

            String fileUrl = storageService.uploadFile(file, userId);

            return ResponseEntity.ok(Map.of(
                    "fileUrl", fileUrl,
                    "fileName", Objects.requireNonNull(file.getOriginalFilename()),
                    "message", "Plik został pomyślnie przesłany"
            ));
        } catch (Exception e) {
            log.error("Błąd podczas uploadowania pliku", e);
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("message", "Wystąpił błąd podczas uploadowania pliku: " + e.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<SaveDietResponse> saveDiet(@RequestBody SaveDietRequest request) {
        try {
            String dietId = dietManagerService.saveDietWithShoppingList(
                    request.getParsedData(),
                    request.getUserId(),
                    request.getAuthorId(),
                    request.getFileInfo()
            );

            return ResponseEntity.ok(new SaveDietResponse(
                    dietId,
                    "Dieta została pomyślnie zapisana"
            ));
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania diety", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new SaveDietResponse(
                            null,
                            "Wystąpił błąd podczas zapisywania diety: " + e.getMessage()
                    ));
        }
    }

    @PutMapping("/{dietId}/structure")
    public ResponseEntity<Map<String, String>> updateDietStructure(
            @PathVariable String dietId,
            @RequestBody UpdateDietRequest request
    ) {
        try {
            if (request.getDays() == null || request.getDays().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Lista dni nie może być pusta"));
            }

            dietManagerService.updateDietStructure(dietId, request.getDays());

            return ResponseEntity.ok(Map.of(
                    "message", "Dieta została pomyślnie zaktualizowana"
            ));

        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji diety {}", dietId, e);
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("message", "Wystąpił błąd: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<DietHistorySummaryDto>> getDietHistory(
            @RequestParam String trainerId
    ) {
        return ResponseEntity.ok(dietManagerService.getTrainerDietHistory(trainerId));
    }

    @GetMapping("/draft/{dietId}")
    public ResponseEntity<DietDraftDto> getDietDraft(@PathVariable String dietId) {
        return ResponseEntity.ok(dietManagerService.getDietAsDraft(dietId));
    }
}