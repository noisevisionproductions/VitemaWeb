package com.noisevisionsoftware.vitema.service.diet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.vitema.dto.diet.DietDayDto;
import com.noisevisionsoftware.vitema.mapper.diet.FirestoreDietMapper;
import com.noisevisionsoftware.vitema.model.diet.*;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.model.shopping.CategorizedShoppingListItem;
import com.noisevisionsoftware.vitema.model.shopping.ShoppingList;
import com.noisevisionsoftware.vitema.repository.ShoppingListRepository;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.service.shoppingList.ShoppingListGeneratorService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietCommandService {

    private final Firestore firestore;
    private final FirestoreDietMapper firestoreMapper;
    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;
    private final ShoppingListGeneratorService shoppingListGenerator;
    private final ShoppingListRepository shoppingListRepository;

    public String saveDietWithShoppingList(
            ParsedDietData parsedData,
            String userId,
            String authorId,
            DietFileInfo fileInfo
    ) {
        try {
            // 1. Create Diet Object
            DocumentReference dietDocRef = firestore.collection("diets").document();
            Timestamp now = Timestamp.now();

            Diet diet = Diet.builder()
                    .userId(userId)
                    .authorId(authorId)
                    .createdAt(now)
                    .updatedAt(now)
                    .days(new ArrayList<>())
                    .metadata(DietMetadata.builder()
                            .totalDays(parsedData.getDays().size())
                            .fileName(fileInfo.getFileName())
                            .fileUrl(fileInfo.getFileUrl())
                            .build())
                    .build();

            // 2. Save Recipes & Structure
            Map<String, String> savedRecipeIds = saveRecipes(parsedData, userId, dietDocRef.getId());
            List<Map<String, Object>> updatedDaysMap = createDaysWithMealsMap(parsedData, savedRecipeIds);

            // 3. Save Diet to Firestore
            Map<String, Object> dietData = firestoreMapper.toFirestoreMap(diet);
            dietData.put("days", updatedDaysMap);
            dietDocRef.set(dietData).get();

            // 4. IMPORTANT: Update the 'diet' object with the generated days so the Generator can use it
            diet.setDays(convertParsedDaysToModelDays(parsedData, savedRecipeIds));

            // 5. Generate and Save Shopping List using the NEW Service
            saveShoppingList(diet, userId, dietDocRef.getId());

            // Cache refresh
            recipeService.refreshRecipesCache();

            return dietDocRef.getId();
        } catch (Exception e) {
            log.error("Error saving diet", e);
            throw new RuntimeException("Failed to save diet", e);
        }
    }

    public void updateDietStructure(String dietId, List<DietDayDto> daysFromFrontend) {
        try {
            DocumentReference dietRef = firestore.collection("diets").document(dietId);

            List<Map<String, Object>> mappedDays = daysFromFrontend.stream()
                    .map(day -> {
                        Map<String, Object> dayMap = new HashMap<>();
                        dayMap.put("date", day.getDate());

                        List<Map<String, Object>> mealsList = day.getMeals().stream()
                                .map(mealDto -> {
                                    Map<String, Object> mealMap = new HashMap<>();
                                    mealMap.put("name", mealDto.getName());
                                    mealMap.put("mealType", mealDto.getMealType());
                                    mealMap.put("time", mealDto.getTime());
                                    mealMap.put("instructions", mealDto.getInstructions());
                                    mealMap.put("originalRecipeId", mealDto.getOriginalRecipeId());

                                    List<Map<String, Object>> ingredientsMap = mealDto.getIngredients().stream()
                                            .map(ing -> {
                                                Map<String, Object> iMap = new HashMap<>();
                                                iMap.put("name", ing.getName());
                                                iMap.put("quantity", ing.getQuantity());
                                                iMap.put("unit", ing.getUnit());
                                                iMap.put("productId", ing.getProductId());
                                                iMap.put("categoryId", ing.getCategoryId());
                                                return iMap;
                                            }).collect(Collectors.toList());

                                    mealMap.put("ingredients", ingredientsMap);

                                    if (mealDto.getNutritionalValues() != null) {
                                        mealMap.put("nutritionalValues", objectMapper.convertValue(mealDto.getNutritionalValues(), Map.class));
                                    }

                                    return mealMap;
                                }).collect(Collectors.toList());

                        dayMap.put("meals", mealsList);
                        return dayMap;
                    }).collect(Collectors.toList());

            dietRef.update("days", mappedDays).get();
            log.info("Pomyślnie zaktualizowano strukturę diety: {}", dietId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Wątek przerwany podczas aktualizacji diety: {}", dietId, e);
            throw new RuntimeException("Operacja została przerwana", e);
        } catch (ExecutionException e) {
            log.error("Błąd wykonania update w Firestore dla diety: {}", dietId, e);
            throw new RuntimeException("Błąd bazy danych podczas aktualizacji diety", e);
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas aktualizacji diety: {}", dietId, e);
            throw new RuntimeException("Nie udało się zaktualizować diety", e);
        }
    }

    private void saveShoppingList(Diet diet, String userId, String dietId) {
        try {
            Map<String, List<CategorizedShoppingListItem>> items =
                    shoppingListGenerator.generateItemsFromDiet(diet);

            Timestamp startDate = null;
            Timestamp endDate = null;

            if (diet.getDays() != null && !diet.getDays().isEmpty()) {
                startDate = diet.getDays().getFirst().getDate();
                endDate = diet.getDays().getLast().getDate();
            }

            ShoppingList shoppingList = ShoppingList.builder()
                    .dietId(dietId)
                    .userId(userId)
                    .items(items)
                    .createdAt(Timestamp.now())
                    .startDate(startDate)
                    .endDate(endDate)
                    .version(4)
                    .build();

            shoppingListRepository.save(shoppingList);
        } catch (Exception e) {
            log.error("Error generating shopping list", e);
            throw new RuntimeException("Shopping list generation failed", e);
        }
    }

    private List<Map<String, Object>> createDaysWithMealsMap(
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
                mealMap.put("originalRecipeId", savedRecipeIds.get(dayIndex + "_" + meal.getMealType().name()));
                mealMap.put("name", meal.getName());
                mealMap.put("mealType", meal.getMealType().name());
                mealMap.put("time", meal.getTime());
                meals.add(mealMap);
            }
            dayMap.put("meals", meals);
            updatedDays.add(dayMap);
        }
        return updatedDays;
    }

    protected Map<String, String> saveRecipes(ParsedDietData parsedData, String userId, String dietId) {
        Map<String, String> savedRecipeIds = new HashMap<>();
        Timestamp now = Timestamp.now();

        for (int dayIndex = 0; dayIndex < parsedData.getDays().size(); dayIndex++) {
            ParsedDay day = parsedData.getDays().get(dayIndex);

            for (ParsedMeal meal : day.getMeals()) {
                Recipe recipe = Recipe.builder()
                        .name(meal.getName())
                        .instructions(meal.getInstructions())
                        .nutritionalValues(meal.getNutritionalValues())
                        .createdAt(now)
                        .photos(meal.getPhotos() != null ? meal.getPhotos() : new ArrayList<>())
                        .ingredients(convertToRecipeIngredients(meal.getIngredients()))
                        .parentRecipeId(null)
                        .build();

                Recipe savedRecipe = recipeService.findOrCreateRecipe(recipe);

                RecipeReference recipeRef = RecipeReference.builder()
                        .recipeId(savedRecipe.getId())
                        .dietId(dietId)
                        .userId(userId)
                        .mealType(meal.getMealType())
                        .addedAt(now)
                        .build();

                recipeRepository.saveReference(recipeRef);
                savedRecipeIds.put(dayIndex + "_" + meal.getMealType().name(), savedRecipe.getId());
            }
        }
        return savedRecipeIds;
    }

    protected List<RecipeIngredient> convertToRecipeIngredients(List<ParsedProduct> parsedProducts) {
        if (parsedProducts == null) return new ArrayList<>();
        return parsedProducts.stream()
                .map(product -> {
                    Long productId = null;
                    if (product.getId() != null) {
                        try {
                            productId = Long.parseLong(product.getId());
                        } catch (NumberFormatException ignored) {
                            // If ID is not a number (e.g. UUID), we leave productId as null
                        }
                    }

                    return RecipeIngredient.builder()
                            .id(product.getId() != null ? product.getId() : UUID.randomUUID().toString())
                            .name(product.getName())
                            .quantity(product.getQuantity())
                            .unit(product.getUnit())
                            .original(product.getOriginal())
                            .categoryId(product.getCategoryId())
                            .productId(productId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Converts the DTO-like ParsedDietData into the rich Domain Model (Diet -> Day -> DayMeal).
     */
    private List<Day> convertParsedDaysToModelDays(ParsedDietData parsedData, Map<String, String> recipeIds) {
        List<Day> modelDays = new ArrayList<>();

        if (parsedData.getDays() == null) return modelDays;

        for (int i = 0; i < parsedData.getDays().size(); i++) {
            ParsedDay parsedDay = parsedData.getDays().get(i);

            Day modelDay = new Day();
            modelDay.setDate(parsedDay.getDate());

            List<DayMeal> modelMeals = new ArrayList<>();
            if (parsedDay.getMeals() != null) {
                for (ParsedMeal parsedMeal : parsedDay.getMeals()) {

                    DayMeal modelMeal = new DayMeal();
                    modelMeal.setName(parsedMeal.getName());
                    modelMeal.setInstructions(parsedMeal.getInstructions());
                    modelMeal.setTime(parsedMeal.getTime());
                    modelMeal.setMealType(parsedMeal.getMealType());

                    String recipeKey = i + "_" + parsedMeal.getMealType().name();
                    if (recipeIds.containsKey(recipeKey)) {
                        modelMeal.setRecipeId(recipeIds.get(recipeKey));
                    }

                    // 4. Map Ingredients
                    modelMeal.setIngredients(mapParsedIngredientsToModel(parsedMeal.getIngredients()));

                    modelMeals.add(modelMeal);
                }
            }
            modelDay.setMeals(modelMeals);
            modelDays.add(modelDay);
        }
        return modelDays;
    }

    private List<RecipeIngredient> mapParsedIngredientsToModel(List<ParsedProduct> parsedProducts) {
        if (parsedProducts == null) return new ArrayList<>();

        return parsedProducts.stream()
                .map(pp -> {
                    Long validPostgresId = null;
                    if (pp.getId() != null) {
                        try {
                            validPostgresId = Long.parseLong(pp.getId());
                        } catch (NumberFormatException e) {
                            // Valid scenario: ID is a UUID (legacy) or null, ignore it
                        }
                    }

                    return RecipeIngredient.builder()
                            .id(UUID.randomUUID().toString())
                            .name(pp.getName())
                            .quantity(pp.getQuantity())
                            .unit(pp.getUnit())
                            .original(pp.getOriginal())
                            .categoryId(pp.getCategoryId())
                            .hasCustomUnit(pp.isHasCustomUnit())
                            .productId(validPostgresId)
                            .build();
                })
                .collect(Collectors.toList());
    }
}