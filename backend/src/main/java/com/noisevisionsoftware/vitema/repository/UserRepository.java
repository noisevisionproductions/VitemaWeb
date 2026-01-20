package com.noisevisionsoftware.vitema.repository;

import com.google.cloud.firestore.*;
import com.noisevisionsoftware.vitema.mapper.user.FirestoreUserMapper;
import com.noisevisionsoftware.vitema.model.user.User;
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
public class UserRepository {
    private final Firestore firestore;
    private final FirestoreUserMapper firestoreUserMapper;
    private static final String COLLECTION_NAME = "users";

    public List<User> findAll() {
        try {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME).get().get();
            return snapshot.getDocuments().stream()
                    .map(firestoreUserMapper::toUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch users", e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }

    public Optional<User> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot document = docRef.get().get();
            return Optional.ofNullable(firestoreUserMapper.toUser(document));
        } catch (Exception e) {
            log.error("Failed to fetch user by id: {}", id, e);
            throw new RuntimeException("Failed to fetch user", e);
        }
    }

    public User save(User user) {
        try {
            DocumentReference docRef;
            if (user.getId() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(user.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                user.setId(docRef.getId());
            }

            Map<String, Object> data = firestoreUserMapper.toFirestoreMap(user);
            docRef.set(data).get();
        } catch (Exception e) {
            log.error("Failed to save user", e);
            throw new RuntimeException("Failed to save user", e);
        }
        return user;
    }

    public void update(String id, User user) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            Map<String, Object> data = firestoreUserMapper.toFirestoreMap(user);
            docRef.update(data).get();
        } catch (Exception e) {
            log.error("Failed to update user with id: {}", id, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
}