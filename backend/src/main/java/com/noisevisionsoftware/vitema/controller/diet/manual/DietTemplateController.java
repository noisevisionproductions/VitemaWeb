package com.noisevisionsoftware.vitema.controller.diet.manual;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.DietTemplateRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.DietTemplateResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.DietTemplateStatsResponse;
import com.noisevisionsoftware.vitema.service.diet.manual.dietTemplate.DietTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diet-templates")
@RequiredArgsConstructor
@Slf4j
public class DietTemplateController {

    private final DietTemplateService dietTemplateService;

    @GetMapping
    public ResponseEntity<List<DietTemplateResponse>> getAllTemplates(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<DietTemplateResponse> templates = dietTemplateService.getAllTemplatesForUser(userId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonów diet", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DietTemplateResponse> getTemplate(@PathVariable String id) {
        try {
            DietTemplateResponse template = dietTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonu: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<DietTemplateResponse>> getTemplatesByCategory(
            @PathVariable String category,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            List<DietTemplateResponse> templates = dietTemplateService.getTemplatesByCategory(category, userId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonów dla kategorii: {}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<DietTemplateResponse>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();
            List<DietTemplateResponse> templates = dietTemplateService.getMostUsedTemplates(userId, limit);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania popularnych szablonów", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DietTemplateResponse>> searchTemplates(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit,
            Authentication auth) {
        try {
            String userId = auth.getName();
            List<DietTemplateResponse> templates = dietTemplateService.searchTemplates(query, userId, limit);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania szablonów", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<DietTemplateResponse> createTemplate(
            @RequestBody DietTemplateRequest request,
            Authentication auth) {
        try {
            String userId = auth.getName();
            DietTemplateResponse template = dietTemplateService.createTemplate(request, userId);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Błąd podczas tworzenia szablonu", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/from-diet")
    public ResponseEntity<DietTemplateResponse> createTemplateFromDiet(
            @RequestBody DietTemplateRequest request,
            Authentication auth) {
        try {
            String userId = auth.getName();
            DietTemplateResponse template = dietTemplateService.createTemplateFromManualDiet(request, userId);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Błąd podczas tworzenia szablonu z diety", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DietTemplateResponse> updateTemplate(
            @PathVariable String id,
            @RequestBody DietTemplateRequest request,
            Authentication auth) {
        try {
            String userId = auth.getName();
            DietTemplateResponse template = dietTemplateService.updateTemplate(id, request, userId);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji szablonu: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id, Authentication auth) {
        try {
            String userId = auth.getName();
            dietTemplateService.deleteTemplate(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Błąd podczas usuwania szablonu: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<Void> incrementUsage(@PathVariable String id) {
        try {
            dietTemplateService.incrementUsageCount(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji licznika użycia: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<DietTemplateStatsResponse> getTemplateStats(Authentication auth) {
        try {
            String userId = auth.getName();
            DietTemplateStatsResponse stats = dietTemplateService.getTemplateStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania statystyk szablonów", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}