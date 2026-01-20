package com.noisevisionsoftware.vitema.controller.diet;

import com.noisevisionsoftware.vitema.dto.request.diet.SaveDietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.SaveDietResponse;
import com.noisevisionsoftware.vitema.service.diet.DietManagerService;
import com.noisevisionsoftware.vitema.service.firebase.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/diets/manager")
@RequiredArgsConstructor
@Slf4j
public class DietManagerController {

    private final DietManagerService dietManagerService;
    private final FileStorageService storageService;

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
}