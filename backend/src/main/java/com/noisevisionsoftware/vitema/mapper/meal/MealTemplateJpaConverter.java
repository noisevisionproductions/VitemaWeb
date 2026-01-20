package com.noisevisionsoftware.vitema.mapper.meal;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.meal.MealIngredient;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateEntity;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateIngredientEntity;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplatePhotoEntity;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MealTemplateJpaConverter {

    public MealTemplate toModel(MealTemplateEntity entity) {
        if (entity == null) return null;

        return MealTemplate.builder()
                .id(entity.getExternalId())
                .name(entity.getName())
                .instructions(entity.getInstructions())
                .nutritionalValues(convertNutritionalValues(entity))
                .photos(convertPhotos(entity.getPhotos()))
                .ingredients(convertIngredients(entity.getIngredients()))
                .mealType(entity.getMealType())
                .category(entity.getCategory())
                .createdBy(entity.getCreatedBy())
                .createdAt(convertToTimestamp(entity.getCreatedAt()))
                .updatedAt(convertToTimestamp(entity.getUpdatedAt()))
                .lastUsed(convertToTimestamp(entity.getLastUsed()))
                .usageCount(entity.getUsageCount())
                .build();
    }

    public MealTemplateEntity toEntity(MealTemplate model) {
        if (model == null) return null;

        MealTemplateEntity entity = MealTemplateEntity.builder()
                .externalId(model.getId())
                .name(model.getName())
                .instructions(model.getInstructions())
                .mealType(model.getMealType())
                .category(model.getCategory())
                .createdBy(model.getCreatedBy())
                .createdAt(convertToLocalDateTime(model.getCreatedAt()))
                .updatedAt(convertToLocalDateTime(model.getUpdatedAt()))
                .lastUsed(convertToLocalDateTime(model.getLastUsed()))
                .usageCount(model.getUsageCount())
                .build();

        if (model.getNutritionalValues() != null) {
            entity.setCalories(convertToBigDecimal(model.getNutritionalValues().getCalories()));
            entity.setProtein(convertToBigDecimal(model.getNutritionalValues().getProtein()));
            entity.setFat(convertToBigDecimal(model.getNutritionalValues().getFat()));
            entity.setCarbs(convertToBigDecimal(model.getNutritionalValues().getCarbs()));
        }

        if (model.getPhotos() != null) {
            List<MealTemplatePhotoEntity> photoEntities = new ArrayList<>();
            for (int i = 0; i < model.getPhotos().size(); i++) {
                photoEntities.add(MealTemplatePhotoEntity.builder()
                        .mealTemplate(entity)
                        .photoUrl(model.getPhotos().get(i))
                        .displayOrder(i)
                        .build());
            }
            entity.setPhotos(photoEntities);
        }

        if (model.getIngredients() != null) {
            List<MealTemplateIngredientEntity> ingredientEntities = new ArrayList<>();
            for (int i = 0; i < model.getIngredients().size(); i++) {
                MealIngredient ingredient = model.getIngredients().get(i);
                ingredientEntities.add(MealTemplateIngredientEntity.builder()
                        .mealTemplate(entity)
                        .name(ingredient.getName())
                        .quantity(BigDecimal.valueOf(ingredient.getQuantity()))
                        .unit(ingredient.getUnit())
                        .originalText(ingredient.getOriginal())
                        .categoryId(ingredient.getCategoryId())
                        .hasCustomUnit(ingredient.isHasCustomUnit())
                        .displayOrder(i)
                        .build());
            }
            entity.setIngredients(ingredientEntities);
        }

        return entity;
    }

    public void updateEntity(MealTemplateEntity entity, MealTemplate model) {
        entity.setName(model.getName());
        entity.setInstructions(model.getInstructions());
        entity.setMealType(model.getMealType());
        entity.setCategory(model.getCategory());

        if (model.getNutritionalValues() != null) {
            entity.setCalories(convertToBigDecimal(model.getNutritionalValues().getCalories()));
            entity.setProtein(convertToBigDecimal(model.getNutritionalValues().getProtein()));
            entity.setFat(convertToBigDecimal(model.getNutritionalValues().getFat()));
            entity.setCarbs(convertToBigDecimal(model.getNutritionalValues().getCarbs()));
        }

        entity.getPhotos().clear();
        if (model.getPhotos() != null) {
            for (int i = 0; i < model.getPhotos().size(); i++) {
                entity.getPhotos().add(MealTemplatePhotoEntity.builder()
                        .mealTemplate(entity)
                        .photoUrl(model.getPhotos().get(i))
                        .displayOrder(i)
                        .build());
            }
        }

        entity.getIngredients().clear();
        if (model.getIngredients() != null) {
            for (int i = 0; i < model.getIngredients().size(); i++) {
                MealIngredient ingredient = model.getIngredients().get(i);
                entity.getIngredients().add(MealTemplateIngredientEntity.builder()
                        .mealTemplate(entity)
                        .name(ingredient.getName())
                        .quantity(BigDecimal.valueOf(ingredient.getQuantity()))
                        .unit(ingredient.getUnit())
                        .originalText(ingredient.getOriginal())
                        .categoryId(ingredient.getCategoryId())
                        .hasCustomUnit(ingredient.isHasCustomUnit())
                        .displayOrder(i)
                        .build());
            }
        }
    }

    private List<MealIngredient> convertIngredients(List<MealTemplateIngredientEntity> ingredientEntities) {
        return ingredientEntities.stream()
                .sorted(Comparator.comparingInt(MealTemplateIngredientEntity::getDisplayOrder))
                .map(e -> MealIngredient.builder()
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

    private NutritionalValues convertNutritionalValues(MealTemplateEntity entity) {
        if (entity.getCalories() == null && entity.getProtein() == null &&
                entity.getFat() == null && entity.getCarbs() == null) {
            return null;
        }


        return NutritionalValues.builder()
                .calories(convertToDouble(entity.getCalories()))
                .protein(convertToDouble(entity.getProtein()))
                .fat(convertToDouble(entity.getFat()))
                .carbs(convertToDouble(entity.getCarbs()))
                .build();
    }


    private List<String> convertPhotos(List<MealTemplatePhotoEntity> photoEntities) {
        return photoEntities.stream()
                .sorted(Comparator.comparingInt(MealTemplatePhotoEntity::getDisplayOrder))
                .map(MealTemplatePhotoEntity::getPhotoUrl)
                .collect(Collectors.toList());
    }


    private BigDecimal convertToBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value);
    }

    private LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneOffset.UTC
        );
    }

    private Timestamp convertToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.ofTimeSecondsAndNanos(
                instant.getEpochSecond(),
                instant.getNano()
        );
    }

    private Double convertToDouble(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }
}
