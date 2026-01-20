package com.noisevisionsoftware.vitema.repository.jpa.newsletter;

import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    /**
     * Znajduje subskrybenta po adresie email
     *
     * @param email adres email subskrybenta
     * @return optional zawierający subskrybenta lub pusty
     */
    Optional<NewsletterSubscriber> findByEmail(String email);

    /**
     * Znajduje subskrybenta po tokenie weryfikacyjnym
     *
     * @param token token weryfikacyjny
     * @return optional zawierający subskrybenta lub pusty
     */
    Optional<NewsletterSubscriber> findByVerificationToken(String token);

    /**
     * Znajduje wszystkich aktywnych i zweryfikowanych subskrybentów
     *
     * @return lista aktywnych i zweryfikowanych subskrybentów
     */
    List<NewsletterSubscriber> findAllByActiveTrueAndVerifiedTrue();

    /**
     * Liczy zweryfikowanych subskrybentów
     *
     * @return liczba zweryfikowanych subskrybentów
     */
    @Query("SELECT COUNT(s) FROM NewsletterSubscriber s WHERE s.verified = true")
    long countVerifiedSubscribers();

    /**
     * Liczy aktywnych subskrybentów
     *
     * @return liczba aktywnych subskrybentów
     */
    @Query("SELECT COUNT(s) FROM NewsletterSubscriber s WHERE s.active = true")
    long countActiveSubscribers();

    /**
     * Liczy aktywnych i zweryfikowanych subskrybentów
     *
     * @return liczba aktywnych i zweryfikowanych subskrybentów
     */
    @Query("SELECT COUNT(s) FROM NewsletterSubscriber s WHERE s.active = true AND s.verified = true")
    long countActiveVerifiedSubscribers();

    /**
     * Liczy subskrybentów według roli
     *
     * @return lista par [rola, liczba]
     */
    @Query("SELECT s.role, COUNT(s) FROM NewsletterSubscriber s GROUP BY s.role")
    List<Object[]> countByRole();
}