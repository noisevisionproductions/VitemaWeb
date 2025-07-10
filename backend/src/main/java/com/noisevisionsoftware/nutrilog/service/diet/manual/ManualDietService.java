package com.noisevisionsoftware.nutrilog.service.diet.manual;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.manual.PreviewMealSaveRequest;
import com.noisevisionsoftware.nutrilog.dto.request.diet.manual.SaveMealTemplateRequest;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.MealSavePreviewResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.MealSuggestionResponse;
import com.noisevisionsoftware.nutrilog.dto.response.diet.manual.MealTemplateResponse;
import com.noisevisionsoftware.nutrilog.model.diet.DietFileInfo;
import com.noisevisionsoftware.nutrilog.model.meal.MealTemplate;
import com.noisevisionsoftware.nutrilog.model.recipe.Recipe;
import com.noisevisionsoftware.nutrilog.service.RecipeService;
import com.noisevisionsoftware.nutrilog.service.diet.DietManagerService;
import com.noisevisionsoftware.nutrilog.utils.MealTemplateConverter;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Główny service do zarządzania dietami ręcznymi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManualDietService {

    // Główne zależności
    private final DietManagerService dietManagerService;
    private final RecipeService recipeService;
    private final MealTemplateService mealTemplateService;

    // Wyspecjalizowane serwisy
    private final MealSuggestionService mealSuggestionService;
    private final IngredientManagementService ingredientManagementService;
    private final DietValidationService dietValidationService;
    private final DietDataConverter dietDataConverter;

    // Konwertery
    private final MealTemplateConverter mealTemplateConverter;

    /**
     * Zapisuje ręczną dietę w systemie
     */
    public String saveManualDiet(ManualDietRequest request) {
        try {
            log.info("Rozpoczynanie zapisywania ręcznej diety dla użytkownika: {}", request.getUserId());

            // Walidacja danych
            validateRequest(request);

            // Konwersja do ParsedDietData
            ParsedDietData parsedData = dietDataConverter.convertToParsedDietData(request);

            // Zapisywanie przy użyciu istniejącego systemu
            String dietId = dietManagerService.saveDietWithShoppingList(
                    parsedData,
                    request.getUserId(),
                    new DietFileInfo("Dieta ręczna", null)
            );

            log.info("Pomyślnie zapisano ręczną dietę z ID: {}", dietId);
            return dietId;

        } catch (Exception e) {
            log.error("Błąd podczas zapisywania ręcznej diety dla użytkownika: {}", request.getUserId(), e);
            throw new RuntimeException("Nie udało się zapisać diety: " + e.getMessage());
        }
    }

    /*
     * Aktualizuje istniejący szablon posiłku
     * */
    @Transactional
    public MealTemplateResponse updateMealTemplate(String templateId, SaveMealTemplateRequest request) {
        try {
            log.info("Aktualizacja szablonu {} - zdjęcia: {}", templateId,
                    request.getPhotos() != null ? request.getPhotos().size() : 0);
            if (templateId.startsWith("recipe-")) {
                String recipeId = templateId.replace("recipe-", "");
                Recipe updatedRecipe = Recipe.builder()
                        .id(recipeId)
                        .name(request.getName())
                        .instructions(request.getInstructions())
                        .nutritionalValues(mealTemplateConverter.convertNutritionalValuesFromRequest(request.getNutritionalValues()))
                        .photos(request.getPhotos() != null ? request.getPhotos() : new ArrayList<>())
                        .build();

                Recipe savedRecipe = recipeService.updateRecipe(recipeId, updatedRecipe);
                return mealTemplateConverter.convertRecipeToTemplate(savedRecipe);
            } else {
                MealTemplate existingTemplate = mealTemplateService.getById(templateId);

                // Aktualizuj tylko zmienione pola
                MealTemplate updatedTemplate = MealTemplate.builder()
                        .id(templateId)
                        .name(request.getName())
                        .instructions(request.getInstructions())
                        .nutritionalValues(mealTemplateConverter.convertNutritionalValuesFromRequest(request.getNutritionalValues()))
                        .photos(request.getPhotos() != null ? request.getPhotos() : new ArrayList<>())
                        .ingredients(mealTemplateConverter.convertIngredientsFromRequest(request.getIngredients()))
                        .mealType(request.getMealType())
                        .category(request.getCategory())
                        .createdBy(existingTemplate.getCreatedBy())
                        .createdAt(existingTemplate.getCreatedAt())
                        .updatedAt(Timestamp.now())
                        .usageCount(existingTemplate.getUsageCount())
                        .build();

                MealTemplate savedTemplate = mealTemplateService.save(updatedTemplate);
                return mealTemplateConverter.convertTemplateToResponse(savedTemplate);
            }
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji szablonu posiłku: {}", templateId, e);
            throw new RuntimeException("Nie udało się zaktualizować szablonu posiłku");
        }
    }

    /**
     * Wyszukuje sugestie posiłków
     */
    public List<MealSuggestionResponse> searchMealSuggestions(String query, int limit) {
        String userId = getCurrentUserId();
        return mealSuggestionService.searchMealSuggestions(query, limit, userId);
    }

    /**
     * Pobiera szablon posiłku
     */
    public MealTemplateResponse getMealTemplate(String id) {
        try {
            if (id.startsWith("recipe-")) {
                String recipeId = id.replace("recipe-", "");
                var recipe = recipeService.getRecipeById(recipeId);
                return mealTemplateConverter.convertRecipeToTemplate(recipe);
            } else {
                MealTemplate template = mealTemplateService.getById(id);
                return mealTemplateConverter.convertTemplateToResponse(template);
            }
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonu posiłku: {}", id, e);
            throw new RuntimeException("Nie znaleziono szablonu posiłku");
        }
    }

    /**
     * Podgląd zapisywania posiłku
     */
    public MealSavePreviewResponse previewMealSave(PreviewMealSaveRequest request) {
        try {
            String userId = getCurrentUserId();
            String query = request.getName().trim();

            // Wyszukaj podobne posiłki
            List<MealSuggestionResponse> similarMeals = mealSuggestionService.findSimilarMeals(query, 5, userId);
            boolean foundExact = mealSuggestionService.existsExactMeal(query, userId);
            List<MealSuggestionResponse> highlySimilar = mealSuggestionService.findHighlySimilarMeals(query, userId);

            // Określ rekomendowaną akcję
            String recommendedAction;
            String message;

            if (foundExact) {
                recommendedAction = "USE_EXISTING";
                message = "Znaleziono posiłek o takiej samej nazwie. Czy chcesz użyć istniejącego?";
            } else if (!highlySimilar.isEmpty()) {
                recommendedAction = "UPDATE_EXISTING";
                message = "Znaleziono podobne posiłki. Czy chcesz zaktualizować istniejący?";
            } else {
                recommendedAction = "CREATE_NEW";
                message = "Nie znaleziono podobnych posiłków. Zostanie utworzony nowy szablon.";
            }

            return MealSavePreviewResponse.builder()
                    .willCreateNew(!foundExact)
                    .foundSimilar(!highlySimilar.isEmpty())
                    .similarMeals(highlySimilar)
                    .recommendedAction(recommendedAction)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("Błąd podczas podglądu zapisywania posiłku", e);
            throw new RuntimeException("Błąd podczas sprawdzania podobnych posiłków");
        }
    }

    /**
     * Zapisuje szablon posiłku
     */
    @Transactional
    public MealTemplateResponse saveMealTemplate(SaveMealTemplateRequest request) {
        try {
            if (!request.isShouldSave()) {
                return MealTemplateResponse.builder()
                        .name(request.getName())
                        .instructions(request.getInstructions())
                        .build();
            }

            // Konwertuj żądanie na szablon
            MealTemplate template = mealTemplateConverter.convertRequestToTemplate(request);

            // Zapisz szablon
            MealTemplate savedTemplate = mealTemplateService.save(template);

            // Zwiększ licznik użyć
            mealTemplateService.incrementUsageCount(savedTemplate.getId());

            return mealTemplateConverter.convertTemplateToResponse(savedTemplate);

        } catch (Exception e) {
            log.error("Błąd podczas zapisywania szablonu posiłku", e);
            throw new RuntimeException("Nie udało się zapisać szablonu posiłku");
        }
    }

    /**
     * Wyszukuje składniki
     */
    public List<ParsedProduct> searchIngredients(String query, int limit) {
        return ingredientManagementService.searchIngredients(query, limit);
    }

    /**
     * Tworzy nowy składnik
     */
    public ParsedProduct createIngredient(ParsedProduct ingredient) {
        return ingredientManagementService.createIngredient(ingredient);
    }

    /**
     * Waliduje ręczną dietę
     */
    public Map<String, Object> validateManualDiet(ManualDietRequest request) {
        return dietValidationService.validateManualDiet(request);
    }

    /**
     * Przesyła zdjęcie posiłku
     */
    public String uploadMealImage(MultipartFile image, String mealId) {
        try {
            if (mealId != null && !mealId.isEmpty()) {
                return recipeService.uploadImage(mealId, image);
            } else {
                // Dla tymczasowych zdjęć używaj base64
                byte[] imageBytes = image.getBytes();
                String base64Image = "data:" + image.getContentType() + ";base64," +
                        Base64.getEncoder().encodeToString(imageBytes);
                return recipeService.uploadBase64Image(base64Image);
            }
        } catch (Exception e) {
            log.error("Błąd podczas przesyłania zdjęcia posiłku", e);
            throw new RuntimeException("Nie udało się przesłać zdjęcia");
        }
    }

    /**
     * Przesyła zdjęcie base64 posiłku
     */
    public String uploadBase64MealImage(String base64Image, String mealId) {
        try {
            return recipeService.uploadBase64Image(base64Image);
        } catch (Exception e) {
            log.error("Błąd podczas przesyłania zdjęcia base64", e);
            throw new RuntimeException("Nie udało się przesłać zdjęcia");
        }
    }

    private void validateRequest(ManualDietRequest request) {
        Map<String, Object> validation = dietValidationService.validateManualDiet(request);
        if (!(Boolean) validation.get("isValid")) {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) validation.get("errors");
            throw new IllegalArgumentException("Błędy walidacji: " + String.join(", ", errors));
        }
    }

    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof String) {
                return (String) authentication.getPrincipal();
            }
            return null;
        } catch (Exception e) {
            log.warn("Nie udało się pobrać ID użytkownika", e);
            return null;
        }
    }
}