package com.noisevisionsoftware.vitema.repository;

import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.model.product.Product;
import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductRepository {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "products";

    public List<Product> searchProducts(String query, String trainerId) {
        try {
            String normalizedQuery = query.toLowerCase().trim();

            // Query for products where searchName starts with the query
            Query globalQuery = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("type", ProductType.GLOBAL.name())
                    .whereGreaterThanOrEqualTo("searchName", normalizedQuery)
                    .whereLessThanOrEqualTo("searchName", normalizedQuery + "\uf8ff")
                    .limit(50);

            QuerySnapshot globalSnapshot = globalQuery.get().get();
            List<Product> results = globalSnapshot.getDocuments().stream()
                    .map(this::documentToProduct)
                    .filter(Objects::nonNull).collect(Collectors.toList());

            // If trainerId is provided, also search custom products
            if (trainerId != null && !trainerId.isEmpty()) {
                Query customQuery = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("type", ProductType.CUSTOM.name())
                        .whereEqualTo("authorId", trainerId)
                        .whereGreaterThanOrEqualTo("searchName", normalizedQuery)
                        .whereLessThanOrEqualTo("searchName", normalizedQuery + "\uf8ff")
                        .limit(50);

                QuerySnapshot customSnapshot = customQuery.get().get();
                results.addAll(customSnapshot.getDocuments().stream()
                        .map(this::documentToProduct)
                        .filter(Objects::nonNull)
                        .toList());
            }

            return results;
        } catch (Exception e) {
            log.error("Failed to search products for query: {}", query, e);
            throw new RuntimeException("Failed to search products", e);
        }
    }

    public Optional<Product> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(documentToProduct(document));
        } catch (Exception e) {
            log.error("Failed to fetch product by id: {}", id, e);
            throw new RuntimeException("Failed to fetch product", e);
        }
    }

    public Product save(Product product) {
        try {
            DocumentReference docRef;
            if (product.getId() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(product.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                product.setId(docRef.getId());
            }

            if (product.getCreatedAt() == null) {
                product.setCreatedAt(System.currentTimeMillis());
            }

            Map<String, Object> data = productToMap(product);
            docRef.set(data).get();

            log.info("Product saved successfully: {}", product.getName());
            return product;
        } catch (Exception e) {
            log.error("Failed to save product: {}", product.getName(), e);
            throw new RuntimeException("Failed to save product", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
            log.info("Product deleted successfully: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete product with id: {}", id, e);
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    private Product documentToProduct(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        try {
            Map<String, Object> data = document.getData();
            if (data == null) {
                return null;
            }

            // Parse nutritional values
            NutritionalValues nutritionalValues = null;
            Object nutritionObj = data.get("nutritionalValues");
            if (nutritionObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nutritionMap = (Map<String, Object>) nutritionObj;
                nutritionalValues = NutritionalValues.builder()
                        .calories(getDoubleValue(nutritionMap, "calories"))
                        .protein(getDoubleValue(nutritionMap, "protein"))
                        .fat(getDoubleValue(nutritionMap, "fat"))
                        .carbs(getDoubleValue(nutritionMap, "carbs"))
                        .build();
            }

            return Product.builder()
                    .id(document.getId())
                    .name((String) data.get("name"))
                    .searchName((String) data.get("searchName"))
                    .defaultUnit((String) data.get("defaultUnit"))
                    .nutritionalValues(nutritionalValues)
                    .type(ProductType.valueOf((String) data.get("type")))
                    .authorId((String) data.get("authorId"))
                    .categoryId((String) data.get("categoryId"))
                    .createdAt(getLongValue(data))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse product document: {}", document.getId(), e);
            return null;
        }
    }

    private Map<String, Object> productToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", product.getName());
        map.put("searchName", product.getSearchName());
        map.put("defaultUnit", product.getDefaultUnit());
        map.put("type", product.getType().name());
        map.put("authorId", product.getAuthorId());
        map.put("categoryId", product.getCategoryId());
        map.put("createdAt", product.getCreatedAt());

        if (product.getNutritionalValues() != null) {
            Map<String, Object> nutritionMap = new HashMap<>();
            nutritionMap.put("calories", product.getNutritionalValues().getCalories());
            nutritionMap.put("protein", product.getNutritionalValues().getProtein());
            nutritionMap.put("fat", product.getNutritionalValues().getFat());
            nutritionMap.put("carbs", product.getNutritionalValues().getCarbs());
            map.put("nutritionalValues", nutritionMap);
        }

        return map;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private Long getLongValue(Map<String, Object> map) {
        Object value = map.get("createdAt");
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
