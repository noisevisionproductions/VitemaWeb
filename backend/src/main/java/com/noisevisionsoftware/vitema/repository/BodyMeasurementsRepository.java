package com.noisevisionsoftware.vitema.repository;

import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.measurements.FirestoreMeasurementsMapper;
import com.noisevisionsoftware.vitema.model.measurements.BodyMeasurements;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BodyMeasurementsRepository {
    private final Firestore firestore;
    private final FirestoreMeasurementsMapper firestoreMapper;
    private static final String COLLECTION_NAME = "bodyMeasurements";

    public List<BodyMeasurements> findByUserId(String userId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING);

            QuerySnapshot querySnapshot = query.get().get();
            return querySnapshot.getDocuments().stream()
                    .map(firestoreMapper::toBodyMeasurements)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch measurements for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch measurements", e);
        }
    }

    public Optional<BodyMeasurements> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(firestoreMapper.toBodyMeasurements(document));
        } catch (Exception e) {
            log.error("Failed to fetch measurement: {}", id, e);
            throw new RuntimeException("Failed to fetch measurement", e);
        }
    }

    public void save(BodyMeasurements measurements) {
        try {
            DocumentReference docRef;
            if (measurements.getId() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(measurements.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                measurements.setId(docRef.getId());
            }

            Map<String, Object> data = firestoreMapper.toFirestoreMap(measurements);
            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save measurement", e);
            throw new RuntimeException("Failed to save measurement", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (Exception e) {
            log.error("Failed to delete measurement: {}", id, e);
            throw new RuntimeException("Failed to delete measurement", e);
        }
    }
}