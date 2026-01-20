package com.noisevisionsoftware.vitema.repository.impl;

import com.noisevisionsoftware.vitema.mapper.recipe.RecipeImageReferenceJpaConverter;
import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeImageReferenceEntity;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeImageReferenceJpaRepository;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostgresRecipeImageRepositoryImpl implements RecipeImageRepository {

    private final RecipeImageReferenceJpaRepository jpaRepository;
    private final RecipeImageReferenceJpaConverter converter;

    @Override
    public RecipeImageReference save(RecipeImageReference imageReference) {
        try {
            RecipeImageReferenceEntity entity = converter.toJpaEntity(imageReference);
            entity = jpaRepository.save(entity);
            return converter.toModel(entity);
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania referencji zdjęcia", e);
            throw new RuntimeException("Nie udało się zapisać referencji zdjęcia", e);
        }
    }

    @Override
    public Optional<RecipeImageReference> findByImageUrl(String imageUrl) {
        try {
            return jpaRepository.findByImageUrl(imageUrl)
                    .map(converter::toModel);
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania referencji po URL: {}", imageUrl, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void incrementReferenceCount(String imageUrl) {
        try {
            Optional<RecipeImageReferenceEntity> entityOpt = jpaRepository.findByImageUrl(imageUrl);

            if (entityOpt.isPresent()) {
                jpaRepository.incrementReferenceCount(imageUrl);
            } else {
                log.warn("Próba zwiększenia licznika referencji dla nieistniejącego zdjęcia: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("Błąd podczas zwiększania licznika referencji: {}", imageUrl, e);
            throw new RuntimeException("Nie udało się zwiększyć licznika referencji", e);
        }
    }

    @Override
    @Transactional
    public int decrementReferenceCount(String imageUrl) {
        try {
            Optional<RecipeImageReferenceEntity> entityOpt = jpaRepository.findByImageUrl(imageUrl);

            if (entityOpt.isPresent()) {
                jpaRepository.decrementReferenceCount(imageUrl);
                return jpaRepository.findReferenceCount(imageUrl).orElse(0);
            } else {
                log.warn("Próba zmniejszenia licznika referencji dla nieistniejącego zdjęcia: {}", imageUrl);
                return 0;
            }
        } catch (Exception e) {
            log.error("Błąd podczas zmniejszania licznika referencji: {}", imageUrl, e);
            throw new RuntimeException("Nie udało się zmniejszyć licznika referencji", e);
        }
    }

    @Override
    @Transactional
    public void deleteByImageUrl(String imageUrl) {
        try {
            jpaRepository.deleteByImageUrl(imageUrl);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania referencji: {}", imageUrl, e);
            throw new RuntimeException("Nie udało się usunąć referencji zdjęcia", e);
        }
    }

    @Override
    public List<RecipeImageReference> findAllWithZeroReferences() {
        try {
            return jpaRepository.findByReferenceCount(0)
                    .stream()
                    .map(converter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas wyszukiwania referencji z zerowym licznikiem", e);
            return List.of(); // Zwracamy pustą listę zamiast rzucać wyjątek
        }
    }
}