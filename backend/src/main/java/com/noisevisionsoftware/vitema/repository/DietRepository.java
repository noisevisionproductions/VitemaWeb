package com.noisevisionsoftware.vitema.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.diet.FirestoreDietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DietRepository {
    private final Firestore firestore;
    private final FirestoreDietMapper firestoreDietMapper;
    private static final String COLLECTION_NAME = "diets";

    public Diet save(Diet diet) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            Map<String, Object> data = firestoreDietMapper.toFirestoreMap(diet);
            ApiFuture<WriteResult> result = docRef.set(data);
            result.get();
            diet.setId(docRef.getId());
            return diet;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save diet", e);
        }
    }

    public Diet update(String id, Diet diet) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            Map<String, Object> data = firestoreDietMapper.toFirestoreMap(diet);
            ApiFuture<WriteResult> result = docRef.update(data);
            result.get();
            diet.setId(id);
            return diet;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update diet", e);
        }
    }

    public Optional<Diet> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(firestoreDietMapper.toDiet(document));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch diet", e);
        }
    }

    public List<Diet> findByUserId(String userId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId);
            QuerySnapshot querySnapshot = query.get().get();

            return querySnapshot.getDocuments().stream()
                    .map(firestoreDietMapper::toDiet)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch diets by userId", e);
        }
    }

    public void delete(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            docRef.delete().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete diet", e);
        }
    }

    public List<Diet> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            QuerySnapshot snapshot = future.get();

            return snapshot.getDocuments().stream()
                    .map(firestoreDietMapper::toDiet)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all diets", e);
        }
    }

    public List<Diet> findAllPaginated(int page, int size) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .offset(page * size)
                    .limit(size);

            QuerySnapshot querySnapshot = query.get().get();

            return querySnapshot.getDocuments().stream()
                    .map(firestoreDietMapper::toDiet)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch paginated diets", e);
            throw new RuntimeException("Failed to fetch paginated diets", e);
        }
    }
}