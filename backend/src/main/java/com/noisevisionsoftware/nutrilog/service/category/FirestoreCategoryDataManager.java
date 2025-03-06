package com.noisevisionsoftware.nutrilog.service.category;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.nutrilog.model.shopping.category.ProductCategoryData;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirestoreCategoryDataManager {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "product_categories_test";

    public Map<String, ProductCategoryData> loadData() {
        try {
            Map<String, ProductCategoryData> result = new ConcurrentHashMap<>();
            CollectionReference categoriesRef = firestore.collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> future = categoriesRef.get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                result.put(document.getId(), document.toObject(ProductCategoryData.class));
            }

            return result;
        } catch (Exception e) {
            log.error("Error loading category data from Firestore", e);
            return new ConcurrentHashMap<>();
        }
    }

    public void saveData(Map<String, ProductCategoryData> data) {
        try {
            WriteBatch batch = firestore.batch();
            int batchSize = 0;
            final int MAX_BATCH_SIZE = 500;

            for (Map.Entry<String, ProductCategoryData> entry : data.entrySet()) {
                String docId = createDocumentId(entry.getKey());
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(docId);
                batch.set(docRef, entry.getValue());
                batchSize++;

                if (batchSize >= MAX_BATCH_SIZE) {
                    batch.commit().get();
                    batch = firestore.batch();
                    batchSize = 0;
                }
            }

            if (batchSize > 0) {
                batch.commit().get();
            }

        } catch (Exception e) {
            log.error("Error saving category data to Firestore", e);
            throw new RuntimeException("Could not save category data", e);
        }
    }

    public ParsedProduct updateProduct(ParsedProduct oldProduct, ParsedProduct newProduct) {
        try {
            String oldNormalizedName = normalizeProductName(oldProduct.getName());
            String newNormalizedName = normalizeProductName(newProduct.getName());

            QuerySnapshot existingDocs = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("productName", oldNormalizedName)
                    .get()
                    .get();

            if (!existingDocs.isEmpty()) {
                DocumentSnapshot doc = existingDocs.getDocuments().getFirst();
                DocumentReference docRef = doc.getReference();

                List<String> variations = getVariationsFromDocument(doc);

                if (!variations.contains(newProduct.getOriginal().toLowerCase())) {
                    variations.add(newProduct.getOriginal().toLowerCase());
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("productName", newNormalizedName);
                updates.put("variations", variations);
                updates.put("updatedAt", Timestamp.now());

                docRef.update(updates).get();
            }

            return newProduct;
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji produktu: {}", e.getMessage());
            throw new RuntimeException("Nie udało się zaktualizować produktu", e);
        }
    }

    private List<String> getVariationsFromDocument(DocumentSnapshot doc) {
        Object variationsObj = doc.get("variations");
        List<String> variations = new ArrayList<>();

        if (variationsObj instanceof List<?>) {
            try {
                // Sprawdź każdy element listy
                for (Object item : (List<?>) variationsObj) {
                    if (item instanceof String) {
                        variations.add((String) item);
                    }
                }
            } catch (Exception e) {
                log.error("Error converting variations list: {}", e.getMessage());
            }
        }

        return variations;
    }

    private String createDocumentId(String productName) {
        if (productName.length() > 40) {
            String prefix = productName.substring(0, 20).toLowerCase()
                    .replaceAll("[^a-z0-9]", "_");
            return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
        }

        return productName.toLowerCase()
                .replaceAll("[^a-z0-9ąćęłńóśźż]", "_")
                .replaceAll("_+", "_")
                .trim();
    }

    /**
     * Normalizuje nazwę produktu usuwając jednostki miary, liczby i znaki specjalne.
     *
     * @param name Nazwa produktu do znormalizowania
     * @return Znormalizowana nazwa produktu
     */
    private String normalizeProductName(String name) {
        if (name == null) {
            return "";
        }

        return name.toLowerCase()
                .replaceAll("\\d+(?:[.,]\\d+)?\\s*(?:g|kg|ml|l|szt|sztuk|sztuki|bochenek|opakowanie|opak|porcja|porcji)\\b", "")

                // Usuń same liczby
                .replaceAll("\\d+", "")

                // Zostaw tylko litery (w tym polskie), cyfry i spacje
                .replaceAll("[^a-ząćęłńóśźż\\s]", " ")

                // Zamień wielokrotne spacje na pojedynczą
                .replaceAll("\\s+", " ")

                // Usuń spacje z początku i końca
                .trim();
    }
}