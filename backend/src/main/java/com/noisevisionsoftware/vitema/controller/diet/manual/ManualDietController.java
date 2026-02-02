package com.noisevisionsoftware.vitema.controller.diet.manual;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.PreviewMealSaveRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.SaveMealTemplateRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.*;
import com.noisevisionsoftware.vitema.service.diet.manual.ManualDietService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diets/manual")
@RequiredArgsConstructor
@Slf4j
public class ManualDietController {

    private final ManualDietService manualDietService;

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<ManualDietResponse> saveManualDiet(
            @RequestBody ManualDietRequest request) {
        try {
            String dietId = manualDietService.saveManualDiet(request);
            return ResponseEntity.ok(new ManualDietResponse(dietId, "Dieta została pomyślnie zapisana"));
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania ręcznej diety", e);
            return ResponseEntity.internalServerError()
                    .body(new ManualDietResponse(null, "Wystąpił błąd: " + e.getMessage()));
        }
    }

    @GetMapping("/ingredients/search")
    public ResponseEntity<List<IngredientDTO>> searchIngredients(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<IngredientDTO> ingredients = manualDietService.searchIngredients(query, limit);
            return ResponseEntity.ok(ingredients);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania składników", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ingredients")
    public ResponseEntity<IngredientDTO> createIngredient(@RequestBody IngredientDTO ingredient) {
        try {
            IngredientDTO savedIngredient = manualDietService.createIngredient(ingredient);
            return ResponseEntity.ok(savedIngredient);
        } catch (Exception e) {
            log.error("Błąd podczas tworzenia składnika", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateDiet(@RequestBody ManualDietRequest request) {
        try {
            Map<String, Object> validation = manualDietService.validateManualDiet(request);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            log.error("Błąd podczas walidacji diety", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/meals/{id}")
    public ResponseEntity<MealTemplateResponse> getMealTemplate(@PathVariable String id) {
        try {
            MealTemplateResponse template = manualDietService.getMealTemplate(id);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonu posiłku: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/meals/save-template")
    @Transactional
    public ResponseEntity<MealTemplateResponse> saveMealTemplate(
            @RequestBody SaveMealTemplateRequest request) {
        try {
            MealTemplateResponse savedTemplate = manualDietService.saveMealTemplate(request);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania szablonu posiłku", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/meals/preview-save")
    public ResponseEntity<MealSavePreviewResponse> previewMealSave(
            @RequestBody PreviewMealSaveRequest request) {
        try {
            MealSavePreviewResponse preview = manualDietService.previewMealSave(request);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("Błąd podczas podglądu zapisywania posiłku", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/meals/upload-image")
    public ResponseEntity<MealImageResponse> uploadMealImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(required = false) String mealId
    ) {
        try {
            String imageUrl = manualDietService.uploadMealImage(image, mealId);
            return ResponseEntity.ok(new MealImageResponse(imageUrl));
        } catch (Exception e) {
            log.error("Błąd podczas przesyłania zdjęcia posiłku", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/meals/upload-base64-image")
    public ResponseEntity<MealImageResponse> uploadBase64MealImage(
            @RequestBody Map<String, String> request
    ) {
        try {
            String base64Image = request.get("imageData");
            String mealId = request.get("mealId");

            if (base64Image == null) {
                return ResponseEntity.badRequest().build();
            }

            String imageUrl = manualDietService.uploadBase64MealImage(base64Image);
            return ResponseEntity.ok(new MealImageResponse(imageUrl));
        } catch (Exception e) {
            log.error("Błąd podczas przesyłania zdjęcia base64", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/meals/search")
    public ResponseEntity<List<MealSuggestionResponse>> searchMealSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<MealSuggestionResponse> suggestions = manualDietService.searchMealSuggestions(query, limit);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania sugestii posiłków", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/meals/templates/{templateId}")
    public ResponseEntity<MealTemplateResponse> updateMealTemplate(
            @PathVariable String templateId,
            @RequestBody SaveMealTemplateRequest request
    ) {
        try {
            MealTemplateResponse updatedTemplate = manualDietService.updateMealTemplate(templateId, request);
            return ResponseEntity.ok(updatedTemplate);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji szablonu posiłku: {}", templateId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
