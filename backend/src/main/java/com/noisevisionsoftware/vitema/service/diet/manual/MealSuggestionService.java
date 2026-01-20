package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.dto.response.diet.manual.MealSuggestionResponse;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.utils.MealTemplateConverter;
import com.noisevisionsoftware.vitema.utils.SimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service odpowiedzialny za wyszukiwanie i konwersję sugestii posiłków
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MealSuggestionService {

    private final RecipeService recipeService;
    private final MealTemplateService mealTemplateService;
    private final MealTemplateConverter mealTemplateConverter;
    private final SimilarityCalculator similarityCalculator;

    /**
     * Wyszukuje sugestie posiłków na podstawie zapytania
     */
    @Cacheable(value = "mealSuggestionCache", key = "{#query, #limit, #userId}")
    public List<MealSuggestionResponse> searchMealSuggestions(String query, int limit, String userId) {
        List<MealSuggestionResponse> suggestions = new ArrayList<>();

        try {
            // Walidacja parametrów wejściowych
            if (query == null || query.trim().isEmpty()) {
                log.warn("Puste zapytanie wyszukiwania");
                return suggestions;
            }

            if (limit <= 0) {
                log.warn("Nieprawidłowy limit: {}, używam domyślnej wartości 10", limit);
                limit = 10;
            }

            int recipeLimit = Math.max(1, limit / 2);
            int templateLimit = Math.max(1, limit / 2);

            // Wyszukuj przepisy
            List<Recipe> recipes = recipeService.searchRecipes(query);
            suggestions.addAll(convertRecipesToSuggestions(recipes, query, recipeLimit));

            // Wyszukuj szablony posiłków
            List<MealTemplate> templates = mealTemplateService.searchAccessibleTemplates(query, userId, templateLimit);
            suggestions.addAll(convertTemplatesToSuggestions(templates, query));

            // Sortuj wyniki według trafności
            suggestions.sort((a, b) -> {
                if (a.isExact() && !b.isExact()) return -1;
                if (!a.isExact() && b.isExact()) return 1;
                return Double.compare(b.getSimilarity(), a.getSimilarity());
            });

            return suggestions.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania sugestii posiłków", e);
            return new ArrayList<>();
        }
    }

    /**
     * Wyszukuje podobne posiłki dla podglądu zapisywania
     */
    public List<MealSuggestionResponse> findSimilarMeals(String mealName, int limit, String userId) {
        return searchMealSuggestions(mealName.trim(), limit, userId);
    }

    /**
     * Sprawdza, czy istnieje posiłek o dokładnie takiej nazwie
     */
    public boolean existsExactMeal(String mealName, String userId) {
        List<MealSuggestionResponse> suggestions = searchMealSuggestions(mealName.trim(), 5, userId);
        return suggestions.stream()
                .anyMatch(meal -> meal.getName().equalsIgnoreCase(mealName.trim()));
    }

    /**
     * Znajduje bardzo podobne posiłki (podobieństwo > 0.8)
     */
    public List<MealSuggestionResponse> findHighlySimilarMeals(String mealName, String userId) {
        List<MealSuggestionResponse> suggestions = searchMealSuggestions(mealName.trim(), 10, userId);
        return suggestions.stream()
                .filter(meal -> meal.getSimilarity() > 0.8)
                .collect(Collectors.toList());
    }


    private List<MealSuggestionResponse> convertRecipesToSuggestions(List<Recipe> recipes, String query, int limit) {
        return recipes.stream()
                .limit(limit)
                .map(recipe -> {
                    double similarity = similarityCalculator.calculateSimilarity(query, recipe.getName());
                    boolean isExact = recipe.getName().equalsIgnoreCase(query.trim());

                    return MealSuggestionResponse.builder()
                            .id("recipe-" + recipe.getId())
                            .name(recipe.getName())
                            .instructions(recipe.getInstructions())
                            .nutritionalValues(mealTemplateConverter.convertNutritionalValuesToResponse(recipe.getNutritionalValues()))
                            .photos(recipe.getPhotos() != null ? recipe.getPhotos() : new ArrayList<>())
                            .ingredients(new ArrayList<>())
                            .similarity(similarity)
                            .isExact(isExact)
                            .source("RECIPE")
                            .usageCount(0)
                            .lastUsed(recipe.getCreatedAt() != null ? recipe.getCreatedAt().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<MealSuggestionResponse> convertTemplatesToSuggestions(List<MealTemplate> templates, String query) {
        return templates.stream()
                .map(template -> {
                    double similarity = similarityCalculator.calculateSimilarity(query, template.getName());
                    boolean isExact = template.getName().equalsIgnoreCase(query.trim());

                    return MealSuggestionResponse.builder()
                            .id(template.getId())
                            .name(template.getName())
                            .instructions(template.getInstructions())
                            .nutritionalValues(mealTemplateConverter.convertNutritionalValuesToResponse(template.getNutritionalValues()))
                            .photos(template.getPhotos() != null ? template.getPhotos() : new ArrayList<>())
                            .ingredients(mealTemplateConverter.convertIngredientsToSuggestions(template.getIngredients()))
                            .similarity(similarity)
                            .isExact(isExact)
                            .source("TEMPLATE")
                            .usageCount(template.getUsageCount())
                            .lastUsed(template.getLastUsed() != null ? template.getLastUsed().toString() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }
}