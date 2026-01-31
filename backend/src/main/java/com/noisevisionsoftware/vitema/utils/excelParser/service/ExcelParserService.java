package com.noisevisionsoftware.vitema.utils.excelParser.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.utils.excelParser.config.ExcelParserConfig;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsingResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ExcelParserService {

    private final ProductParsingService productParsingService;
    private final ProductCategorizationService categorizationService;
    private final ExcelParserConfig excelParserConfig;

    public record ParsedExcelResult(
            List<ParsedMeal> meals,
            int totalMeals,
            List<Map.Entry<String, ParsedProduct>> shoppingList
    ) {
    }

    public ParsedExcelResult parseDietExcel(MultipartFile file) throws IOException {
        return parseDietExcel(file, excelParserConfig.getSkipColumnsCount());
    }

    public ParsedExcelResult parseDietExcel(MultipartFile file, int skipColumnsCount) throws IOException {
        if (skipColumnsCount < 0) {
            skipColumnsCount = excelParserConfig.getSkipColumnsCount();
        } else if (skipColumnsCount > excelParserConfig.getMaxSkipColumnsCount()) {
            skipColumnsCount = excelParserConfig.getSkipColumnsCount();
        }

        List<List<String>> rows = new ArrayList<>();

        EasyExcel.read(file.getInputStream())
                .sheet()
                .headRowNumber(0)
                .registerReadListener(new AnalysisEventListener<Map<Integer, String>>() {
                    @Override
                    public void invoke(Map<Integer, String> rowMap, AnalysisContext context) {
                        List<String> rowData = new ArrayList<>();
                        for (int i = 0; i < rowMap.size(); i++) {
                            String value = rowMap.get(i);
                            rowData.add(value != null ? value.trim() : "");
                        }
                        rows.add(rowData);
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        log.debug("Zakończono czytanie pliku Excel. Znaleziono {} wierszy.", rows.size());
                    }
                })
                .doRead();

        List<ParsedMeal> meals = new ArrayList<>();
        Map<String, ParsedProduct> uniqueItems = new HashMap<>();

        // Pomijamy pierwszy wiersz (nagłówki)
        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);

            if (row.size() <= skipColumnsCount + 1) {
                continue;
            }

            String mealName = row.get(skipColumnsCount).trim();
            if (mealName.isEmpty()) {
                continue;
            }

            ParsedMeal meal = new ParsedMeal();
            meal.setName(mealName);

            String instructions = "";
            if (row.size() > skipColumnsCount + 1) {
                instructions = row.get(skipColumnsCount + 1).trim();
            }
            meal.setInstructions(instructions);

            // Parsowanie składników-trzecia kolumna po pominiętych
            List<ParsedProduct> mealIngredients = new ArrayList<>();
            if (row.size() > skipColumnsCount + 2 && !row.get(skipColumnsCount + 2).trim().isEmpty()) {
                List<String> shoppingItems = splitIngredientsList(row.get(skipColumnsCount + 2));
                for (String item : shoppingItems) {
                    item = item.trim();
                    if (!item.isEmpty()) {
                        try {
                            ParsedProduct product = parseProduct(item);
                            
                            // Dodawanie do składników posiłku
                            mealIngredients.add(product);

                            // Dodawanie bezpośrednio do listy zakupów
                            String key = product.getOriginal().toLowerCase().trim();
                            uniqueItems.merge(key, product, (existing, newProduct) -> {
                                if (existing.getUnit().equals(newProduct.getUnit())) {
                                    existing.setQuantity(existing.getQuantity() + newProduct.getQuantity());
                                    return existing;
                                }
                                return newProduct;
                            });
                        } catch (Exception e) {
                            log.error("Błąd podczas parsowania produktu: {}", item, e);
                            // Tworzymy podstawowy produkt, gdy parsowanie się nie powiedzie
                            ParsedProduct fallbackProduct = ParsedProduct.builder()
                                    .name(item)
                                    .quantity(1.0)
                                    .unit("szt")
                                    .original(item)
                                    .hasCustomUnit(false)
                                    .build();

                            // Dodawanie do składników posiłku
                            mealIngredients.add(fallbackProduct);

                            // Dodawanie do listy zakupów
                            String key = item.toLowerCase().trim();
                            uniqueItems.put(key, fallbackProduct);
                        }
                    }
                }
            }

            // Parsowanie wartości odżywczych-czwarta kolumna po pominiętych
            if (row.size() > skipColumnsCount + 3 && !row.get(skipColumnsCount + 3).trim().isEmpty()) {
                try {
                    meal.setNutritionalValues(parseNutritionalValues(row.get(skipColumnsCount + 3)));
                } catch (Exception e) {
                    log.error("Błąd podczas parsowania wartości odżywczych: {}", row.get(skipColumnsCount + 3), e);
                }
            }

            meal.setMealType(MealType.BREAKFAST);
            meal.setTime("");

            meal.setIngredients(mealIngredients);

            meals.add(meal);
        }

