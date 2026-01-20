package com.noisevisionsoftware.vitema.service.newsletter;

import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.model.newsletter.SubscriberRole;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.NewsletterSubscriberRepository;
import com.noisevisionsoftware.vitema.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminNewsletterService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final EmailService emailService;

    /**
     * Pobiera wszystkich aktywnych i zweryfikowanych subskrybentów
     */
    @Cacheable("newsletterSubscribers")
    @Transactional(readOnly = true)
    public List<NewsletterSubscriber> getAllActiveSubscribers() {
        return subscriberRepository.findAllByActiveTrueAndVerifiedTrue();
    }

    /**
     * Pobiera wszystkich subskrybentów
     */
    @Transactional(readOnly = true)
    public List<NewsletterSubscriber> getAllSubscribers(){
        return subscriberRepository.findAll();
    }

    /**
     * Dezaktywuje subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public void deactivateSubscriber(Long id) {
        subscriberRepository.findById(id).ifPresent(subscriber ->{
            subscriber.setActive(false);
            subscriberRepository.save(subscriber);
        });
    }

    /**
     * Aktywuje subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public void activateSubscriber(Long id) {
        subscriberRepository.findById(id).ifPresent(subscriber -> {
            subscriber.setActive(true);
            subscriberRepository.save(subscriber);
        });
    }

    /**
     * Ręcznie weryfikuje subskrybenta (przez admina)
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public void verifySubscriberManually(Long id){
        subscriberRepository.findById(id).ifPresent(subscriber ->{
            subscriber.setVerified(true);
            subscriber.setVerifiedAt(LocalDateTime.now());
            subscriberRepository.save(subscriber);
        });
    }

    /**
     * Aktualizuje metadane subskrybenta (dla admina)
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public void updateSubscriberMetadata(Long id, Map<String, String> newMetadata) {
        subscriberRepository.findById(id).ifPresent(subscriber -> {
            if (subscriber.getMetadataEntries() == null) {
                subscriber.setMetadataEntries(new HashSet<>());
            }

            Map<String, String> metadata = subscriber.getMetadata();
            if (metadata == null) {
                metadata = new HashMap<>();
            }

            metadata.putAll(newMetadata);
            subscriber.setMetadata(metadata);

            subscriber.updateMetadata();

            subscriberRepository.save(subscriber);
        });
    }

    /**
     * Aktualizuje datę ostatniego wysłanego emaila dla subskrybenta
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public void updateLastEmailSent(String email){
        subscriberRepository.findByEmail(email).ifPresent(subscriber -> {
            subscriber.setLastEmailSent(LocalDateTime.now());
            subscriberRepository.save(subscriber);
        });
    }

    /**
     * Usuwa subskrybenta całkowicie z bazy danych
     */
    @CacheEvict(value = "newsletterSubscribers", allEntries = true)
    @Transactional
    public void deleteSubscriber(Long id)  {
        subscriberRepository.deleteById(id);
    }

    /**
     * Wysyła wiadomość do wszystkich aktywnych i zweryfikowanych subskrybentów
     */
    @Transactional
    public void sendBulkEmail(String subject, String content) {
        try {
            List<NewsletterSubscriber> subscribers = getAllActiveSubscribers();

            for (NewsletterSubscriber subscriber : subscribers) {
                emailService.sendCustomEmail(subscriber.getEmail(), subject, content);
                updateLastEmailSent(subscriber.getEmail());
            }
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas wysyłania masowego emaila", e);
        }
    }

    /**
     * Pobiera statystyki newslettera
     */
    @Cacheable("newsletterStats")
    @Transactional(readOnly = true)
    public Map<String, Object> getNewsletterStats() {
        long totalCount = subscriberRepository.count();
        long verifiedCount = subscriberRepository.countVerifiedSubscribers();
        long activeCount = subscriberRepository.countActiveSubscribers();
        long activeVerifiedCount = subscriberRepository.countActiveVerifiedSubscribers();

        Map<SubscriberRole, Long> roleDistribution =  new HashMap<>();
        List<Object[]> roleCounts = subscriberRepository.countByRole();

        for (Object[] roleCount : roleCounts){
            SubscriberRole role = (SubscriberRole) roleCount[0];
            Long count = (Long)     roleCount[1];
            roleDistribution.put(role, count);
        }

        return Map.of(
                "total", totalCount,
                "verified", verifiedCount,
                "active", activeCount,
                "activeVerified", activeVerifiedCount,
                "roleDistribution", roleDistribution
        );
    }
}