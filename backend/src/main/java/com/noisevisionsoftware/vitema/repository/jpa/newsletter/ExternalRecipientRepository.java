package com.noisevisionsoftware.vitema.repository.jpa.newsletter;

import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalRecipientRepository extends JpaRepository<ExternalRecipient, Long> {

    /**
     * Znajduje odbiorców posortowanych po dacie utworzenia malejąco
     *
     * @return lista odbiorców
     */
    List<ExternalRecipient> findAllByOrderByCreatedAtDesc();

    /**
     * Znajduje odbiorcę po adresie email
     *
     * @param email adres email odbiorcy
     * @return optional zawierający odbiorcę lub pusty
     */
    Optional<ExternalRecipient> findByEmail(String email);

    /**
     * Znajduje wszystkie kategorie odbiorców
     *
     * @return lista kategorii
     */
    @Query("SELECT DISTINCT e.category FROM ExternalRecipient e")
    List<String> findAllCategories();

    @Query("SELECT e FROM ExternalRecipient e JOIN e.tags t WHERE t.tag IN :tags")
    List<ExternalRecipient> findAllByTags(List<String> tags);
}