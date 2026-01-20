package com.noisevisionsoftware.vitema.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.recipe.jpa.NutritionalValuesEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeIngredientEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RecipeJpaConverter {

    public RecipeEntity toJpaEntity(Recipe recipe) {
        if (recipe == null) return null;

        NutritionalValuesEntity nutritionalValuesEntity = null;
        if (recipe.getNutritionalValues() != null) {
            nutritionalValuesEntity = NutritionalValuesEntity.builder()
                    .calories(recipe.getNutritionalValues().getCalories())
                    .protein(recipe.getNutritionalValues().getProtein())
                    .fat(recipe.getNutritionalValues().getFat())
                    .carbs(recipe.getNutritionalValues().getCarbs())
                    .build();
        }

        LocalDateTime createdAt;
        if (recipe.getCreatedAt() != null) {
            createdAt = LocalDateTime.ofEpochSecond(
                    recipe.getCreatedAt().getSeconds(),
                    recipe.getCreatedAt().getNanos(),
                    ZoneOffset.UTC);
        } else {
            createdAt = LocalDateTime.now();
        }

        String externalId = recipe.getId();
        if (externalId == null || externalId.isEmpty()) {
            externalId = UUID.randomUUID().toString();
        }

        RecipeEntity recipeEntity = RecipeEntity.builder()
                .externalId(externalId)
                .name(recipe.getName())
                .instructions(recipe.getInstructions())
                .createdAt(createdAt)
                .photos(recipe.getPhotos() != null ? new ArrayList<>(recipe.getPhotos()) : new ArrayList<>())
                .nutritionalValues(nutritionalValuesEntity)
                .parentRecipeId(recipe.getParentRecipeId())
                .build();

        if (recipe.getIngredients() != null) {
            List<RecipeIngredientEntity> ingredientEntities = new ArrayList<>();
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                RecipeIngredient ingredient = recipe.getIngredients().get(i);
                ingredientEntities.add(RecipeIngredientEntity.builder()
                        .recipe(recipeEntity)
                        .name(ingredient.getName())
                        .quantity(BigDecimal.valueOf(ingredient.getQuantity()))
                        .unit(ingredient.getUnit())
                        .originalText(ingredient.getOriginal())
                        .categoryId(ingredient.getCategoryId())
                        .hasCustomUnit(ingredient.isHasCustomUnit())
                        .displayOrder(i)
                        .build());
            }
            recipeEntity.setIngredients(ingredientEntities);
        }

        return recipeEntity;
    }

    public Recipe toModel(RecipeEntity entity) {
        if (entity == null) return null;

        NutritionalValues nutritionalValues = null;
        if (entity.getNutritionalValues() != null) {
            nutritionalValues = NutritionalValues.builder()
                    .calories(entity.getNutritionalValues().getCalories())
                    .protein(entity.getNutritionalValues().getProtein())
                    .fat(entity.getNutritionalValues().getFat())
                    .carbs(entity.getNutritionalValues().getCarbs())
                    .build();
        }

        Timestamp createdAt = null;
        if (entity.getCreatedAt() != null) {
            createdAt = Timestamp.ofTimeSecondsAndNanos(
                    entity.getCreatedAt().toEpochSecond(ZoneOffset.UTC),
                    entity.getCreatedAt().getNano());
        }

        return Recipe.builder()
                .id(entity.getExternalId())
                .name(entity.getName())
                .instructions(entity.getInstructions())
                .createdAt(createdAt)
                .photos(entity.getPhotos())
                .ingredients(convertIngredients(entity.getIngredients()))
                .nutritionalValues(nutritionalValues)
                .parentRecipeId(entity.getParentRecipeId())
                .build();
    }

    private List<RecipeIngredient> convertIngredients(List<RecipeIngredientEntity> ingredientEntities) {
        if (ingredientEntities == null) return new ArrayList<>();

        return ingredientEntities.stream()
                .sorted(Comparator.comparingInt(RecipeIngredientEntity::getDisplayOrder))
                .map(e -> RecipeIngredient.builder()
                        .id(e.getId().toString())
                        .name(e.getName())
                        .quantity(e.getQuantity().doubleValue())
                        .unit(e.getUnit())
                        .original(e.getOriginalText())
                        .categoryId(e.getCategoryId())
                        .hasCustomUnit(e.isHasCustomUnit())
                        .build())
                .collect(Collectors.toList());
    }
}