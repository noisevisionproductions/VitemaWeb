package com.noisevisionsoftware.vitema.mapper.diet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.diet.template.*;
import com.noisevisionsoftware.vitema.model.diet.template.jpa.*;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DietTemplateJpaConverter {

    private final ObjectMapper objectMapper;

    public DietTemplate toModel(DietTemplateEntity entity) {
        if (entity == null) return null;

        return DietTemplate.builder()
                .id(entity.getExternalId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(convertCategory(entity.getCategory()))
                .createdBy(entity.getCreatedBy())
                .createdAt(convertToTimestamp(entity.getCreatedAt()))
                .updatedAt(convertToTimestamp(entity.getUpdatedAt()))
                .version(entity.getVersion())
                .duration(entity.getDuration())
                .mealsPerDay(entity.getMealsPerDay())
                .mealTimes(deserializeMealTimes(entity.getMealTimesJson()))
                .mealTypes(deserializeMealTypes(entity.getMealTypesJson()))
                .days(convertDays(entity.getDays()))
                .targetNutrition(convertNutrition(entity))
                .usageCount(entity.getUsageCount())
                .lastUsed(convertToTimestamp(entity.getLastUsed()))
                .notes(entity.getNotes())
                .build();
    }

    public DietTemplateEntity toEntity(DietTemplate model) {
        if (model == null) return null;

        DietTemplateEntity entity = DietTemplateEntity.builder()
                .externalId(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .category(convertCategoryEnum(model.getCategory()))
                .createdBy(model.getCreatedBy())
                .createdAt(convertToLocalDateTime(model.getCreatedAt()))
                .updatedAt(convertToLocalDateTime(model.getUpdatedAt()))
                .version(model.getVersion())
                .duration(model.getDuration())
                .mealsPerDay(model.getMealsPerDay())
                .mealTimesJson(serializeMealTimes(model.getMealTimes()))
                .mealTypesJson(serializeMealTypes(model.getMealTypes()))
                .usageCount(model.getUsageCount())
                .lastUsed(convertToLocalDateTime(model.getLastUsed()))
                .notes(model.getNotes())
                .build();

        // Wartości odżywcze
        if (model.getTargetNutrition() != null) {
            entity.setTargetCalories(model.getTargetNutrition().getTargetCalories());
            entity.setTargetProtein(model.getTargetNutrition().getTargetProtein());
            entity.setTargetFat(model.getTargetNutrition().getTargetFat());
            entity.setTargetCarbs(model.getTargetNutrition().getTargetCarbs());
            entity.setCalculationMethod(model.getTargetNutrition().getCalculationMethod());
        }

        // Dni
        if (model.getDays() != null) {
            List<DietTemplateDayEntity> dayEntities = new ArrayList<>();
            for (DietTemplateDayData day : model.getDays()) {
                DietTemplateDayEntity dayEntity = convertDayToEntity(day, entity);
                dayEntities.add(dayEntity);
            }
            entity.setDays(dayEntities);
        }

        return entity;
    }

    public void updateEntity(DietTemplateEntity entity, DietTemplate model) {
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setCategory(convertCategoryEnum(model.getCategory()));
        entity.setDuration(model.getDuration());
        entity.setMealsPerDay(model.getMealsPerDay());
        entity.setMealTimesJson(serializeMealTimes(model.getMealTimes()));
        entity.setMealTypesJson(serializeMealTypes(model.getMealTypes()));
        entity.setNotes(model.getNotes());

        // Wartości odżywcze
        if (model.getTargetNutrition() != null) {
            entity.setTargetCalories(model.getTargetNutrition().getTargetCalories());
            entity.setTargetProtein(model.getTargetNutrition().getTargetProtein());
            entity.setTargetFat(model.getTargetNutrition().getTargetFat());
            entity.setTargetCarbs(model.getTargetNutrition().getTargetCarbs());
            entity.setCalculationMethod(model.getTargetNutrition().getCalculationMethod());
        }

        // Aktualizuj dni
        entity.getDays().clear();
        if (model.getDays() != null) {
            for (DietTemplateDayData day : model.getDays()) {
                DietTemplateDayEntity dayEntity = convertDayToEntity(day, entity);
                entity.getDays().add(dayEntity);
            }
        }
    }

    private DietTemplateDayEntity convertDayToEntity(DietTemplateDayData day, DietTemplateEntity template) {
        DietTemplateDayEntity dayEntity = DietTemplateDayEntity.builder()
                .dietTemplate(template)
                .dayNumber(day.getDayNumber())
                .dayName(day.getDayName())
                .notes(day.getNotes())
                .build();

        if (day.getMeals() != null) {
            List<DietTemplateMealEntity> mealEntities = new ArrayList<>();
            for (int i = 0; i < day.getMeals().size(); i++) {
                DietTemplateMealData meal = day.getMeals().get(i);
                DietTemplateMealEntity mealEntity = convertMealToEntity(meal, dayEntity, i);
                mealEntities.add(mealEntity);
            }
            dayEntity.setMeals(mealEntities);
        }

        return dayEntity;
    }

    private DietTemplateMealEntity convertMealToEntity(DietTemplateMealData meal, DietTemplateDayEntity day, int order) {
        DietTemplateMealEntity mealEntity = DietTemplateMealEntity.builder()
                .templateDay(day)
                .name(meal.getName())
                .mealType(meal.getMealType())
                .time(meal.getTime())
                .instructions(meal.getInstructions())
                .mealOrder(order)
                .mealTemplateId(meal.getMealTemplateId())
                .build();

        // Wartości odżywcze
        if (meal.getNutritionalValues() != null) {
            mealEntity.setCalories(convertToBigDecimal(meal.getNutritionalValues().getCalories()));
            mealEntity.setProtein(convertToBigDecimal(meal.getNutritionalValues().getProtein()));
            mealEntity.setFat(convertToBigDecimal(meal.getNutritionalValues().getFat()));
            mealEntity.setCarbs(convertToBigDecimal(meal.getNutritionalValues().getCarbs()));
        }

        // Składniki
        if (meal.getIngredients() != null) {
            List<DietTemplateIngredientEntity> ingredientEntities = new ArrayList<>();
            for (int i = 0; i < meal.getIngredients().size(); i++) {
                DietTemplateIngredient ingredient = meal.getIngredients().get(i);
                ingredientEntities.add(DietTemplateIngredientEntity.builder()
                        .templateMeal(mealEntity)
                        .name(ingredient.getName())
                        .quantity(BigDecimal.valueOf(ingredient.getQuantity()))
                        .unit(ingredient.getUnit())
                        .originalText(ingredient.getOriginal())
                        .categoryId(ingredient.getCategoryId())
                        .hasCustomUnit(ingredient.isHasCustomUnit())
                        .displayOrder(i)
                        .build());
            }
            mealEntity.setIngredients(ingredientEntities);
        }

        // Zdjęcia
        if (meal.getPhotos() != null) {
            List<DietTemplateMealPhotoEntity> photoEntities = new ArrayList<>();
            for (int i = 0; i < meal.getPhotos().size(); i++) {
                photoEntities.add(DietTemplateMealPhotoEntity.builder()
                        .templateMeal(mealEntity)
                        .photoUrl(meal.getPhotos().get(i))
                        .displayOrder(i)
                        .build());
            }
            mealEntity.setPhotos(photoEntities);
        }

        return mealEntity;
    }

    private List<DietTemplateDayData> convertDays(List<DietTemplateDayEntity> dayEntities) {
        return dayEntities.stream()
                .sorted(Comparator.comparing(DietTemplateDayEntity::getDayNumber))
                .map(this::convertDay)
                .collect(Collectors.toList());
    }

    private DietTemplateDayData convertDay(DietTemplateDayEntity entity) {
        return DietTemplateDayData.builder()
                .dayNumber(entity.getDayNumber())
                .dayName(entity.getDayName())
                .notes(entity.getNotes())
                .meals(convertMeals(entity.getMeals()))
                .build();
    }

    private List<DietTemplateMealData> convertMeals(List<DietTemplateMealEntity> mealEntities) {
        return mealEntities.stream()
                .sorted(Comparator.comparing(DietTemplateMealEntity::getMealOrder))
                .map(this::convertMeal)
                .collect(Collectors.toList());
    }

    private DietTemplateMealData convertMeal(DietTemplateMealEntity entity) {
        return DietTemplateMealData.builder()
                .name(entity.getName())
                .mealType(entity.getMealType())
                .time(entity.getTime())
                .instructions(entity.getInstructions())
                .ingredients(convertIngredients(entity.getIngredients()))
                .nutritionalValues(convertMealNutrition(entity))
                .photos(convertPhotos(entity.getPhotos()))
                .mealTemplateId(entity.getMealTemplateId())
                .build();
    }

    private List<DietTemplateIngredient> convertIngredients(List<DietTemplateIngredientEntity> ingredientEntities) {
        return ingredientEntities.stream()
                .sorted(Comparator.comparing(DietTemplateIngredientEntity::getDisplayOrder))
                .map(e -> DietTemplateIngredient.builder()
                        .name(e.getName())
                        .quantity(e.getQuantity().doubleValue())
                        .unit(e.getUnit())
                        .original(e.getOriginalText())
                        .categoryId(e.getCategoryId())
                        .hasCustomUnit(e.isHasCustomUnit())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> convertPhotos(List<DietTemplateMealPhotoEntity> photoEntities) {
        return photoEntities.stream()
                .sorted(Comparator.comparing(DietTemplateMealPhotoEntity::getDisplayOrder))
                .map(DietTemplateMealPhotoEntity::getPhotoUrl)
                .collect(Collectors.toList());
    }

    // Metody pomocnicze konwersji
    private DietTemplateCategory convertCategory(DietTemplateCategory categoryEnum) {
        try {
            return DietTemplateCategory.valueOf(categoryEnum.name());
        } catch (Exception e) {
            log.warn("Unknown category : {}", categoryEnum);
            return DietTemplateCategory.CUSTOM;
        }
    }

    private DietTemplateCategory convertCategoryEnum(DietTemplateCategory category) {
        try {
            return DietTemplateCategory.valueOf(category.name());
        } catch (Exception e) {
            log.warn("Unknown category: {}", category);
            return DietTemplateCategory.CUSTOM;
        }
    }

    private DietTemplateNutrition convertNutrition(DietTemplateEntity entity) {
        if (entity.getTargetCalories() == null && entity.getTargetProtein() == null &&
                entity.getTargetFat() == null && entity.getTargetCarbs() == null) {
            return null;
        }

        return DietTemplateNutrition.builder()
                .targetCalories(entity.getTargetCalories())
                .targetProtein(entity.getTargetProtein())
                .targetFat(entity.getTargetFat())
                .targetCarbs(entity.getTargetCarbs())
                .calculationMethod(entity.getCalculationMethod())
                .build();
    }

    private NutritionalValues convertMealNutrition(DietTemplateMealEntity entity) {
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

    // Metody konwersji typów
    private BigDecimal convertToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private Double convertToDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    private LocalDateTime convertToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneOffset.UTC
        );
    }

    private Timestamp convertToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
    }

    // Serializacja JSON
    private String serializeMealTimes(Map<String, String> mealTimes) {
        if (mealTimes == null) return null;
        try {
            return objectMapper.writeValueAsString(mealTimes);
        } catch (JsonProcessingException e) {
            log.error("Error serializing meal times", e);
            return "{}";
        }
    }

    private Map<String, String> deserializeMealTimes(String json) {
        if (json == null || json.trim().isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error deserializing meal times: {}", json, e);
            return new HashMap<>();
        }
    }

    private String serializeMealTypes(List<String> mealTypes) {
        if (mealTypes == null) return null;
        try {
            return objectMapper.writeValueAsString(mealTypes);
        } catch (JsonProcessingException e) {
            log.error("Error serializing meal types", e);
            return "[]";
        }
    }

    private List<String> deserializeMealTypes(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error deserializing meal types: {}", json, e);
            return new ArrayList<>();
        }
    }
}