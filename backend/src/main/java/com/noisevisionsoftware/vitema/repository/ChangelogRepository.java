package com.noisevisionsoftware.vitema.repository;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.changelog.FirestoreChangelogMapper;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ChangelogRepository {
    private final Firestore firestore;
    private final FirestoreChangelogMapper firestoreMapper;
    private static final String COLLECTION_NAME = "changelog";

    public List<ChangelogEntry> findAll() {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .orderBy("createdAt", Query.Direction.DESCENDING);

            QuerySnapshot snapshot = query.get().get();
            return snapshot.getDocuments().stream()
                    .map(firestoreMapper::toChangelogEntry)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch changelog entries", e);
            throw new RuntimeException("Failed to fetch changelog entries", e);
        }
    }

    public void save(ChangelogEntry entry) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            entry.setId(docRef.getId());

            Map<String, Object> data = firestoreMapper.toFirestoreMap(entry);
            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save changelog entry", e);
            throw new RuntimeException("Failed to save changelog entry", e);
        }
    }

    public void updateUserSettings(String userId, Timestamp lastRead) {
        try {
            DocumentReference docRef = firestore.collection("userSettings").document(userId);
            docRef.set(Map.of("lastChangelogRead", lastRead), SetOptions.merge()).get();
        } catch (Exception e) {
            log.error("Failed to update user settings", e);
            throw new RuntimeException("Failed to update user settings", e);
        }
    }

    public Timestamp getLastReadTimestamp(String userId) {
        try {
            DocumentReference docRef = firestore.collection("userSettings").document(userId);
            DocumentSnapshot doc = docRef.get().get();

            if (doc.exists() && doc.get("lastChangelogRead") != null) {
                return (Timestamp) doc.get("lastChangelogRead");
            }
            return Timestamp.ofTimeSecondsAndNanos(0, 0);
        } catch (Exception e) {
            log.error("Failed to get last read timestamp", e);
            throw new RuntimeException("Failed to get last read timestamp", e);
        }
    }
}