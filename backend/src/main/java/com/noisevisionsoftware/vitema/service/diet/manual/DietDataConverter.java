package com.noisevisionsoftware.vitema.service.diet.manual;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service odpowiedzialny za konwersję danych diet między różnymi formatami
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DietDataConverter {

    /**
     * Konwertuje ManualDietRequest na ParsedDietData
     */
    public ParsedDietData convertToParsedDietData(ManualDietRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ManualDietRequest nie może być null");
        }

        ParsedDietData parsedData = new ParsedDietData();

        // Podstawowe dane
        parsedData.setDays(request.getDays());
        parsedData.setMealsPerDay(request.getMealsPerDay());
        parsedData.setDuration(request.getDuration());
        parsedData.setMealTimes(request.getMealTimes());

        // Konwersja daty rozpoczęcia
        parsedData.setStartDate(convertStartDate(request.getStartDate()));

        // Generowanie listy zakupów
        generateShoppingList(parsedData, request);

        log.debug("Skonwertowano ManualDietRequest na ParsedDietData dla {} dni",
                parsedData.getDays() != null ? parsedData.getDays().size() : 0);

        return parsedData;
    }

    /**
     * Generuje listę zakupów na podstawie składników w diecie
     */
    public void generateShoppingList(ParsedDietData parsedData, ManualDietRequest request) {
        List<String> shoppingList = new ArrayList<>();
        Map<String, List<String>> categorizedProducts = new HashMap<>();
        Map<String, Double> ingredientQuantities = new HashMap<>();

        // Przejdź przez wszystkie dni i posiłki
        for (ParsedDay day : request.getDays()) {
            if (day.getMeals() == null) continue;

            for (ParsedMeal meal : day.getMeals()) {
                if (meal.getIngredients() == null) continue;

                for (ParsedProduct ingredient : meal.getIngredients()) {
                    processIngredientForShopping(ingredient, shoppingList,
                            categorizedProducts, ingredientQuantities);
                }
            }
        }

        // Optymalizuj listę zakupów - agreguj podobne składniki
        optimizeShoppingList(shoppingList, ingredientQuantities);

        parsedData.setShoppingList(shoppingList);
        parsedData.setCategorizedProducts(categorizedProducts);

        log.debug("Wygenerowano listę zakupów z {} pozycjami w {} kategoriach",
                shoppingList.size(), categorizedProducts.size());
    }

    /**
     * Konwertuje ParsedDietData z powrotem na ManualDietRequest (jeśli potrzebne)
     */
    public ManualDietRequest convertToManualDietRequest(ParsedDietData parsedData, String userId) {
        if (parsedData == null) {
            throw new IllegalArgumentException("ParsedDietData nie może być null");
        }

        return ManualDietRequest.builder()
                .userId(userId)
                .days(parsedData.getDays())
                .mealsPerDay(parsedData.getMealsPerDay())
                .duration(parsedData.getDuration())
                .startDate(convertTimestampToString(parsedData.getStartDate()))
                .mealTimes(parsedData.getMealTimes())
                .build();
    }

    /**
     * Tworzy podsumowanie diety
     */
    public Map<String, Object> createDietSummary(ParsedDietData parsedData) {
        Map<String, Object> summary = new HashMap<>();

        int totalMeals = 0;
        int totalIngredients = 0;
        int daysWithoutMeals = 0;

        if (parsedData.getDays() != null) {
            for (ParsedDay day : parsedData.getDays()) {
                if (day.getMeals() == null || day.getMeals().isEmpty()) {
                    daysWithoutMeals++;
                    continue;
                }

                totalMeals += day.getMeals().size();

                for (ParsedMeal meal : day.getMeals()) {
                    if (meal.getIngredients() != null) {
                        totalIngredients += meal.getIngredients().size();
                    }
                }
            }
        }

        summary.put("totalDays", parsedData.getDays() != null ? parsedData.getDays().size() : 0);
        summary.put("totalMeals", totalMeals);
        summary.put("totalIngredients", totalIngredients);
        summary.put("daysWithoutMeals", daysWithoutMeals);
        summary.put("averageMealsPerDay", parsedData.getDays() != null && !parsedData.getDays().isEmpty()
                ? (double) totalMeals / parsedData.getDays().size() : 0);
        summary.put("shoppingListSize", parsedData.getShoppingList() != null ? parsedData.getShoppingList().size() : 0);
        summary.put("categoriesCount", parsedData.getCategorizedProducts() != null ? parsedData.getCategorizedProducts().size() : 0);

        return summary;
    }

    // Metody pomocnicze (private)

    private Timestamp convertStartDate(String startDateString) {
        try {
            LocalDate startDate = LocalDate.parse(startDateString);
            return Timestamp.of(
                    java.sql.Timestamp.valueOf(startDate.atStartOfDay())
            );
        } catch (Exception e) {
            log.error("Błąd podczas konwersji daty rozpoczęcia: {}", startDateString, e);
            // Fallback na dzisiejszą datę
            return Timestamp.of(
                    java.sql.Timestamp.valueOf(LocalDate.now().atStartOfDay())
            );
        }
    }

    private String convertTimestampToString(Timestamp timestamp) {
        if (timestamp == null) return LocalDate.now().toString();

        try {
            return LocalDate.ofEpochDay(timestamp.getSeconds() / 86400).toString();
        } catch (Exception e) {
            log.warn("Błąd podczas konwersji Timestamp na String", e);
            return LocalDate.now().toString();
        }
    }

    private void processIngredientForShopping(ParsedProduct ingredient,
                                              List<String> shoppingList,
                                              Map<String, List<String>> categorizedProducts,
                                              Map<String, Double> ingredientQuantities) {

        if (ingredient.getOriginal() == null || ingredient.getOriginal().trim().isEmpty()) {
            return;
        }

        String originalText = ingredient.getOriginal().trim();

        // Dodaj do głównej listy zakupów
        if (!shoppingList.contains(originalText)) {
            shoppingList.add(originalText);
        }

        // Agreguj ilości
        String key = createIngredientKey(ingredient);
        ingredientQuantities.merge(key, ingredient.getQuantity() != null ? ingredient.getQuantity() : 1.0, Double::sum);

        // Kategoryzuj produkty
        String categoryId = ingredient.getCategoryId();
        if (categoryId != null && !categoryId.trim().isEmpty()) {
            categorizedProducts.computeIfAbsent(categoryId, k -> new ArrayList<>());
            if (!categorizedProducts.get(categoryId).contains(originalText)) {
                categorizedProducts.get(categoryId).add(originalText);
            }
        }
    }

    private String createIngredientKey(ParsedProduct ingredient) {
        return ingredient.getName() + "_" + ingredient.getUnit();
    }

    private void optimizeShoppingList(List<String> shoppingList, Map<String, Double> ingredientQuantities) {
        // Tutaj można dodać logikę optymalizacji listy zakupów
        // Na przykład: łączenie podobnych składników, sortowanie według kategorii, itp.

        // Obecnie tylko sortujemy alfabetycznie
        shoppingList.sort(String::compareToIgnoreCase);

        log.debug("Zoptymalizowano listę zakupów - {} unikalnych składników", shoppingList.size());
    }

    /**
     * Waliduje czy dane można skonwertować
     */
    public boolean canConvert(ManualDietRequest request) {
        if (request == null) return false;
        if (request.getDays() == null || request.getDays().isEmpty()) return false;
        if (request.getStartDate() == null || request.getStartDate().trim().isEmpty()) return false;
        if (request.getMealsPerDay() <= 0) return false;

        return true;
    }

    /**
     * Tworzy pustą strukturę diety na podstawie parametrów
     */
    public ParsedDietData createEmptyDietStructure(int days, int mealsPerDay, String startDate) {
        ParsedDietData parsedData = new ParsedDietData();
        parsedData.setDays(new ArrayList<>());
        parsedData.setMealsPerDay(mealsPerDay);
        parsedData.setDuration(days);
        parsedData.setStartDate(convertStartDate(startDate));
        parsedData.setShoppingList(new ArrayList<>());
        parsedData.setCategorizedProducts(new HashMap<>());

        // Stwórz pustą strukturę dni i posiłków
        for (int day = 0; day < days; day++) {
            ParsedDay parsedDay = new ParsedDay();
            parsedDay.setMeals(new ArrayList<>());

            for (int meal = 0; meal < mealsPerDay; meal++) {
                ParsedMeal parsedMeal = new ParsedMeal();
                parsedMeal.setName("Posiłek " + (meal + 1));
                parsedMeal.setIngredients(new ArrayList<>());
                parsedDay.getMeals().add(parsedMeal);
            }

            parsedData.getDays().add(parsedDay);
        }

        return parsedData;
    }

    /**
     * Klonuje dietę z możliwością modyfikacji parametrów
     */
    public ParsedDietData cloneDietWithModifications(ParsedDietData original,
                                                     Integer newDuration,
                                                     Integer newMealsPerDay,
                                                     String newStartDate) {
        if (original == null) {
            throw new IllegalArgumentException("Oryginalna dieta nie może być null");
        }

        ParsedDietData cloned = new ParsedDietData();

        // Użyj nowych wartości lub zachowaj oryginalne
        cloned.setDuration(newDuration != null ? newDuration : original.getDuration());
        cloned.setMealsPerDay(newMealsPerDay != null ? newMealsPerDay : original.getMealsPerDay());
        cloned.setStartDate(newStartDate != null ? convertStartDate(newStartDate) : original.getStartDate());
        cloned.setMealTimes(original.getMealTimes());

        // Klonuj dni - głęboka kopia
        cloned.setDays(cloneDays(original.getDays(), cloned.getDuration(), cloned.getMealsPerDay()));

        // Przelicz listę zakupów
        cloned.setShoppingList(new ArrayList<>(original.getShoppingList()));
        cloned.setCategorizedProducts(cloneCategorizedProducts(original.getCategorizedProducts()));

        return cloned;
    }

    private List<ParsedDay> cloneDays(List<ParsedDay> originalDays, int newDuration, int newMealsPerDay) {
        List<ParsedDay> clonedDays = new ArrayList<>();

        for (int i = 0; i < newDuration; i++) {
            ParsedDay clonedDay = new ParsedDay();
            clonedDay.setMeals(new ArrayList<>());

            // Jeśli oryginalny dzień istnieje, sklonuj go
            if (originalDays != null && i < originalDays.size()) {
                ParsedDay originalDay = originalDays.get(i);

                for (int j = 0; j < newMealsPerDay; j++) {
                    ParsedMeal clonedMeal;

                    // Jeśli oryginalny posiłek istnieje, sklonuj go
                    if (originalDay.getMeals() != null && j < originalDay.getMeals().size()) {
                        clonedMeal = cloneMeal(originalDay.getMeals().get(j));
                    } else {
                        // Stwórz pusty posiłek
                        clonedMeal = new ParsedMeal();
                        clonedMeal.setName("Posiłek " + (j + 1));
                        clonedMeal.setIngredients(new ArrayList<>());
                    }

                    clonedDay.getMeals().add(clonedMeal);
                }
            } else {
                // Stwórz pusty dzień
                for (int j = 0; j < newMealsPerDay; j++) {
                    ParsedMeal emptyMeal = new ParsedMeal();
                    emptyMeal.setName("Posiłek " + (j + 1));
                    emptyMeal.setIngredients(new ArrayList<>());
                    clonedDay.getMeals().add(emptyMeal);
                }
            }

            clonedDays.add(clonedDay);
        }

        return clonedDays;
    }

    private ParsedMeal cloneMeal(ParsedMeal original) {
        ParsedMeal cloned = new ParsedMeal();
        cloned.setName(original.getName());
        cloned.setInstructions(original.getInstructions());
        cloned.setPhotos(original.getPhotos() != null ? new ArrayList<>(original.getPhotos()) : new ArrayList<>());

        // Klonuj składniki
        if (original.getIngredients() != null) {
            cloned.setIngredients(new ArrayList<>());
            for (ParsedProduct ingredient : original.getIngredients()) {
                cloned.getIngredients().add(cloneIngredient(ingredient));
            }
        } else {
            cloned.setIngredients(new ArrayList<>());
        }

        return cloned;
    }

    private ParsedProduct cloneIngredient(ParsedProduct original) {
        return ParsedProduct.builder()
                .id(original.getId())
                .name(original.getName())
                .quantity(original.getQuantity())
                .unit(original.getUnit())
                .original(original.getOriginal())
                .categoryId(original.getCategoryId())
                .hasCustomUnit(original.isHasCustomUnit())
                .build();
    }

    private Map<String, List<String>> cloneCategorizedProducts(Map<String, List<String>> original) {
        if (original == null) return new HashMap<>();

        Map<String, List<String>> cloned = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : original.entrySet()) {
            cloned.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return cloned;
    }
}