//        List<ParsedProduct> allProducts = new ArrayList<>(uniqueItems.values());
//        List<ParsedProduct> combinedProducts = combineSimilarProducts(allProducts);

        List<Map.Entry<String, ParsedProduct>> shoppingList = uniqueItems.values().stream()
                .map(product -> Map.entry(product.getOriginal(), product))
                .collect(Collectors.toList());

        return new ParsedExcelResult(
                meals,
                meals.size(),
                shoppingList
        );
    }

    private ParsedProduct parseProduct(String ingredient) {
        try {
            ParsingResult result = productParsingService.parseProduct(ingredient);

            if (result.isSuccess() && result.getProduct() != null) {
                ParsedProduct originalProduct = result.getProduct();

                String suggestedCategory = categorizationService.suggestCategory(originalProduct);

                ParsedProduct product = ParsedProduct.builder()
                        .name(originalProduct.getName())
                        .quantity(originalProduct.getQuantity())
                        .unit(originalProduct.getUnit())
                        .original(originalProduct.getOriginal())
                        .hasCustomUnit(originalProduct.isHasCustomUnit())
                        .categoryId(suggestedCategory)
                        .build();

                if (suggestedCategory != null) {
                    categorizationService.updateCategorization(product);
                }
                return product;
            } else {

                return ParsedProduct.builder()
                        .name(ingredient)
                        .quantity(1.0)
                        .unit("szt")
                        .original(ingredient)
                        .hasCustomUnit(false)
                        .categoryId(null)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error parsing product: {}", ingredient, e);
            return ParsedProduct.builder()
                    .name(ingredient)
                    .quantity(1.0)
                    .unit("szt")
                    .original(ingredient)
                    .hasCustomUnit(false)
                    .categoryId(null)
                    .build();
        }
    }

    private NutritionalValues parseNutritionalValues(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        NutritionalValues nutritionalValues = new NutritionalValues();
        try {
            String[] values = value.split(",");
            if (values.length == 4) {
                // Czyszczenie i parsowanie każdej wartości
                double calories = parseNutritionalValue(values[0]);
                double protein = parseNutritionalValue(values[1]);
                double fat = parseNutritionalValue(values[2]);
                double carbs = parseNutritionalValue(values[3]);

                // Walidacja wartości
                if (isValidNutritionalValue(calories) &&
                        isValidNutritionalValue(protein) &&
                        isValidNutritionalValue(fat) &&
                        isValidNutritionalValue(carbs)) {

                    nutritionalValues.setCalories(calories);
                    nutritionalValues.setProtein(protein);
                    nutritionalValues.setFat(fat);
                    nutritionalValues.setCarbs(carbs);
                    return nutritionalValues;
                }
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.warn("Error parsing nutritional values: {}", value, e);
        }
        return null;
    }

    private double parseNutritionalValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }

        try {
            String cleaned = value.replaceAll("[^0-9.,]", "")
                    .replace(',', '.');

            if (cleaned.isEmpty()) {
                return 0.0;
            }

            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Nie można sparsować wartości: '{}', zwracam 0", value);
            return 0.0;
        }
    }

    private boolean isValidNutritionalValue(double value) {
        return value >= 0 && value <= 1000;
    }


    private List<String> splitIngredientsList(String ingredientsStr) {
        List<String> result = new ArrayList<>();

        // Wzorzec do wykrywania produktów oddzielonych przecinkami
        // Ten wzorzec uwzględnia przecinki w liczbach
        Pattern pattern = Pattern.compile(
                // Produkt może zawierać tekst, liczby (również z przecinkiem jako separator dziesiętny)
                // i kończy się przecinkiem z opcjonalną spacją
                "([^,]+\\d+[,.]\\d+\\s*[a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ]+|[^,]+),\\s*"
        );

        Matcher matcher = pattern.matcher(ingredientsStr + ", "); // Dodajemy przecinek na końcu dla ułatwienia
        int lastEnd = 0;

        // Znajdź wszystkie dopasowania
        while (matcher.find()) {
            String item = matcher.group(1).trim();
            if (!item.isEmpty()) {
                result.add(item);
            }
            lastEnd = matcher.end();
        }

        // Dodaj ostatni element, jeśli pozostał
        if (lastEnd < ingredientsStr.length()) {
            String lastItem = ingredientsStr.substring(lastEnd).trim();
            if (!lastItem.isEmpty() && !lastItem.equals(",")) {
                result.add(lastItem);
            }
        }

        return result;
    }
}
/*

    // Metoda do łączenia podobnych produktów w liście zakupów
    private List<ParsedProduct> combineSimilarProducts(List<ParsedProduct> products) {
        Map<String, ParsedProduct> combinedProducts = new HashMap<>();

        for (ParsedProduct product : products) {
            // Szukamy podobnego produktu
            String bestMatch = findBestMatch(product.getName(), combinedProducts.keySet());

            if (bestMatch != null) {
                ParsedProduct existing = combinedProducts.get(bestMatch);
                // Sprawdzamy, czy jednostki można połączyć
                if (unitService.canCombineQuantities(existing.getUnit(), product.getUnit())) {
                    // Normalizujemy jednostki przed połączeniem
                    double normalizedQuantity = normalizeAndCombineQuantities(
                            existing.getQuantity(), existing.getUnit(),
                            product.getQuantity(), product.getUnit()
                    );
                    existing.setQuantity(normalizedQuantity);
                }
            } else {
                combinedProducts.put(product.getName(), product);
            }
        }

        return new ArrayList<>(combinedProducts.values());
    }

    private String findBestMatch(String productName, Set<String> existingNames) {
        double similarityThreshold = 0.9;
        double bestSimilarity = 0.0;
        String bestMatch = null;

        for (String existing : existingNames) {
            double similarity = calculateSimilarity(productName, existing);
            if (similarity > similarityThreshold && similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = existing;
            }
        }

        return bestMatch;
    }

    double calculateSimilarity(String str1, String str2) {
        LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();
        int distance = levenshtein.apply(str1.toLowerCase(), str2.toLowerCase());

        return 1.0 - ((double) distance / Math.max(str1.length(), str2.length()));
    }

    private double normalizeAndCombineQuantities(
            double quantity1, String unit1,
            double quantity2, String unit2
    ) {
        // Normalizacja do jednostki bazowej
        Optional<UnitService.NormalizedValue> normalized1 = unitService.normalizeToBaseUnit(quantity1, unit1);
        Optional<UnitService.NormalizedValue> normalized2 = unitService.normalizeToBaseUnit(quantity2, unit2);

        if (normalized1.isPresent() && normalized2.isPresent() &&
                normalized1.get().unit().equals(normalized2.get().unit())) {
            return normalized1.get().value() + normalized2.get().value();
        }

        // Jeśli nie można znormalizować, zwracamy sumę oryginalnych wartości
        return quantity1 + quantity2;
    }

}*/
