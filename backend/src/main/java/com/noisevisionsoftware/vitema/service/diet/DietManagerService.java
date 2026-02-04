package com.noisevisionsoftware.vitema.service.diet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.vitema.dto.diet.*;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.diet.FirestoreDietMapper;
import com.noisevisionsoftware.vitema.model.diet.*;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.repository.DietRepository;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietManagerService {

    private final Firestore firestore;
    private final ProductParsingService productParsingService;
    private final FirestoreDietMapper firestoreMapper;
    private final DietService dietService;
    private final DietRepository dietRepository;
    private final RecipeService recipeService;
    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    public String saveDietWithShoppingList(
            ParsedDietData parsedData,
            String userId,
            String authorId,
            DietFileInfo fileInfo
    ) {
        try {
            // 1. Create new diet document
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

    public List<DietHistorySummaryDto> getTrainerDietHistory(String trainerId) {
        return dietRepository.findByUserId(trainerId).stream()
                .filter(diet -> trainerId.equals(diet.getAuthorId()))
                .sorted(Comparator.comparing(Diet::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(20)
                .map(diet -> DietHistorySummaryDto.builder()
                        .id(diet.getId())
                        .name(diet.getMetadata() != null ? diet.getMetadata().getFileName() : "Dieta bez nazwy")
                        .clientName("Pacjent: " + diet.getUserId())
                        .date(diet.getCreatedAt() != null ? diet.getCreatedAt().toString() : "")
                        .build())
                .collect(Collectors.toList());
    }

    public DietDraftDto getDietAsDraft(String dietId) {
        Diet oldDiet = dietRepository.findById(dietId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono diety: " + dietId));

        String oldName = (oldDiet.getMetadata() != null && oldDiet.getMetadata().getFileName() != null)
                ? oldDiet.getMetadata().getFileName()
                : "Nowa dieta";

        return DietDraftDto.builder()
                .dietId(null)
                .userId(oldDiet.getUserId())
                .name(oldName + " (Kopia)")
                .days(mapDaysToDto(oldDiet.getDays()))
                .build();
    }

    private List<DietDayDto> mapDaysToDto(List<Day> days) {
        if (days == null) return Collections.emptyList();

        return days.stream().map(day -> DietDayDto.builder()
                .date(day.getDate() != null ? day.getDate().toDate().toInstant().toString() : null)
                .meals(mapMealsToDto(day.getMeals()))
                .build()
        ).collect(Collectors.toList());
    }

    private List<DietMealDto> mapMealsToDto(List<DayMeal> meals) {
        if (meals == null) return Collections.emptyList();

        return meals.stream().map(meal -> DietMealDto.builder()
                .originalRecipeId(meal.getRecipeId())
                .name(meal.getName())
                .mealType(meal.getMealType() != null ? meal.getMealType().name() : null)
                .time(meal.getTime())
                .instructions(meal.getInstructions())
                .ingredients(mapIngredientsToDto(meal.getIngredients()))
                .build()
        ).collect(Collectors.toList());
    }

    private List<DietIngredientDto> mapIngredientsToDto(List<RecipeIngredient> ingredients) {
        if (ingredients == null) return Collections.emptyList();

        return ingredients.stream().map(ing -> DietIngredientDto.builder()
                .name(ing.getName())
                .quantity(ing.getQuantity())
                .unit(ing.getUnit())
                .productId(ing.getId())
                .categoryId(ing.getCategoryId())
                .build()
        ).collect(Collectors.toList());
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        return timestamp.toDate().toString();
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

                // Tutaj zapisujemy podstawowe dane, ale nowy model wspiera pełne dane w posiłku
                // W przyszłości warto to zaktualizować, by zapisywało też składniki wprost (tak jak updateDietStructure)
                mealMap.put("recipeId", savedRecipeIds.get(dayIndex + "_" + meal.getMealType().name()));
                mealMap.put("originalRecipeId", savedRecipeIds.get(dayIndex + "_" + meal.getMealType().name())); // Dla spójności z nowym modelem
                mealMap.put("name", meal.getName()); // Dodajemy nazwę wprost
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