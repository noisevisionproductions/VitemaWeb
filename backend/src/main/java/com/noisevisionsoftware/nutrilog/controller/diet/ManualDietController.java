package com.noisevisionsoftware.nutrilog.controller.diet;

import com.noisevisionsoftware.nutrilog.dto.request.diet.ManualDietRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.ManualDietResponse;
import com.noisevisionsoftware.nutrilog.service.diet.ManualDietService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diets/manual")
@RequiredArgsConstructor
@Slf4j
public class ManualDietController {

    private final ManualDietService manualDietService;

    @PostMapping("/save")
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
    public ResponseEntity<List<ParsedProduct>> searchIngredients(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<ParsedProduct> ingredients = manualDietService.searchIngredients(query, limit);
            return ResponseEntity.ok(ingredients);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania składników", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/ingredients")
    public ResponseEntity<ParsedProduct> createIngredient(@RequestBody ParsedProduct ingredient) {
        try {
            ParsedProduct savedIngredient = manualDietService.createIngredient(ingredient);
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
}
