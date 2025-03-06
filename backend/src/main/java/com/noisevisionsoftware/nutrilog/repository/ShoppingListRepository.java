package com.noisevisionsoftware.nutrilog.repository;

import com.google.cloud.firestore.*;
import com.noisevisionsoftware.nutrilog.mapper.shopping.FirestoreShoppingMapper;
import com.noisevisionsoftware.nutrilog.model.shopping.ShoppingList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ShoppingListRepository {
    private final Firestore firestore;
    private final FirestoreShoppingMapper firestoreShoppingMapper;
    private static final String COLLECTION_NAME = "shopping_lists";

    public Optional<ShoppingList> findByDietId(String dietId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("dietId", dietId);
            QuerySnapshot querySnapshot = query.get().get();

            if (querySnapshot.isEmpty()) {
                return Optional.empty();
            }

            return Optional.ofNullable(firestoreShoppingMapper.toShoppingList(querySnapshot.getDocuments().getFirst()));
        } catch (Exception e) {
            log.error("Failed to fetch shopping list by dietId: {}", dietId, e);
            throw new RuntimeException("Failed to fetch shopping list", e);
        }
    }


    public Optional<ShoppingList> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();

            if (!document.exists()) {
                return Optional.empty();
            }

            return Optional.ofNullable(firestoreShoppingMapper.toShoppingList(document));
        } catch (Exception e) {
            log.error("Failed to fetch shopping list by id: {}", id, e);
            throw new RuntimeException("Failed to fetch shopping list", e);
        }
    }

    // Zaktualizujmy też metodę save, aby obsługiwała aktualizacje
    public ShoppingList save(ShoppingList shoppingList) {
        try {
            DocumentReference docRef;
            if (shoppingList.getId() != null) {
                // Aktualizacja istniejącego dokumentu
                docRef = firestore.collection(COLLECTION_NAME).document(shoppingList.getId());
            } else {
                // Tworzenie nowego dokumentu
                docRef = firestore.collection(COLLECTION_NAME).document();
                shoppingList.setId(docRef.getId());
            }

            Map<String, Object> data = firestoreShoppingMapper.toFirestoreMap(shoppingList);
            docRef.set(data).get();

            // Pobierz zaktualizowany dokument i zwróć go
            DocumentSnapshot updatedDoc = docRef.get().get();
            return firestoreShoppingMapper.toShoppingList(updatedDoc);
        } catch (Exception e) {
            log.error("Failed to save shopping list", e);
            throw new RuntimeException("Failed to save shopping list", e);
        }
    }
}