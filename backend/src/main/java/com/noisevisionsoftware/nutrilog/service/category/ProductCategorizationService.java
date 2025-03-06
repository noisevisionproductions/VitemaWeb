package com.noisevisionsoftware.nutrilog.service.category;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.shopping.category.ProductCategoryData;
import com.noisevisionsoftware.nutrilog.utils.StringUtils;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
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
    private static final double SIMILARITY_THRESHOLD = 0.85;

    private final FirestoreCategoryDataManager dataManager;
    private Map<String, ProductCategoryData> categoryData = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        categoryData = dataManager.loadData();
    }

    private void saveDataIfChanged() {
        try {
            dataManager.saveData(categoryData);
        } catch (Exception e) {
            log.error("Error during save of categorization data", e);
        }
    }

    public String suggestCategory(ParsedProduct product) {
        String normalizedName = cleanProductName(product.getOriginal());
        log.debug("Suggesting category for: '{}'", normalizedName);

        // 1. Dokładne dopasowanie
        Optional<ProductCategoryData> exactMatch = findExactMatch(normalizedName);
        if (exactMatch.isPresent()) {
            log.debug("Found exact match: {}", exactMatch.get().getCategoryId());
            return exactMatch.get().getCategoryId();
        }

        // 2. Dopasowanie przez warianty
        Optional<ProductCategoryData> variationMatch = findVariationMatch(normalizedName);
        if (variationMatch.isPresent()) {
            log.debug("Found variation match: {}", variationMatch.get().getCategoryId());
            return variationMatch.get().getCategoryId();
        }

        // 3. Dopasowanie przez podobieństwo
        Optional<ProductCategoryData> similarMatch = findSimilarMatch(normalizedName);
        if (similarMatch.isPresent()) {
            log.debug("Found similarity match: {}", similarMatch.get().getCategoryId());
            return similarMatch.get().getCategoryId();
        }

        log.debug("No category found for: '{}'", normalizedName);
        return null;
    }

    public void updateCategorization(ParsedProduct product) {
        String originalName = product.getOriginal();
        String normalizedNameWithoutUnits = cleanProductName(StringUtils.removeUnits(originalName));
        Timestamp now = Timestamp.now();

        categoryData.compute(normalizedNameWithoutUnits, (key, existingData) -> {
            if (existingData == null) {
                List<String> variations = new ArrayList<>();
                variations.add(originalName);

                return ProductCategoryData.builder()
                        .productName(StringUtils.removeUnits(originalName))
                        .categoryId(product.getCategoryId())
                        .usageCount(1)
                        .variations(variations)
                        .lastUsed(now)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
            } else {
                existingData.setUsageCount(existingData.getUsageCount() + 1);
                if (!existingData.getVariations().contains(originalName)) {
                    existingData.getVariations().add(originalName);
                }
                existingData.setLastUsed(now);
                existingData.setUpdatedAt(now);
                return existingData;
            }
        });
    }

    public void updateCategoriesInTransaction(Map<String, List<ParsedProduct>> categorizedProducts) {
        if (categorizedProducts == null || categorizedProducts.isEmpty()) {
            log.warn("No categorized products to update");
            return;
        }

        categorizedProducts.forEach((categoryId, products) -> {
            products.forEach(product -> {
                product.setCategoryId(categoryId);
                updateCategorization(product);
            });
        });

        saveDataIfChanged();
    }

    private Optional<ProductCategoryData> findExactMatch(String normalizedName) {
        String nameWithoutUnits = StringUtils.removeUnits(normalizedName);
        return categoryData.values().stream()
                .filter(data -> data.getProductName().equals(nameWithoutUnits))
                .max(Comparator.comparing(ProductCategoryData::getUsageCount));
    }

    private Optional<ProductCategoryData> findVariationMatch(String normalizedName) {
        return categoryData.values().stream()
                .filter(data -> data.getVariations() != null &&
                        data.getVariations().stream()
                                .map(this::cleanProductName)
                                .anyMatch(v -> v.equals(normalizedName)))
                .max(Comparator.comparing(ProductCategoryData::getUsageCount));
    }

    private Optional<ProductCategoryData> findSimilarMatch(String normalizedName) {
        String nameWithoutUnits = StringUtils.removeUnits(normalizedName);
        return categoryData.values().stream()
                .map(data -> Map.entry(data, calculateSimilarity(nameWithoutUnits, data.getProductName())))
                .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1.length() < 3 || s2.length() < 3) return 0.0;

        int maxLength = Math.max(s1.length(), s2.length());

        int levenshteinDistance = LevenshteinDistance.getDefaultInstance().apply(s1, s2);
        return 1.0 - ((double) levenshteinDistance / maxLength);
    }

    private String cleanProductName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9ąćęłńóśźż]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
