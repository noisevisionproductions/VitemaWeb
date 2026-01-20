package com.noisevisionsoftware.vitema.service.newsletter;

import com.noisevisionsoftware.vitema.dto.request.newsletter.ExternalRecipientRequest;
import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import com.noisevisionsoftware.vitema.repository.jpa.newsletter.ExternalRecipientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalRecipientsService {

    private final ExternalRecipientRepository externalRecipientRepository;
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Dietetyk", "Firma", "Portal dietetyczny", "Potencjalny partner", "Inne"
    );

    /**
     * Pobiera wszystkich zewnętrznych odbiorców
     */
    @Transactional(readOnly = true)
    public List<ExternalRecipient> getAllRecipients() {
        return externalRecipientRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Pobiera odbiorców po identyfikatorach
     */
    @Transactional(readOnly = true)
    public List<ExternalRecipient> getRecipientsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        return externalRecipientRepository.findAllById(ids);
    }

    /**
     * Dodaje nowego odbiorcę
     */
    @Transactional
    public ExternalRecipient addRecipient(ExternalRecipientRequest request) {
        if (externalRecipientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Odbiorca o podanym adresie email już istnieje");
        }

        ExternalRecipient recipient = ExternalRecipient.builder()
                .email(request.getEmail())
                .name(request.getName())
                .category(request.getCategory())
                .status(request.getStatus())
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .build();

        recipient.setTagList(request.getTags());

        return externalRecipientRepository.save(recipient);
    }

    /**
     * Dodaje wielu odbiorców
     */
    @Transactional
    public Map<String, Object> bulkAddRecipients(List<ExternalRecipientRequest> requests) {
        List<String> addedEmails = new ArrayList<>();
        List<Map<String, Object>> errors = new ArrayList<>();

        for (ExternalRecipientRequest request : requests) {
            try {
                if (externalRecipientRepository.findByEmail(request.getEmail()).isPresent()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("email", request.getEmail());
                    error.put("error", "Odbiorca o podanym adresie email już istnieje");
                    errors.add(error);
                    continue;
                }

                ExternalRecipient recipient = ExternalRecipient.builder()
                        .email(request.getEmail())
                        .name(request.getName())
                        .category(request.getCategory())
                        .status(request.getStatus())
                        .notes(request.getNotes())
                        .createdAt(LocalDateTime.now())
                        .build();

                recipient.setTagList(request.getTags());

                externalRecipientRepository.save(recipient);

                addedEmails.add(request.getEmail());
            } catch (Exception e) {
                Map<String, Object> error = new HashMap<>();
                error.put("email", request.getEmail());
                error.put("error", e.getMessage());
                errors.add(error);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("added", addedEmails.size());
        result.put("addedEmails", addedEmails);
        result.put("errors", errors);

        return result;
    }

    /**
     * Aktualizuje dane odbiorcy
     */
    @Transactional
    public ExternalRecipient updateRecipient(Long id, ExternalRecipientRequest request) {
        ExternalRecipient recipient = findRecipientById(id);

        if (request.getName() != null) recipient.setName(request.getName());
        if (request.getNotes() != null) recipient.setNotes(request.getNotes());
        if (request.getCategory() != null) recipient.setCategory(request.getCategory());
        if (request.getTags() != null) recipient.setTagList(request.getTags());

        return externalRecipientRepository.save(recipient);
    }

    /**
     * Aktualizuje status odbiorcy
     */
    @Transactional
    public ExternalRecipient updateStatus(Long id, String status) {
        ExternalRecipient recipient = findRecipientById(id);

        recipient.setStatus(status);
        if ("contacted".equals(status)) {
            recipient.setLastContactDate(LocalDateTime.now());
        }

        return externalRecipientRepository.save(recipient);
    }

    /**
     * Usuwa odbiorcę
     */
    @Transactional
    public void deleteRecipient(Long id) {
        externalRecipientRepository.deleteById(id);
    }

    /**
     * Pobiera dostępne kategorie
     */
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        List<String> dbCategories = externalRecipientRepository.findAllCategories();

        Set<String> allCategories = new HashSet<>(dbCategories);
        allCategories.addAll(DEFAULT_CATEGORIES);

        return allCategories.stream().sorted().collect(Collectors.toList());
    }

    private ExternalRecipient findRecipientById(Long id) {
        return externalRecipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono odbiorcy o podanym ID"));
    }
}
