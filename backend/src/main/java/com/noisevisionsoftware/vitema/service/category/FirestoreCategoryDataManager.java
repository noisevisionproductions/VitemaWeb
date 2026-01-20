package com.noisevisionsoftware.vitema.service.category;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.model.shopping.category.ProductCategoryData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
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
    private static final String COLLECTION_NAME = "product_categories";

    public Map<String, ProductCategoryData> loadData() {
        try {
            Map<String, ProductCategoryData> result = new ConcurrentHashMap<>();
            CollectionReference categoriesRef = firestore.collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> future = categoriesRef.get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                ProductCategoryData data = document.toObject(ProductCategoryData.class);

                String normalizedKey = normalizeProductName(data.getProductName());
                result.put(normalizedKey, data);
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
                String normalizedName = entry.getKey();
                ProductCategoryData productData = entry.getValue();

                // Check if document already exists
                String existingDocId = findExistingDocumentId(normalizedName);

                DocumentReference docRef;
                if (existingDocId != null) {
                    // Update current document
                    docRef = firestore.collection(COLLECTION_NAME).document(existingDocId);
                } else {
                    // Create new document with determined ID
                    String docId = createDeterministicDocumentId(normalizedName);
                    docRef = firestore.collection(COLLECTION_NAME).document(docId);
                }

                batch.set(docRef, productData, SetOptions.merge());
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

    public ParsedProduct updateProduct(ParsedProduct newProduct) {
        try {
            String normalizedName = normalizeProductName(newProduct.getName());
            String existingDocId = findExistingDocumentId(normalizedName);

            DocumentReference docRef;
            if (existingDocId != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(existingDocId);
            } else {
                String docId = createDeterministicDocumentId(normalizedName);
                docRef = firestore.collection(COLLECTION_NAME).document(docId);
            }

            DocumentSnapshot doc = docRef.get().get();
            List<String> variations = new ArrayList<>();

            if (doc.exists()) {
                variations = getVariationsFromDocument(doc);
            }

            String newVariation = newProduct.getOriginal().toLowerCase().trim();
            if (!variations.contains(newVariation)) {
                variations.add(newVariation);
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("productName", normalizedName);
            updates.put("variations", variations);
            updates.put("updatedAt", Timestamp.now());

            if (!doc.exists()) {
                updates.put("createdAt", Timestamp.now());
                updates.put("usageCount", 1);
                updates.put("categoryId", newProduct.getCategoryId());
            }

            docRef.set(updates, SetOptions.merge()).get();

            return newProduct;
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji produktu: {}", e.getMessage());
            throw new RuntimeException("Nie udało się zaktualizować produktu", e);
        }
    }

    /*
     * Finds ID of existing document for given normalized product name
     * */
    private String findExistingDocumentId(String normalizedName) {
        try {
            // Search based on productName
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("productName", normalizedName)
                    .limit(1)
                    .get()
                    .get();

            if (!querySnapshot.isEmpty()) {
                return querySnapshot.getDocuments().getFirst().getId();
            }

            String deterministicId = createDeterministicDocumentId(normalizedName);
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(deterministicId)
                    .get()
                    .get();

            if (doc.exists()) {
                return deterministicId;
            }

            return null;
        } catch (Exception e) {
            log.error("Error finding existing document for: {}", normalizedName, e);
            return null;
        }
    }

    /*
     * Creates deterministic document ID based on normalized name
     * */
    private String createDeterministicDocumentId(String normalizedName) {
        if (normalizedName == null || normalizedName.trim().isEmpty()) {
            return "unnamed_product_" + UUID.randomUUID().toString().substring(0, 8);
        }

        String cleanId = normalizedName
                .replaceAll("[^a-z0-9ąćęłńóśźż]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .trim();

        if (cleanId.isEmpty()) {
            return "product_" + UUID.randomUUID().toString().substring(0, 8);
        }

        if (cleanId.length() > 30) {
            cleanId = cleanId.substring(0, 30);
        }

        return cleanId;
    }

    private List<String> getVariationsFromDocument(DocumentSnapshot doc) {
        Object variationsObj = doc.get("variations");
        List<String> variations = new ArrayList<>();

        if (variationsObj instanceof List<?>) {
            try {
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

    /**
     * Normalizuje nazwę produktu - ta sama logika co w ProductCategorizationService
     */
    private String normalizeProductName(String name) {
        if (name == null) {
            return "";
        }

        return name.toLowerCase()
                .replaceAll("\\d+(?:[.,]\\d+)?\\s*(?:g|kg|ml|l|szt|sztuk|sztuki|bochenek|opakowanie|opak|porcja|porcji)\\b", "")
                .replaceAll("\\d+", "")
                .replaceAll("[^a-ząćęłńóśźż\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}