package com.noisevisionsoftware.nutrilog.service.newsletter;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.noisevisionsoftware.nutrilog.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.nutrilog.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.nutrilog.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminNewsletterService {

    private final Firestore firestore;
    private final EmailService emailService;
    private static final String COLLECTION_NAME = "newsletter_subscribers";

    protected CollectionReference getCollection() {
        return firestore.collection(COLLECTION_NAME);
    }

    /**
     * Pobiera wszystkich aktywnych i zweryfikowanych subskrybentów
     */
    @Cacheable("newsletterSubscribers")
    public List<NewsletterSubscriber> getAllActiveSubscribers() throws ExecutionException, InterruptedException {
        return getCollection()
                .whereEqualTo("active", true)
                .whereEqualTo("verified", true)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(NewsletterSubscriber.class))
                .collect(Collectors.toList());
    }

    /**
     * Pobiera wszystkich subskrybentów
     */
    public List<NewsletterSubscriber> getAllSubscribers() throws ExecutionException, InterruptedException {
        return getCollection()
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(NewsletterSubscriber.class))
                .collect(Collectors.toList());
    }

    /**
     * Dezaktywuje subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public void deactivateSubscriber(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", false).get();
    }

    /**
     * Aktywuje subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public void activateSubscriber(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).update("active", true).get();
    }

    /**
     * Ręcznie weryfikuje subskrybenta (przez admina)
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public void verifySubscriberManually(String id) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("verified", true);
        updates.put("verifiedAt", Timestamp.now());

        getCollection().document(id).update(updates).get();
    }

    /**
     * Aktualizuje metadane subskrybenta (dla admina)
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public void updateSubscriberMetadata(String id, Map<String, String> newMetadata) throws ExecutionException, InterruptedException {
        NewsletterSubscriber subscriber = getCollection().document(id).get().get().toObject(NewsletterSubscriber.class);

        if (subscriber != null) {
            Map<String, String> metadata = subscriber.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }

            metadata.putAll(newMetadata);
            getCollection().document(id).update("metadata", metadata).get();
        }
    }

    /**
     * Usuwa subskrybenta całkowicie z bazy danych
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    public void deleteSubscriber(String id) throws ExecutionException, InterruptedException {
        getCollection().document(id).delete().get();
    }

    /**
     * Wysyła wiadomość do wszystkich aktywnych i zweryfikowanych subskrybentów
     */
    public void sendBulkEmail(String subject, String content) {
        try {
            List<NewsletterSubscriber> subscribers = getAllActiveSubscribers();

            for (NewsletterSubscriber subscriber : subscribers) {
                emailService.sendCustomEmail(subscriber.getEmail(), subject, content);
            }
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas wysyłania masowego emaila", e);
        }
    }

    /**
     * Pobiera statystyki newslettera
     */
    @Cacheable("newsletterStats")
    public Map<String, Object> getNewsletterStats() throws ExecutionException, InterruptedException {
        List<NewsletterSubscriber> allSubscribers = getAllSubscribers();

        long totalCount = allSubscribers.size();
        long verifiedCount = allSubscribers.stream().filter(NewsletterSubscriber::isVerified).count();
        long activeCount = allSubscribers.stream().filter(NewsletterSubscriber::isActive).count();
        long activeVerifiedCount = allSubscribers.stream()
                .filter(s -> s.isActive() && s.isVerified())
                .count();

        Map<SubscriberRole, Long> roleDistribution = allSubscribers.stream()
                .collect(Collectors.groupingBy(
                        NewsletterSubscriber::getRole,
                        Collectors.counting()
                ));

        return Map.of(
                "total", totalCount,
                "verified", verifiedCount,
                "active", activeCount,
                "activeVerified", activeVerifiedCount,
                "roleDistribution", roleDistribution
        );
    }
}