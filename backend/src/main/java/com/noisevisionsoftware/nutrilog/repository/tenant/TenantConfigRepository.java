package com.noisevisionsoftware.nutrilog.repository.tenant;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.noisevisionsoftware.nutrilog.model.TenantConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TenantConfigRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "tenant_configs";

    public TenantConfig save(TenantConfig config) {
        try {
            DocumentReference docRef;
            if (config.getId() != null) {
                docRef = firestore.collection(COLLECTION_NAME).document(config.getId());
            } else {
                docRef = firestore.collection(COLLECTION_NAME).document();
                config.setId(docRef.getId());
            }
            docRef.set(config).get();
            return config;
        } catch (Exception e) {
            log.error("Error saving tenant config", e);
            throw new RuntimeException("Failed to save tenant config", e);
        }
    }

    public Optional<TenantConfig> findById(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            DocumentSnapshot snapshot = docRef.get().get();
            if (snapshot.exists()) {
                return Optional.ofNullable(snapshot.toObject(TenantConfig.class));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding tenant config by id", e);
            throw new RuntimeException("Failed to find tenant config", e);
        }
    }

    public Optional<TenantConfig> findByEmail(String email) {
        try {
            Query query = firestore.collection(COLLECTION_NAME).whereEqualTo("email", email);
            QuerySnapshot querySnapshot = query.get().get();
            if (!querySnapshot.isEmpty()) {
                return Optional.of(querySnapshot.getDocuments().getFirst().toObject(TenantConfig.class));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding tenant config by email", e);
            throw new RuntimeException("Failed to find tenant config", e);
        }
    }

    public List<TenantConfig> findAllDemoAccounts() {
        try {
            Query query = firestore.collection(COLLECTION_NAME).whereEqualTo("isDemoAccount", true);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            return querySnapshot.get().getDocuments().stream()
                    .map(doc -> doc.toObject(TenantConfig.class))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding all demo accounts", e);
            throw new RuntimeException("Failed to find demo accounts", e);
        }
    }

    public void delete(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (Exception e) {
            log.error("Error deleting tenant config", e);
            throw new RuntimeException("Failed to delete tenant config", e);
        }
    }
}
