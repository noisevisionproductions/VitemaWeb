package com.noisevisionsoftware.vitema.service.newsletter;

import com.noisevisionsoftware.vitema.dto.request.newsletter.SubscriptionRequest;
import com.noisevisionsoftware.vitema.exception.TooManyRequestsException;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscribersMetadata;
import com.noisevisionsoftware.vitema.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicNewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final EmailService emailService;

    /**
     * Zapisuje nowego subskrybenta do newslettera
     */
    @CacheEvict(value = {"newsletterSubscribers", "newsletterStats"}, allEntries = true)
    @Transactional
    public void subscribe(SubscriptionRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        Optional<NewsletterSubscriber> existingOpt = subscriberRepository.findByEmail(normalizedEmail);

        if (existingOpt.isPresent()) {
            NewsletterSubscriber subscriber = existingOpt.get();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastEmailSent = subscriber.getLastEmailSent();

            if (lastEmailSent != null &&
                    Duration.between(lastEmailSent, now).toMinutes() < 15) {
                throw new TooManyRequestsException("Wysłano już mail weryfikacyjny. Sprawdź swoją skrzynkę lub spróbuj ponownie za kilka minut.");
            }

            if (!subscriber.isVerified()) {
                subscriber.setLastEmailSent(now);
                subscriberRepository.save(subscriber);

                emailService.sendVerificationEmail(subscriber);
                return;
            }

            throw new IllegalStateException("Email już istnieje w bazie newslettera");
        }

        SubscriberRole role = "dietetyk".equals(request.getRole())
                ? SubscriberRole.DIETITIAN
                : SubscriberRole.COMPANY;

        NewsletterSubscriber subscriber = NewsletterSubscriber.create(normalizedEmail, role);
        subscriber.setLastEmailSent(LocalDateTime.now());

        subscriberRepository.save(subscriber);

        emailService.sendVerificationEmail(subscriber);
    }

    /**
     * Weryfikuje email subskrybenta za pomocą tokenu i zwraca obiekt subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public NewsletterSubscriber verifySubscriberAndGet(String token) {
        Optional<NewsletterSubscriber> subscriberOpt = subscriberRepository.findByVerificationToken(token);

        if (subscriberOpt.isEmpty()) {
            return null;
        }

        NewsletterSubscriber subscriber = subscriberOpt.get();

        if (subscriber.isVerified()) {
            return subscriber;
        }

        try {
            subscriber.setVerified(true);
            subscriber.setVerifiedAt(LocalDateTime.now());
            subscriberRepository.save(subscriber);

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
    @Transactional
    public boolean unsubscribe(String email) {
        Optional<NewsletterSubscriber> subscriberOpt = subscriberRepository.findByEmail(email);

        if (subscriberOpt.isEmpty()) {
            return false;
        }

        NewsletterSubscriber subscriber = subscriberOpt.get();
        subscriber.setActive(false);
        subscriberRepository.save(subscriber);

        return true;
    }

    /**
     * Aktualizuje metadane subskrybenta-dostępne dla zweryfikowanego subskrybenta
     * (np. do zapisywania wyników ankiety)
     */
    @Transactional
    public void updateSubscriberMetadata(Long id, Map<String, String> newMetadata) {
        Optional<NewsletterSubscriber> subscriberOpt = subscriberRepository.findById(id);
        if (subscriberOpt.isEmpty()) {
            throw new IllegalArgumentException("Subskrybent o podanym ID nie istnieje");
        }

        NewsletterSubscriber subscriber = subscriberOpt.get();
        log.info("Aktualizacja metadanych dla subskrybenta: {}", subscriber.getEmail());

        if (subscriber.getMetadataEntries() == null) {
            subscriber.setMetadataEntries(new HashSet<>());
        }

        // Aktualizuj metadane
        for (Map.Entry<String, String> entry : newMetadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Szukaj istniejącej metadanej o tym kluczu
            boolean found = false;
            for (NewsletterSubscribersMetadata metadataEntry : subscriber.getMetadataEntries()) {
                if (metadataEntry.getKey().equals(key)) {
                    metadataEntry.setValue(value);
                    found = true;
                    break;
                }
            }

            // Jeśli nie znaleziono, utwórz nową
            if (!found) {
                NewsletterSubscribersMetadata newEntry = new NewsletterSubscribersMetadata();
                newEntry.setSubscriber(subscriber);
                newEntry.setKey(key);
                newEntry.setValue(value);
                subscriber.getMetadataEntries().add(newEntry);
            }
        }

        // Zapisz zmiany
        log.info("Zapis subskrybenta z {} metadanymi", subscriber.getMetadataEntries().size());
        subscriberRepository.save(subscriber);
    }
}