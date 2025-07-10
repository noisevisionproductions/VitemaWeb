package com.noisevisionsoftware.nutrilog.repository.diet;

import com.noisevisionsoftware.nutrilog.exception.NotFoundException;
import com.noisevisionsoftware.nutrilog.mapper.diet.DietTemplateJpaConverter;
import com.noisevisionsoftware.nutrilog.model.diet.template.DietTemplate;
import com.noisevisionsoftware.nutrilog.model.diet.template.DietTemplateCategory;
import com.noisevisionsoftware.nutrilog.model.diet.template.jpa.DietTemplateEntity;
import com.noisevisionsoftware.nutrilog.repository.jpa.diet.DietTemplateJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostgresDietTemplateRepository implements DietTemplateRepository {

    private final DietTemplateJpaRepository jpaRepository;
    private final DietTemplateJpaConverter converter;

    @Override
    public Optional<DietTemplate> findById(String id) {
        try {
            return jpaRepository.findByExternalId(id)
                    .map(converter::toModel);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonu diety: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<DietTemplate> findByCreatedBy(String createdBy) {
        try {
            return jpaRepository.findByCreatedByOrderByCreatedAtDesc(createdBy).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonów dla użytkownika: {}", createdBy, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DietTemplate> findByCategoryAndCreatedBy(DietTemplateCategory category, String createdBy) {
        try {
            DietTemplateCategory categoryEnum = DietTemplateCategory.valueOf(category.name());
            return jpaRepository.findByCategoryAndCreatedByOrderByCreatedAtDesc(categoryEnum, createdBy).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonów dla kategorii: {} i użytkownika: {}", category, createdBy, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DietTemplate> findTopByCreatedByOrderByUsageCountDesc(String createdBy, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return jpaRepository.findTopByCreatedByOrderByUsageCountDesc(createdBy, pageable).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania popularnych szablonów", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DietTemplate> searchByNameOrDescription(String query, String createdBy, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return jpaRepository.searchByNameOrDescription(query, createdBy, pageable).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania szablonów", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public DietTemplate save(DietTemplate template) {
        try {
            DietTemplateEntity entity;

            if (template.getId() != null) {
                entity = jpaRepository.findByExternalId(template.getId())
                        .orElseThrow(() -> new NotFoundException("Template not found: " + template.getId()));
                converter.updateEntity(entity, template);
            } else {
                entity = converter.toEntity(template);
            }

            DietTemplateEntity saved = jpaRepository.save(entity);
            return converter.toModel(saved);
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania szablonu diety", e);
            throw new RuntimeException("Nie udało się zapisać szablonu", e);
        }
    }


    @Override
    @Transactional
    public void deleteById(String id) {
        try {
            DietTemplateEntity entity = jpaRepository.findByExternalId(id)
                    .orElseThrow(() -> new NotFoundException("Template not found: " + id));
            jpaRepository.delete(entity);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania szablonu: {}", id, e);
            throw new RuntimeException("Nie udało się usunąć szablonu", e);
        }
    }

    @Override
    @Transactional
    public void incrementUsageCount(String id) {
        try {
            jpaRepository.incrementUsageCount(id);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji licznika użycia: {}", id, e);
        }
    }

    @Override
    public long countByCreatedBy(String createdBy) {
        try {
            return jpaRepository.countByCreatedBy(createdBy);
        } catch (Exception e) {
            log.error("Błąd podczas liczenia szablonów dla użytkownika: {}", createdBy, e);
            return 0;
        }
    }
}
