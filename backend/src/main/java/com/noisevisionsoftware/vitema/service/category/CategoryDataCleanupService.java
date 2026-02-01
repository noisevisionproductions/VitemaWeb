package com.noisevisionsoftware.vitema.service.category;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryDataCleanupService {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "product_categories";

    /*
     * Clean product duplicates from Firestore
     * Leaves document with the newest update date
     * */
    public void cleanupDuplicates() {
        try {
            log.info("Rozpoczynanie czyszczenia duplikatów produktów...");

            Map<String, List<DocumentSnapshot>> productGroups = groupProductsByName();
            int duplicatesRemoved = 0;

            WriteBatch batch = firestore.batch();
            int batchSize = 0;

            for (Map.Entry<String, List<DocumentSnapshot>> entry : productGroups.entrySet()) {
                String productName = entry.getKey();
                List<DocumentSnapshot> documents = entry.getValue();

                if (documents.size() > 1) {
                    log.info("Znaleziono {} duplikatów dla produktu: {}", documents.size(), productName);

                    // Sortuj według daty aktualizacji (najnowsze na początku)
                    documents.sort((a, b) -> {
                        Timestamp timeA = a.getTimestamp("updatedAt");
                        Timestamp timeB = b.getTimestamp("updatedAt");
                        if (timeA == null && timeB == null) return 0;
                        if (timeA == null) return 1;
                        if (timeB == null) return -1;
                        return timeB.compareTo(timeA);
                    });

                    // Zachowaj pierwszy (najnowszy), usuń pozostałe
                    DocumentSnapshot toKeep = documents.getFirst();
                    List<String> allVariations = new ArrayList<>();
                    int totalUsageCount = 0;

                    // Zbierz wszystkie wariacje i usage count z duplikatów
                    for (DocumentSnapshot doc : documents) {
                        List<String> variations = getVariationsFromDocument(doc);
                        for (String variation : variations) {
                            if (!allVariations.contains(variation)) {
                                allVariations.add(variation);
                            }
                        }

                        Long usageCount = doc.getLong("usageCount");
                        if (usageCount != null) {
                            totalUsageCount += usageCount.intValue();
                        }
                    }

                    // Aktualizuj dokument do zachowania
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("variations", allVariations);
                    updates.put("usageCount", totalUsageCount);
                    updates.put("updatedAt", Timestamp.now());

                    batch.update(toKeep.getReference(), updates);
                    batchSize++;

                    // Usuń duplikaty
                    for (int i = 1; i < documents.size(); i++) {
                        batch.delete(documents.get(i).getReference());
                        batchSize++;
                        duplicatesRemoved++;

                        if (batchSize >= 450) {
                            batch.commit().get();
                            batch = firestore.batch();
                            batchSize = 0;
                        }
                    }
                }
            }

            if (batchSize > 0) {
                batch.commit().get();
            }

            log.info("Czyszczenie duplikatów zakończone. Usunięto {} dokumentów.", duplicatesRemoved);

        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia duplikatów", e);
            throw new RuntimeException("Nie udało się wyczyścić duplikatów", e);
        }
    }

    private Map<String, List<DocumentSnapshot>> groupProductsByName() throws Exception {
        Map<String, List<DocumentSnapshot>> groups = new HashMap<>();

        QuerySnapshot allDocs = firestore.collection(COLLECTION_NAME).get().get();

        for (DocumentSnapshot doc : allDocs.getDocuments()) {
            String productName = doc.getString("productName");
            if (productName != null) {
                String normalizedName = normalizeProductName(productName);
                groups.computeIfAbsent(normalizedName, k -> new ArrayList<>()).add(doc);
            }
        }

        return groups;
    }

    private List<String> getVariationsFromDocument(DocumentSnapshot doc) {
        Object variationsObj = doc.get("variations");
        List<String> variations = new ArrayList<>();

        if (variationsObj instanceof List<?>) {
            for (Object item : (List<?>) variationsObj) {
                if (item instanceof String) {
                    variations.add((String) item);
                }
            }
        }

        return variations;
    }

    private String normalizeProductName(String name) {
        if (name == null) return "";

        return name.toLowerCase()
                .replaceAll("\\d+(?:[.,]\\d+)?\\s*(?:g|kg|ml|l|szt|sztuk|sztuki|bochenek|opakowanie|opak|porcja|porcji)\\b", "")
                .replaceAll("\\d+", "")
                .replaceAll("[^a-ząćęłńóśźż\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}