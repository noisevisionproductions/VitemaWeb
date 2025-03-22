package com.noisevisionsoftware.nutrilog.service.newsletter;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.noisevisionsoftware.nutrilog.exception.TooManyRequestsException;
import com.noisevisionsoftware.nutrilog.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.nutrilog.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.nutrilog.dto.request.newsletter.SubscriptionRequest;
import com.noisevisionsoftware.nutrilog.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicNewsletterService {

    private final Firestore firestore;
    private final EmailService emailService;
    private static final String COLLECTION_NAME = "newsletter_subscribers";

    protected CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    /**
     * Zapisuje nowego subskrybenta do newslettera
     */
    @CacheEvict(value = {"newsletterSubscribers", "newsletterStats"}, allEntries = true)
    public void subscribe(SubscriptionRequest request) throws ExecutionException, InterruptedException {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        List<QueryDocumentSnapshot> existing = getCollection()
                .whereEqualTo("email", normalizedEmail)
                .get().get().getDocuments();

        if (!existing.isEmpty()) {
            if (existing.size() > 1) {
                existing.sort((doc1, doc2) -> {
                    Timestamp ts1 = doc1.getTimestamp("createdAt");
                    Timestamp ts2 = doc2.getTimestamp("createdAt");
                    if (ts2 != null && ts1 != null) {
                        return ts2.compareTo(ts1);
                    }
                    return 0;
                });

                for (int i = 1; i < existing.size(); i++) {
                    getCollection().document(existing.get(i).getId()).delete().get();
                }
            }

            NewsletterSubscriber subscriber = existing.getFirst().toObject(NewsletterSubscriber.class);

            Timestamp now = Timestamp.now();
            Timestamp lastEmailSent = subscriber.getLastEmailSent();

            if (lastEmailSent != null &&
                    now.getSeconds() - lastEmailSent.getSeconds() < 15 * 60) {
                throw new TooManyRequestsException("Wysłano już mail weryfikacyjny. Sprawdź swoją skrzynkę lub spróbuj ponownie za kilka minut.");
            }

            if (!subscriber.isVerified()) {
                subscriber.setLastEmailSent(now);
                getCollection().document(subscriber.getId()).update("lastEmailSent", now).get();

                emailService.sendVerificationEmail(subscriber);
                return;
            }

            throw new IllegalStateException("Email już istnieje w bazie newslettera");
        }

        SubscriberRole role = "dietetyk".equals(request.getRole())
                ? SubscriberRole.DIETITIAN
                : SubscriberRole.COMPANY;

        NewsletterSubscriber subscriber = NewsletterSubscriber.create(normalizedEmail, role);
        subscriber.setLastEmailSent(Timestamp.now());

        getCollection().document(subscriber.getId()).set(subscriber).get();

        emailService.sendVerificationEmail(subscriber);
    }

    /**
     * Weryfikuje email subskrybenta za pomocą tokenu i zwraca obiekt subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public NewsletterSubscriber verifySubscriberAndGet(String token) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = getCollection()
                .whereEqualTo("verificationToken", token)
                .get().get().getDocuments();

        if (documents.isEmpty()) {
            return null;
        }

        QueryDocumentSnapshot document = documents.getFirst();
        NewsletterSubscriber subscriber = document.toObject(NewsletterSubscriber.class);

        if (subscriber.isVerified()) {
            return subscriber;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("verified", true);
        updates.put("verifiedAt", Timestamp.now());

        try {
            getCollection().document(subscriber.getId()).update(updates).get();
            emailService.sendWelcomeEmail(subscriber);
            return subscriber;
        } catch (Exception e) {
            log.error("Błąd podczas weryfikacji subskrybenta: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Wypisuje użytkownika z newslettera
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public boolean unsubscribe(String email) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documents = getCollection()
                .whereEqualTo("email", email)
                .get().get().getDocuments();

        if (documents.isEmpty()) {
            return false;
        }

        QueryDocumentSnapshot document = documents.getFirst();
        NewsletterSubscriber subscriber = document.toObject(NewsletterSubscriber.class);

        subscriber.setActive(false);
        getCollection().document(subscriber.getId()).set(subscriber).get();

        return true;
    }

    /**
     * Aktualizuje metadane subskrybenta-dostępne dla zweryfikowanego subskrybenta
     * (np. do zapisywania wyników ankiety)
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public void updateSubscriberMetadata(String id, Map<String, String> newMetadata) throws ExecutionException, InterruptedException {
        DocumentSnapshot document = getCollection().document(id).get().get();

        if (document == null || !document.exists()) {
            throw new IllegalArgumentException("Subskrybent o podanym ID nie istnieje");
        }

        NewsletterSubscriber subscriber = document.toObject(NewsletterSubscriber.class);

        if (subscriber != null) {
            Map<String, String> metadata = subscriber.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }

            metadata.putAll(newMetadata);
            getCollection().document(id).update("metadata", metadata).get();

            log.info("Zaktualizowano metadane subskrybenta: {}", id);
        } else {
            log.error("Błąd podczas aktualizacji metadanych - nie można przekształcić dokumentu");
            throw new IllegalStateException("Nie można zaktualizować metadanych subskrybenta");
        }
    }
}