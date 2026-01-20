package com.noisevisionsoftware.vitema.repository.impl;

import com.noisevisionsoftware.vitema.mapper.recipe.RecipeJpaConverter;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeReferenceJpaConverter;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeReferenceEntity;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeJpaRepository;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeReferenceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostgresRecipeRepositoryImpl implements RecipeRepository {

    private final RecipeJpaRepository recipeJpaRepository;
    private final RecipeJpaConverter recipeJpaConverter;
    private final RecipeReferenceJpaRepository recipeReferenceJpaRepository;
    private final RecipeReferenceJpaConverter recipeReferenceJpaConverter;

    @Override
    public Optional<Recipe> findById(String id) {
        try {
            return recipeJpaRepository.findByExternalId(id)
                    .map(recipeJpaConverter::toModel);
        } catch (Exception e) {
            log.error("Failed to fetch recipe by id: {}", id, e);
            throw new RuntimeException("Failed to fetch recipe", e);
        }
    }

    @Override
    public Optional<Recipe> findByName(String name) {
        try {
            RecipeEntity entity = recipeJpaRepository.findFirstByNameIgnoreCaseOrderByCreatedAtDesc(name)
                    .orElse(null);

            if (entity == null) {
                return Optional.empty();
            }

            return Optional.of(recipeJpaConverter.toModel(entity));
        } catch (Exception e) {
            log.error("Failed to find recipe by name: {}", name, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Recipe> findAllByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<RecipeEntity> entities = recipeJpaRepository.findAllByExternalIdIn(new ArrayList<>(ids));
            return entities.stream()
                    .map(recipeJpaConverter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch recipes by ids", e);
            throw new RuntimeException("Failed to fetch recipes", e);
        }
    }

    @Override
    public List<Recipe> findAll() {
        try {
            List<RecipeEntity> entities = recipeJpaRepository.findAll();

            return entities.stream()
                    .map(recipeJpaConverter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania wszystkich przepisów", e);
            throw new RuntimeException("Failed to fetch all recipes", e);
        }
    }

    @Override
    public Page<Recipe> findAll(Pageable pageable) {
        try {
            Page<RecipeEntity> entityPage = recipeJpaRepository.findAll(pageable);

            List<Recipe> recipes = entityPage.getContent().stream()
                    .map(recipeJpaConverter::toModel)
                    .collect(Collectors.toList());

            return new PageImpl<>(recipes, pageable, entityPage.getTotalElements());
        } catch (Exception e) {
            log.error("Failed to fetch all recipes", e);
            throw new RuntimeException("Failed to fetch recipes", e);
        }
    }

    @Override
    public Recipe update(String id, Recipe recipe) {
        try {
            RecipeEntity entity = recipeJpaRepository.findByExternalId(id)
                    .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));

            RecipeEntity updatedEntity = recipeJpaConverter.toJpaEntity(recipe);
            updatedEntity.setId(entity.getId());
            updatedEntity.setExternalId(id);

            RecipeEntity savedEntity = recipeJpaRepository.save(updatedEntity);
            return recipeJpaConverter.toModel(savedEntity);
        } catch (Exception e) {
            log.error("Failed to update recipe: {}", id, e);
            throw new RuntimeException("Failed to update recipe", e);
        }
    }

    @Override
    public List<Recipe> search(String query) {
        try {
            List<RecipeEntity> entities = recipeJpaRepository.search(query);
            return entities.stream()
                    .map(recipeJpaConverter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search recipes", e);
            throw new RuntimeException("Failed to search recipes", e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            RecipeEntity entity = recipeJpaRepository.findByExternalId(id)
                    .orElseThrow(() -> new RuntimeException("Recipe not found with id: " + id));
            recipeJpaRepository.delete(entity);
        } catch (Exception e) {
            log.error("Failed to delete recipe: {}", id, e);
            throw new RuntimeException("Failed to delete recipe", e);
        }
    }

    @Override
    public Recipe save(Recipe recipe) {
        try {
            if (recipe.getId() == null || recipe.getId().isEmpty()) {
                recipe.setId(generateFirestoreStyleId());
            }

            RecipeEntity entity = recipeJpaConverter.toJpaEntity(recipe);
            RecipeEntity savedEntity = recipeJpaRepository.save(entity);
            return recipeJpaConverter.toModel(savedEntity);
        } catch (Exception e) {
            log.error("Failed to save recipe", e);
            throw new RuntimeException("Failed to save recipe", e);
        }
    }

    @Override
    public List<Recipe> findByParentRecipeId(String parentId) {
        try {
            List<RecipeEntity> entities = recipeJpaRepository.findByParentRecipeId(parentId);
            return entities.stream()
                    .map(recipeJpaConverter::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to find recipes by parent id: {}", parentId, e);
            throw new RuntimeException("Failed to find recipes by parent id", e);
        }
    }

    @Override
    public void saveReference(RecipeReference reference) {
        try {
            RecipeReferenceEntity entity = recipeReferenceJpaConverter.toJpaEntity(reference);
            recipeReferenceJpaRepository.save(entity);
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania referencji przepisu w PostgreSQL", e);
            throw new RuntimeException("Failed to save recipe reference", e);
        }
    }

    /**
     * Generuje ID w stylu Firestore - 20 znaków alfanumerycznych
     */
    private String generateFirestoreStyleId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder id = new StringBuilder(20);
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            id.append(characters.charAt(random.nextInt(characters.length())));
        }

        return id.toString();
    }
}