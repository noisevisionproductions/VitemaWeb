package com.noisevisionsoftware.vitema.repository.meal;

import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.meal.MealTemplateJpaConverter;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateEntity;
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
public class PostgresMealTemplateRepository implements MealTemplateRepository {

    private final MealTemplateJpaRepository jpaRepository;
    private final MealTemplateJpaConverter converter;

    @Override
    public Optional<MealTemplate> findById(String id) {
        try {
            return jpaRepository.findByExternalId(id)
                    .map(converter::toModel);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonu posiłku: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<MealTemplate> searchByName(String query, int limit) {
        try {
            Pageable pageable = PageRequest.of(1, limit);
            List<MealTemplateEntity> entities = jpaRepository.searchByNameOrInstructions(query, pageable);

            return entities.stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania szablonów", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<MealTemplate> findTopByUsageCount(int limit) {
        try {
            Pageable pageable = PageRequest.of(1, limit);
            return jpaRepository.findTopByOrderByUsageCountDescLastUsedDesc(pageable).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania popularnych szablonów", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<MealTemplate> findRecentlyUsed(int limit) {
        try {
            Pageable pageable = PageRequest.of(1, limit);
            return jpaRepository.findRecentlyUsed(pageable).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania ostatnio używanych szablonów", e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public MealTemplate save(MealTemplate template) {
        try {
            MealTemplateEntity entity;

            if (template.getId() != null) {
                entity = jpaRepository.findByExternalId(template.getId())
                        .orElseThrow(() -> new NotFoundException("Template not found: " + template.getId()));
                converter.updateEntity(entity, template);
            } else {
                entity = converter.toEntity(template);
            }

            MealTemplateEntity saved = jpaRepository.save(entity);
            return converter.toModel(saved);
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania szablonu posiłku", e);
            throw new RuntimeException("Nie udało się zapisać szablonu", e);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        try {
            MealTemplateEntity entity = jpaRepository.findByExternalId(id)
                    .orElseThrow(() -> new NotFoundException("Template not found: " + id));
            jpaRepository.delete(entity);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania szablonu: {}", id, e);
            throw new RuntimeException("Nie udało się usunąć szablonu", e);
        }
    }

    public List<MealTemplate> searchAccessibleTemplates(String query, String userId, int limit) {
        try {
            Pageable pageable = PageRequest.of(1, limit);
            return jpaRepository.searchAccessibleTemplates(query, userId, pageable).stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania dostępnych szablonów", e);
            return new ArrayList<>();
        }
    }

    @Transactional
    public void incrementUsageCount(String externalId) {
        try {
            jpaRepository.incrementUsageCount(externalId);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji licznika użycia: {}", externalId, e);
        }
    }
}
