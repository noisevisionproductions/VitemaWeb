package com.noisevisionsoftware.vitema.mapper.recipe;

import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeImageReferenceEntity;
import org.springframework.stereotype.Component;

@Component
public class RecipeImageReferenceJpaConverter {

    public RecipeImageReferenceEntity toJpaEntity(RecipeImageReference model) {
        if (model == null) return null;

        RecipeImageReferenceEntity entity = RecipeImageReferenceEntity.builder()
                .imageUrl(model.getImageUrl())
                .storagePath(model.getStoragePath())
                .referenceCount(model.getReferenceCount())
                .build();

        if (model.getId() != null && !model.getId().isEmpty()) {
            try {
                entity.setId(Long.parseLong(model.getId()));
            } catch (NumberFormatException ignored) {

            }
        }

        return entity;
    }

    public RecipeImageReference toModel(RecipeImageReferenceEntity entity) {
        if (entity == null) return null;

        return RecipeImageReference.builder()
                .id(entity.getId().toString())
                .imageUrl(entity.getImageUrl())
                .storagePath(entity.getStoragePath())
                .referenceCount(entity.getReferenceCount())
                .build();
    }
}
