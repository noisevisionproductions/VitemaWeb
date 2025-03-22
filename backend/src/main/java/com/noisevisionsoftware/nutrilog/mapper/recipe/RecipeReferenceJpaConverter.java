package com.noisevisionsoftware.nutrilog.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.recipe.RecipeReference;
import com.noisevisionsoftware.nutrilog.model.recipe.jpa.RecipeReferenceEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class RecipeReferenceJpaConverter {

    public RecipeReferenceEntity toJpaEntity(RecipeReference reference) {
        if (reference == null) return null;

        LocalDateTime addedAt;
        if (reference.getAddedAt() != null) {
            addedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(reference.getAddedAt().getSeconds(), reference.getAddedAt().getNanos()),
                    ZoneOffset.UTC);
        } else {
            addedAt = LocalDateTime.now();
        }

        return RecipeReferenceEntity.builder()
                .id(reference.getId() != null ? reference.getId() : UUID.randomUUID().toString())
                .recipeId(reference.getRecipeId())
                .dietId(reference.getDietId())
                .userId(reference.getUserId())
                .mealType(reference.getMealType())
                .addedAt(addedAt)
                .build();
    }

    public RecipeReference toModel(RecipeReferenceEntity entity) {
        if (entity == null) return null;

        Timestamp addedAt = null;
        if (entity.getAddedAt() != null) {
            addedAt = Timestamp.ofTimeSecondsAndNanos(
                    entity.getAddedAt().toEpochSecond(ZoneOffset.UTC),
                    entity.getAddedAt().getNano());
        }

        return RecipeReference.builder()
                .id(entity.getId())
                .recipeId(entity.getRecipeId())
                .dietId(entity.getDietId())
                .userId(entity.getUserId())
                .mealType(entity.getMealType())
                .addedAt(addedAt)
                .build();
    }
}
