package com.noisevisionsoftware.nutrilog.service.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.diet.ManualDietRequest;
import com.noisevisionsoftware.nutrilog.model.diet.DietFileInfo;
import com.noisevisionsoftware.nutrilog.service.RecipeService;
import com.noisevisionsoftware.nutrilog.service.category.ProductCategorizationService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualDietService {

    private final DietService dietService;
    private final DietManagerService dietManagerService;
    private final RecipeService recipeService;
    private final ProductCategorizationService categorizationService;

    public String saveManualDiet(ManualDietRequest request) {
        try {
            // Data validation
            validateRequest(request);

            // Conversion to ParsedDietData
            ParsedDietData parsedData = convertToParsedDietData(request);

            // Saving using existing system
            return dietManagerService.saveDietWithShoppingList(
                    parsedData,
                    request.getUserId(),
                    new DietFileInfo("Dieta ręczna", null)
            );
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania ręcznej diety", e);
            throw new RuntimeException("Nie udało się zapisać diety: " + e.getMessage());
        }
    }

    public List<ParsedProduct> searchIngredients(String query, int limit) {
        List<ParsedProduct> results = new ArrayList<>();

        String[] commonIngredients = {
                "mleko 3,2%", "jajka", "mąka pszenna", "masło", "cukier", "sól", "pieprz",
                "kurczak", "wołowina", "wieprzowina", "ryż", "makaron", "ziemniaki",
                "pomidory", "cebula", "czosnek", "marchew", "kapusta", "sałata"
        };

        return results;
    }

    public ParsedProduct createIngredient(ParsedProduct ingredient) {
        if (ingredient.getId() == null) {
            ingredient.setId(UUID.randomUUID().toString());
        }

    /*    // Suggesting category
        if (ingredient.getCategoryId() == null) {
            String categoryId = categorizationService.suggestCategory(ingredient);
            ingredient.setCategoryId(categoryId);
        }*/

        return ingredient;
    }

    public Map<String, Object> validateManualDiet(ManualDietRequest request) {
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            errors.add("ID użytkownika jest wymagane");
        }

        if (request.getDays() == null || request.getDays().isEmpty()) {
            errors.add("Dieta musi zawierać przynajmniej jeden dzień");
        }

        if (request.getMealsPerDay() <= 0 || request.getMealsPerDay() > 10) {
            errors.add("Liczba posiłków dziennie musi być między 1 a 10");
        }

        if (request.getDays() != null) {
            for (int i = 0; i < request.getDays().size(); i++) {
                ParsedDay day = request.getDays().get(i);
                if (day.getMeals() == null || day.getMeals().size() != request.getMealsPerDay()) {
                    errors.add("Dzień " + (i + 1) + " ma nieprawidłową liczbę posiłków");
                }

                if (day.getMeals() != null) {
                    for (int j = 0; j < day.getMeals().size(); j++) {
                        ParsedMeal meal = day.getMeals().get(j);
                        if (meal.getName() == null || meal.getName().trim().isEmpty()) {
                            errors.add("Posiłek " + (j + 1) + " w dniu " + (i + 1) + " musi mieć nazwę");
                        }
                    }
                }
            }
        }

        validation.put("isValid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);

        return validation;
    }

    private void validateRequest(ManualDietRequest request) {
        Map<String, Object> validation = validateManualDiet(request);
        if (!(Boolean) validation.get("isValid")) {
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) validation.get("errors");
            throw new IllegalArgumentException("Błędy walidacji: " + String.join(", ", errors));
        }
    }

    private ParsedDietData convertToParsedDietData(ManualDietRequest request) {
        ParsedDietData parsedData = new ParsedDietData();
        parsedData.setDays(request.getDays());
        parsedData.setMealsPerDay(request.getMealsPerDay());
        parsedData.setStartDate(Timestamp.of(
                java.sql.Timestamp.valueOf(LocalDate.parse(request.getStartDate()).atStartOfDay())
        ));
        parsedData.setDuration(request.getDuration());
        parsedData.setMealTimes(request.getMealTimes());

        // Generating shopping list using all ingredients
        List<String> shoppingList = new ArrayList<>();
        Map<String, List<String>> categorizedProducts = new HashMap<>();

        for (ParsedDay day : request.getDays()) {
            for (ParsedMeal meal : day.getMeals()) {
                if (meal.getIngredients() != null) {
                    for (ParsedProduct ingredient : meal.getIngredients()) {
                        shoppingList.add(ingredient.getOriginal());

                        String categoryId = ingredient.getCategoryId();
                        if (categoryId != null) {
                            categorizedProducts.computeIfAbsent(categoryId, k -> new ArrayList<>())
                                    .add(ingredient.getOriginal());
                        }
                    }
                }
            }
        }

        parsedData.setShoppingList(shoppingList);
        parsedData.setCategorizedProducts(categorizedProducts);

        return parsedData;
    }
}
