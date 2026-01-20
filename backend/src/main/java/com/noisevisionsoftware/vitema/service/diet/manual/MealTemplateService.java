package com.noisevisionsoftware.vitema.service.diet.manual;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.meal.MealIngredient;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.repository.meal.MealTemplateRepository;
import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealTemplateService {

    private final MealTemplateRepository mealTemplateRepository;
    private final ProductCategorizationService categorizationService;

    private static final String MEAL_TEMPLATES_CACHE = "mealTemplatesCache";
    private static final String MEAL_SEARCH_CACHE = "mealSearchCache";

    @Cacheable(value = MEAL_TEMPLATES_CACHE, key = "#id")
    public MealTemplate getById(String id) {
        return mealTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Meal template not found with id: " + id));
    }

    public List<MealTemplate> searchAccessibleTemplates(String query, String userId, int limit) {
        try {
            return mealTemplateRepository.searchAccessibleTemplates(query, userId, limit);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania dostępnych szablonów", e);
            return new ArrayList<>();
        }
    }

    @Cacheable(value = MEAL_SEARCH_CACHE, key = "{#query, #limit}")
    public List<MealTemplate> searchTemplates(String query, int limit) {
        try {
            return mealTemplateRepository.searchByName(query, limit);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania szablonów posiłków", e);
            return new ArrayList<>();
        }
    }

    @CacheEvict(value = {MEAL_TEMPLATES_CACHE, MEAL_SEARCH_CACHE}, allEntries = true)
    @Transactional
    public MealTemplate save(MealTemplate template) {
        try {
            if (template.getCreatedAt() == null) {
                template.setCreatedAt(Timestamp.now());
            }
            template.setUpdatedAt(Timestamp.now());

            if (template.getIngredients() != null) {
                for (MealIngredient ingredient : template.getIngredients()) {
                    if (ingredient.getCategoryId() == null) {
                        ParsedProduct product = ParsedProduct.builder()
                                .name(ingredient.getName())
                                .original(ingredient.getOriginal())
                                .build();
                        String categoryId = categorizationService.suggestCategory(product);
                        ingredient.setCategoryId(categoryId);
                    }
                }
            }

            MealTemplate saved = mealTemplateRepository.save(template);

            if (saved.getId() != null) {
                incrementUsageCount(saved.getId());
            }

            return saved;
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania szablonu posiłku", e);
            throw new RuntimeException("Nie udało się zapisać szablonu posiłku", e);
        }
    }

    @CacheEvict(value = MEAL_TEMPLATES_CACHE, key = "#id")
    @Transactional
    public void incrementUsageCount(String id) {
        try {
            mealTemplateRepository.incrementUsageCount(id);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji licznika użycia dla szablonu: {}", id, e);
        }
    }

    public List<MealTemplate> getPopularTemplates(int limit) {
        return mealTemplateRepository.findTopByUsageCount(limit);
    }

    public List<MealTemplate> getRecentTemplates(int limit) {
        return mealTemplateRepository.findRecentlyUsed(limit);
    }

    @CacheEvict(value = {MEAL_TEMPLATES_CACHE, MEAL_SEARCH_CACHE}, allEntries = true)
    public void refreshCache() {
        log.debug("Odświeżenie cache szablonów posiłków");
    }
}
