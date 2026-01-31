package com.noisevisionsoftware.vitema.service.diet;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.vitema.mapper.diet.FirestoreDietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.diet.DietFileInfo;
import com.noisevisionsoftware.vitema.model.diet.DietMetadata;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.service.ProductParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietManagerService {

    private final Firestore firestore;
    private final ProductParsingService productParsingService;
    private final FirestoreDietMapper firestoreMapper;
    private final DietService dietService;
    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;

    public String saveDietWithShoppingList(
            ParsedDietData parsedData,
            String userId,
            DietFileInfo fileInfo
    ) {
        try {
            // 1. Create new diet document
            DocumentReference dietDocRef = firestore.collection("diets").document();
            Timestamp now = Timestamp.now();

            Diet diet = Diet.builder()
                    .userId(userId)
                    .createdAt(now)
                    .updatedAt(now)
                    .days(new ArrayList<>())
                    .metadata(DietMetadata.builder()
                            .totalDays(parsedData.getDays().size())
                            .fileName(fileInfo.getFileName())
                            .fileUrl(fileInfo.getFileUrl())
                            .build())
                    .build();

            Map<String, Object> dietData = firestoreMapper.toFirestoreMap(diet);
            dietDocRef.set(dietData).get();

            // 2. Save recipes and collect their IDs
            Map<String, String> savedRecipeIds = saveRecipes(parsedData, userId, dietDocRef.getId());

            // 3. Update diet days
            List<Map<String, Object>> updatedDays = createDaysWithMeals(parsedData, savedRecipeIds);
            dietDocRef.update("days", updatedDays).get();

            // 4. Process and save shopping list
            saveShoppingList(parsedData, userId, dietDocRef.getId());

            recipeService.refreshRecipesCache();

            dietService.refreshDietsCache();

            return dietDocRef.getId();
        } catch (Exception e) {
            log.error("Error saving diet", e);
            throw new RuntimeException("Failed to save diet", e);
        }
    }

    protected Map<String, String> saveRecipes(ParsedDietData parsedData, String userId, String dietId) {
        Map<String, String> savedRecipeIds = new HashMap<>();
        Timestamp now = Timestamp.now();

        for (int dayIndex = 0; dayIndex < parsedData.getDays().size(); dayIndex++) {
            ParsedDay day = parsedData.getDays().get(dayIndex);

            for (ParsedMeal meal : day.getMeals()) {

                // Tworzenie obiektu Recipe
                Recipe recipe = Recipe.builder()
                        .name(meal.getName())
                        .instructions(meal.getInstructions())
                        .nutritionalValues(meal.getNutritionalValues())
                        .createdAt(now)
                        .photos(meal.getPhotos() != null ? meal.getPhotos() : new ArrayList<>())
                        .ingredients(convertToRecipeIngredients(meal.getIngredients()))
                        .parentRecipeId(null)
                        .build();

                // Zapisywanie przez serwis
                Recipe savedRecipe = recipeService.findOrCreateRecipe(recipe);

                // Tworzenie referencji
                RecipeReference recipeRef = RecipeReference.builder()
                        .recipeId(savedRecipe.getId())
                        .dietId(dietId)
                        .userId(userId)
                        .mealType(meal.getMealType())
                        .addedAt(now)
                        .build();

                // Zapisujemy referencję przez odpowiednie repo
                recipeRepository.saveReference(recipeRef);

                savedRecipeIds.put(dayIndex + "_" + meal.getMealType().name(), savedRecipe.getId());
            }
        }
        return savedRecipeIds;
    }

    protected List<Map<String, Object>> createDaysWithMeals(
            ParsedDietData parsedData,
            Map<String, String> savedRecipeIds
    ) {
        List<Map<String, Object>> updatedDays = new ArrayList<>();

        for (int dayIndex = 0; dayIndex < parsedData.getDays().size(); dayIndex++) {
            ParsedDay day = parsedData.getDays().get(dayIndex);

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", day.getDate());

            List<Map<String, Object>> meals = new ArrayList<>();
            for (ParsedMeal meal : day.getMeals()) {
                Map<String, Object> mealMap = new HashMap<>();
                mealMap.put("recipeId", savedRecipeIds.get(dayIndex + "_" + meal.getMealType().name()));
                mealMap.put("mealType", meal.getMealType().name());
                mealMap.put("time", meal.getTime());
                meals.add(mealMap);
            }

            dayMap.put("meals", meals);
            updatedDays.add(dayMap);
        }

        return updatedDays;
    }

    protected void saveShoppingList(ParsedDietData parsedData, String userId, String dietId) throws Exception {
        Map<String, List<Map<String, Object>>> items = new HashMap<>();

        if (parsedData.getCategorizedProducts() != null) {
            for (Map.Entry<String, List<String>> entry : parsedData.getCategorizedProducts().entrySet()) {
                String categoryId = entry.getKey();
                List<Map<String, Object>> categoryItems = parseProductStrings(entry.getValue(), categoryId);

                if (!categoryItems.isEmpty()) {
                    items.put(categoryId, categoryItems);
                }
            }
        }

        Map<String, Object> shoppingList = new HashMap<>();
        shoppingList.put("dietId", dietId);
        shoppingList.put("userId", userId);
        shoppingList.put("items", items);
        shoppingList.put("createdAt", Timestamp.now());

        if (parsedData.getDays() != null && !parsedData.getDays().isEmpty()) {
            shoppingList.put("startDate", parsedData.getDays().getFirst().getDate());
            shoppingList.put("endDate", parsedData.getDays().getLast().getDate());
        }

        shoppingList.put("version", 3);

        try {
            firestore.collection("shopping_lists").add(shoppingList).get();
        } catch (Exception e) {
            log.error("Error saving shopping list. Data: {}", shoppingList, e);
            throw e;
        }
    }

    protected List<Map<String, Object>> parseProductStrings(List<String> productStrings, String categoryId) {
        List<Map<String, Object>> categoryItems = new ArrayList<>();

        for (String productString : productStrings) {
            Map<String, Object> itemMap = new HashMap<>();

            ParsedProduct product = productParsingService.parseProduct(productString).getProduct();

            if (product != null) {
                itemMap.put("name", product.getName());
                itemMap.put("quantity", product.getQuantity());
                itemMap.put("unit", product.getUnit());
                itemMap.put("original", productString);
                itemMap.put("hasCustomUnit", product.isHasCustomUnit());
                itemMap.put("categoryId", categoryId);
                if (product.getId() != null) {
                    itemMap.put("id", product.getId());
                }
            } else {
                itemMap.put("name", productString);
                itemMap.put("quantity", 1.0);
                itemMap.put("unit", "szt");
                itemMap.put("original", productString);
                itemMap.put("hasCustomUnit", false);
                itemMap.put("categoryId", categoryId);
            }

            categoryItems.add(itemMap);
        }
        return categoryItems;
    }

    protected List<RecipeIngredient> convertToRecipeIngredients(List<ParsedProduct> parsedProducts) {
        if (parsedProducts == null) return new ArrayList<>();

        return parsedProducts.stream()
                .map(product -> RecipeIngredient.builder()
                        .id(UUID.randomUUID().toString())
                        .name(product.getName())
                        .quantity(product.getQuantity())
                        .unit(product.getUnit())
                        .original(product.getOriginal())
                        .categoryId(product.getCategoryId())
                        .hasCustomUnit(product.isHasCustomUnit())
                        .build())
                .collect(Collectors.toList());
    }
}