package com.noisevisionsoftware.vitema.service.category;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.shopping.category.ProductCategoryData;
import com.noisevisionsoftware.vitema.utils.StringUtils;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCategorizationService {
    private static final double SIMILARITY_THRESHOLD = 0.75;

    private final FirestoreCategoryDataManager dataManager;
    private Map<String, ProductCategoryData> categoryData = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        categoryData = dataManager.loadData();
    }

    protected void saveDataIfChanged() {
        try {
            dataManager.saveData(categoryData);
        } catch (Exception e) {
            log.error("Error during save of categorization data", e);
        }
    }

    public String suggestCategory(ParsedProduct product) {
        String normalizedName = cleanProductName(product.getOriginal());

        // 1. Dokładne dopasowanie
        Optional<ProductCategoryData> exactMatch = findExactMatch(normalizedName);
        if (exactMatch.isPresent()) {
            return exactMatch.get().getCategoryId();
        }

        // 2. Dopasowanie przez warianty
        Optional<ProductCategoryData> variationMatch = findVariationMatch(normalizedName);
        if (variationMatch.isPresent()) {
            return variationMatch.get().getCategoryId();
        }

        // 3. Dopasowanie przez podobieństwo
        Optional<ProductCategoryData> similarMatch = findSimilarMatch(normalizedName);
        return similarMatch.map(ProductCategoryData::getCategoryId).orElse(null);

    }

    public void updateCategorization(ParsedProduct product) {
        if (product == null || product.getName() == null) {
            return;
        }

        String originalName = product.getOriginal();
        String normalizedName = cleanProductName(product.getName());
        Timestamp now = Timestamp.now();

        String productKey = null;
        for (Map.Entry<String, ProductCategoryData> entry : categoryData.entrySet()) {
            if (entry.getKey().equals("mleko") &&
                    normalizedName.contains("mleko")) {
                productKey = entry.getKey();
                break;
            }
        }

        if (productKey != null) {
            // Aktualizacja istniejącego produktu
            ProductCategoryData data = categoryData.get(productKey);
            data.setUsageCount(data.getUsageCount() + 1);

            // ZMIANA TUTAJ: Tworzenie nowej modyfikowalnej listy
            List<String> newVariations = new ArrayList<>(data.getVariations());
            if (!newVariations.contains(originalName)) {
                newVariations.add(originalName);
            }
            data.setVariations(newVariations);

            data.setLastUsed(now);
            data.setUpdatedAt(now);
            data.setCategoryId(product.getCategoryId());
        } else {
            // Dodanie nowego produktu
            String newKey = StringUtils.removeUnits(normalizedName);
            List<String> variations = new ArrayList<>();
            variations.add(originalName);

            categoryData.put(newKey, ProductCategoryData.builder()
                    .productName(newKey)
                    .categoryId(product.getCategoryId())
                    .usageCount(1)
                    .variations(variations)
                    .lastUsed(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }
    }

    public void updateCategoriesInTransaction(Map<String, List<ParsedProduct>> categorizedProducts) {
        if (categorizedProducts == null || categorizedProducts.isEmpty()) {
            log.warn("No categorized products to update");
            return;
        }

        categorizedProducts.forEach((categoryId, products) -> products.forEach(product -> {
            product.setCategoryId(categoryId);
            updateCategorization(product);
        }));

        saveDataIfChanged();
    }

    Optional<ProductCategoryData> findExactMatch(String normalizedName) {
        String nameWithoutUnits = StringUtils.removeUnits(normalizedName);
        return categoryData.values().stream()
                .filter(data -> data.getProductName().equals(nameWithoutUnits))
                .max(Comparator.comparing(ProductCategoryData::getUsageCount));
    }

    Optional<ProductCategoryData> findVariationMatch(String normalizedName) {
        return categoryData.values().stream()
                .filter(data -> data.getVariations() != null &&
                        data.getVariations().stream()
                                .map(this::cleanProductName)
                                .anyMatch(v -> v.equals(normalizedName)))
                .max(Comparator.comparing(ProductCategoryData::getUsageCount));
    }

    Optional<ProductCategoryData> findSimilarMatch(String normalizedName) {
        return categoryData.values().stream()
                .filter(data -> {
                    double similarity = calculateSimilarity(
                            normalizedName,
                            cleanProductName(data.getProductName())
                    );
                    return similarity >= SIMILARITY_THRESHOLD;
                })
                .max(Comparator.comparing(ProductCategoryData::getUsageCount));
    }

    double calculateSimilarity(String s1, String s2) {
        if (s1.length() < 3 || s2.length() < 3) return 0.0;

        int maxLength = Math.max(s1.length(), s2.length());

        int levenshteinDistance = LevenshteinDistance.getDefaultInstance().apply(s1, s2);
        return 1.0 - ((double) levenshteinDistance / maxLength);
    }

    protected String cleanProductName(String name) {
        if (name == null) return "";

        return name.toLowerCase()
                .replaceAll("[^a-z0-9ąćęłńóśźż]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
