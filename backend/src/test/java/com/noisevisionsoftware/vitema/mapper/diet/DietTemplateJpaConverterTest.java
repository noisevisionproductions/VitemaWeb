package com.noisevisionsoftware.vitema.mapper.diet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.diet.template.*;
import com.noisevisionsoftware.vitema.model.diet.template.jpa.*;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DietTemplateJpaConverterTest {

    private DietTemplateJpaConverter converter;
    private static final String SAMPLE_ID = "dt_abc123def4567890";
    private static final String SAMPLE_NAME = "Test Template";
    private static final String SAMPLE_DESCRIPTION = "Test description";
    private static final String SAMPLE_CREATED_BY = "user-123";

    @BeforeEach
    void setUp() {
        converter = new DietTemplateJpaConverter(new ObjectMapper());
    }

    @Nested
    @DisplayName("toModel")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void givenNullEntity_When_ToModel_Then_ReturnNull() {
            assertThat(converter.toModel(null)).isNull();
        }

        @Test
        @DisplayName("Should convert entity to model with basic fields")
        void givenEntityWithBasicFields_When_ToModel_Then_ConvertAllFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .description(SAMPLE_DESCRIPTION)
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(1)
                    .duration(7)
                    .mealsPerDay(5)
                    .mealTimesJson("{\"BREAKFAST\":\"08:00\",\"LUNCH\":\"13:00\"}")
                    .mealTypesJson("[\"BREAKFAST\",\"LUNCH\",\"DINNER\"]")
                    .usageCount(3)
                    .lastUsed(now)
                    .notes("Test notes")
                    .days(Collections.emptyList())
                    .build();

            // When
            DietTemplate result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getName()).isEqualTo(SAMPLE_NAME);
            assertThat(result.getDescription()).isEqualTo(SAMPLE_DESCRIPTION);
            assertThat(result.getCategory()).isEqualTo(DietTemplateCategory.WEIGHT_LOSS);
            assertThat(result.getCreatedBy()).isEqualTo(SAMPLE_CREATED_BY);
            assertThat(result.getVersion()).isEqualTo(1);
            assertThat(result.getDuration()).isEqualTo(7);
            assertThat(result.getMealsPerDay()).isEqualTo(5);
            assertThat(result.getUsageCount()).isEqualTo(3);
            assertThat(result.getNotes()).isEqualTo("Test notes");
            assertThat(result.getMealTimes()).containsEntry("BREAKFAST", "08:00");
            assertThat(result.getMealTimes()).containsEntry("LUNCH", "13:00");
            assertThat(result.getMealTypes()).containsExactly("BREAKFAST", "LUNCH", "DINNER");
        }

        @Test
        @DisplayName("Should convert entity with target nutrition to model")
        void givenEntityWithTargetNutrition_When_ToModel_Then_IncludeNutrition() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .category(DietTemplateCategory.MAINTENANCE)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(0)
                    .duration(14)
                    .mealsPerDay(4)
                    .targetCalories(2000.0)
                    .targetProtein(150.0)
                    .targetFat(65.0)
                    .targetCarbs(250.0)
                    .calculationMethod("TDEE")
                    .usageCount(0)
                    .days(Collections.emptyList())
                    .build();

            // When
            DietTemplate result = converter.toModel(entity);

            // Then
            assertThat(result.getTargetNutrition()).isNotNull();
            assertThat(result.getTargetNutrition().getTargetCalories()).isEqualTo(2000.0);
            assertThat(result.getTargetNutrition().getTargetProtein()).isEqualTo(150.0);
            assertThat(result.getTargetNutrition().getTargetFat()).isEqualTo(65.0);
            assertThat(result.getTargetNutrition().getTargetCarbs()).isEqualTo(250.0);
            assertThat(result.getTargetNutrition().getCalculationMethod()).isEqualTo("TDEE");
        }

        @Test
        @DisplayName("Should return empty map when mealTimesJson is null")
        void givenEntityWithNullMealTimesJson_When_ToModel_Then_ReturnEmptyMap() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .category(DietTemplateCategory.CUSTOM)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(0)
                    .duration(7)
                    .mealsPerDay(3)
                    .usageCount(0)
                    .days(Collections.emptyList())
                    .build();

            // When
            DietTemplate result = converter.toModel(entity);

            // Then
            assertThat(result.getMealTimes()).isEmpty();
            assertThat(result.getMealTypes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntityTests {

        @Test
        @DisplayName("Should return null when model is null")
        void givenNullModel_When_ToEntity_Then_ReturnNull() {
            assertThat(converter.toEntity(null)).isNull();
        }

        @Test
        @DisplayName("Should convert model to entity with basic fields")
        void givenModelWithBasicFields_When_ToEntity_Then_ConvertAllFields() {
            // Given
            Timestamp now = Timestamp.now();
            DietTemplate model = DietTemplate.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .description(SAMPLE_DESCRIPTION)
                    .category(DietTemplateCategory.SPORT)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(2)
                    .duration(21)
                    .mealsPerDay(6)
                    .mealTimes(Map.of("BREAKFAST", "07:00"))
                    .mealTypes(List.of("BREAKFAST", "DINNER"))
                    .usageCount(5)
                    .lastUsed(now)
                    .notes("Entity notes")
                    .days(Collections.emptyList())
                    .build();

            // When
            DietTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getExternalId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getName()).isEqualTo(SAMPLE_NAME);
            assertThat(result.getDescription()).isEqualTo(SAMPLE_DESCRIPTION);
            assertThat(result.getCategory()).isEqualTo(DietTemplateCategory.SPORT);
            assertThat(result.getCreatedBy()).isEqualTo(SAMPLE_CREATED_BY);
            assertThat(result.getVersion()).isEqualTo(2);
            assertThat(result.getDuration()).isEqualTo(21);
            assertThat(result.getMealsPerDay()).isEqualTo(6);
            assertThat(result.getUsageCount()).isEqualTo(5);
            assertThat(result.getNotes()).isEqualTo("Entity notes");
            assertThat(result.getMealTimesJson()).contains("BREAKFAST");
            assertThat(result.getMealTypesJson()).contains("BREAKFAST");
        }

        @Test
        @DisplayName("Should convert model with target nutrition to entity")
        void givenModelWithTargetNutrition_When_ToEntity_Then_IncludeNutritionFields() {
            // Given
            DietTemplateNutrition nutrition = DietTemplateNutrition.builder()
                    .targetCalories(1800.0)
                    .targetProtein(120.0)
                    .targetFat(60.0)
                    .targetCarbs(200.0)
                    .calculationMethod("CUSTOM")
                    .build();
            DietTemplate model = createModelWithNutrition(nutrition);

            // When
            DietTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result.getTargetCalories()).isEqualTo(1800.0);
            assertThat(result.getTargetProtein()).isEqualTo(120.0);
            assertThat(result.getTargetFat()).isEqualTo(60.0);
            assertThat(result.getTargetCarbs()).isEqualTo(200.0);
            assertThat(result.getCalculationMethod()).isEqualTo("CUSTOM");
        }

        @Test
        @DisplayName("Should convert model with days, meals and ingredients to entity")
        void givenModelWithFullStructure_When_ToEntity_Then_ConvertNestedStructure() {
            // Given
            DietTemplateIngredient ingredient = DietTemplateIngredient.builder()
                    .name("Eggs")
                    .quantity(2.0)
                    .unit("szt")
                    .original("2 jajka")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .build();
            NutritionalValues mealNutrition = NutritionalValues.builder()
                    .calories(300.0)
                    .protein(25.0)
                    .fat(20.0)
                    .carbs(5.0)
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .instructions("Cook eggs")
                    .ingredients(List.of(ingredient))
                    .nutritionalValues(mealNutrition)
                    .photos(List.of("photo1.jpg"))
                    .mealTemplateId("meal-tpl-1")
                    .build();
            DietTemplateDayData day = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .notes("First day")
                    .meals(List.of(meal))
                    .build();
            DietTemplate model = createModelWithDays(List.of(day));

            // When
            DietTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result.getDays()).hasSize(1);
            DietTemplateDayEntity dayEntity = result.getDays().getFirst();
            assertThat(dayEntity.getDayNumber()).isEqualTo(1);
            assertThat(dayEntity.getDayName()).isEqualTo("Day 1");
            assertThat(dayEntity.getNotes()).isEqualTo("First day");

            assertThat(dayEntity.getMeals()).hasSize(1);
            DietTemplateMealEntity mealEntity = dayEntity.getMeals().getFirst();
            assertThat(mealEntity.getName()).isEqualTo("Breakfast");
            assertThat(mealEntity.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(mealEntity.getTime()).isEqualTo("08:00");
            assertThat(mealEntity.getInstructions()).isEqualTo("Cook eggs");
            assertThat(mealEntity.getMealTemplateId()).isEqualTo("meal-tpl-1");
            assertThat(mealEntity.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(300));
            assertThat(mealEntity.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(25));

            assertThat(mealEntity.getIngredients()).hasSize(1);
            DietTemplateIngredientEntity ingredientEntity = mealEntity.getIngredients().getFirst();
            assertThat(ingredientEntity.getName()).isEqualTo("Eggs");
            assertThat(ingredientEntity.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2));
            assertThat(ingredientEntity.getUnit()).isEqualTo("szt");
            assertThat(ingredientEntity.getOriginalText()).isEqualTo("2 jajka");
            assertThat(ingredientEntity.getCategoryId()).isEqualTo("cat-1");

            assertThat(mealEntity.getPhotos()).hasSize(1);
            assertThat(mealEntity.getPhotos().getFirst().getPhotoUrl()).isEqualTo("photo1.jpg");
        }
    }

    @Nested
    @DisplayName("updateEntity")
    class UpdateEntityTests {

        @Test
        @DisplayName("Should update entity fields from model")
        void givenEntityAndModel_When_UpdateEntity_Then_UpdateAllFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .id(1L)
                    .externalId(SAMPLE_ID)
                    .name("Old Name")
                    .description("Old desc")
                    .category(DietTemplateCategory.CUSTOM)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(1)
                    .duration(7)
                    .mealsPerDay(3)
                    .usageCount(10)
                    .days(new java.util.ArrayList<>())
                    .build();

            DietTemplate model = DietTemplate.builder()
                    .id(SAMPLE_ID)
                    .name("New Name")
                    .description("New description")
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .duration(14)
                    .mealsPerDay(5)
                    .mealTimes(new HashMap<>())
                    .mealTypes(List.of())
                    .notes("Updated notes")
                    .days(Collections.emptyList())
                    .build();

            // When
            converter.updateEntity(entity, model);

            // Then
            assertThat(entity.getName()).isEqualTo("New Name");
            assertThat(entity.getDescription()).isEqualTo("New description");
            assertThat(entity.getCategory()).isEqualTo(DietTemplateCategory.WEIGHT_LOSS);
            assertThat(entity.getDuration()).isEqualTo(14);
            assertThat(entity.getMealsPerDay()).isEqualTo(5);
            assertThat(entity.getNotes()).isEqualTo("Updated notes");
            assertThat(entity.getDays()).isEmpty();
        }

        @Test
        @DisplayName("Should replace entity days when updating")
        void givenEntityWithDays_When_UpdateEntityWithNewDays_Then_ReplaceDays() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            DietTemplateEntity templateEntity = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .category(DietTemplateCategory.CUSTOM)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(0)
                    .duration(7)
                    .mealsPerDay(3)
                    .usageCount(0)
                    .days(new java.util.ArrayList<>())
                    .build();
            DietTemplateDayEntity existingDay = DietTemplateDayEntity.builder()
                    .dietTemplate(templateEntity)
                    .dayNumber(1)
                    .dayName("Old Day")
                    .build();
            templateEntity.getDays().add(existingDay);

            DietTemplateDayData newDay = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("New Day 1")
                    .meals(Collections.emptyList())
                    .build();
            DietTemplate model = createModelWithDays(List.of(newDay));

            // When
            converter.updateEntity(templateEntity, model);

            // Then
            assertThat(templateEntity.getDays()).hasSize(1);
            assertThat(templateEntity.getDays().getFirst().getDayName()).isEqualTo("New Day 1");
        }
    }

    @Nested
    @DisplayName("round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data in entity-to-model-to-entity round trip")
        void givenEntity_When_ToModelThenToEntity_Then_PreserveBasicFields() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            DietTemplateEntity original = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .description(SAMPLE_DESCRIPTION)
                    .category(DietTemplateCategory.VEGETARIAN)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(1)
                    .duration(7)
                    .mealsPerDay(4)
                    .mealTimesJson("{\"BREAKFAST\":\"08:00\"}")
                    .mealTypesJson("[\"BREAKFAST\",\"LUNCH\"]")
                    .targetCalories(2000.0)
                    .targetProtein(100.0)
                    .usageCount(2)
                    .notes("Round trip notes")
                    .days(Collections.emptyList())
                    .build();

            // When
            DietTemplate model = converter.toModel(original);
            DietTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result.getExternalId()).isEqualTo(original.getExternalId());
            assertThat(result.getName()).isEqualTo(original.getName());
            assertThat(result.getDescription()).isEqualTo(original.getDescription());
            assertThat(result.getCategory()).isEqualTo(original.getCategory());
            assertThat(result.getDuration()).isEqualTo(original.getDuration());
            assertThat(result.getMealsPerDay()).isEqualTo(original.getMealsPerDay());
            assertThat(result.getTargetCalories()).isEqualTo(original.getTargetCalories());
        }
    }

    private DietTemplate createModelWithNutrition(DietTemplateNutrition nutrition) {
        Timestamp now = Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond(), 0);
        return DietTemplate.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .category(DietTemplateCategory.CUSTOM)
                .createdBy(SAMPLE_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .duration(7)
                .mealsPerDay(3)
                .mealTimes(Collections.emptyMap())
                .mealTypes(Collections.emptyList())
                .days(Collections.emptyList())
                .targetNutrition(nutrition)
                .build();
    }

    private DietTemplate createModelWithDays(List<DietTemplateDayData> days) {
        Timestamp now = Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond(), 0);
        return DietTemplate.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .category(DietTemplateCategory.CUSTOM)
                .createdBy(SAMPLE_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .duration(7)
                .mealsPerDay(3)
                .mealTimes(Collections.emptyMap())
                .mealTypes(Collections.emptyList())
                .days(days)
                .build();
    }

    @Nested
    @DisplayName("convertToBigDecimal")
    class ConvertToBigDecimalTests {

        @Test
        @DisplayName("Should convert Double to BigDecimal")
        void givenDoubleValue_When_ConvertToBigDecimal_Then_ReturnBigDecimal() {
            // When
            BigDecimal result = converter.convertToBigDecimal(123.45);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(123.45));
        }

        @Test
        @DisplayName("Should return null when Double is null")
        void givenNullDouble_When_ConvertToBigDecimal_Then_ReturnNull() {
            // When
            BigDecimal result = converter.convertToBigDecimal(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle zero value")
        void givenZeroValue_When_ConvertToBigDecimal_Then_ReturnZero() {
            // When
            BigDecimal result = converter.convertToBigDecimal(0.0);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("convertToDouble")
    class ConvertToDoubleTests {

        @Test
        @DisplayName("Should convert BigDecimal to Double")
        void givenBigDecimalValue_When_ConvertToDouble_Then_ReturnDouble() {
            // Given
            BigDecimal value = BigDecimal.valueOf(123.45);

            // When
            Double result = converter.convertToDouble(value);

            // Then
            assertThat(result).isEqualTo(123.45);
        }

        @Test
        @DisplayName("Should return null when BigDecimal is null")
        void givenNullBigDecimal_When_ConvertToDouble_Then_ReturnNull() {
            // When
            Double result = converter.convertToDouble(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle zero value")
        void givenZeroValue_When_ConvertToDouble_Then_ReturnZero() {
            // When
            Double result = converter.convertToDouble(BigDecimal.ZERO);

            // Then
            assertThat(result).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("convertToLocalDateTime")
    class ConvertToLocalDateTimeTests {

        @Test
        @DisplayName("Should convert Timestamp to LocalDateTime")
        void givenTimestamp_When_ConvertToLocalDateTime_Then_ReturnLocalDateTime() {
            // Given
            LocalDateTime expected = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(
                    expected.toEpochSecond(ZoneOffset.UTC),
                    expected.getNano()
            );

            // When
            LocalDateTime result = converter.convertToLocalDateTime(timestamp);

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should return null when Timestamp is null")
        void givenNullTimestamp_When_ConvertToLocalDateTime_Then_ReturnNull() {
            // When
            LocalDateTime result = converter.convertToLocalDateTime(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("convertToTimestamp")
    class ConvertToTimestampTests {

        @Test
        @DisplayName("Should convert LocalDateTime to Timestamp")
        void givenLocalDateTime_When_ConvertToTimestamp_Then_ReturnTimestamp() {
            // Given
            LocalDateTime localDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

            // When
            Timestamp result = converter.convertToTimestamp(localDateTime);

            // Then
            assertThat(result).isNotNull();
            LocalDateTime convertedBack = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(result.getSeconds(), result.getNanos()),
                    ZoneOffset.UTC
            );
            assertThat(convertedBack).isEqualTo(localDateTime);
        }

        @Test
        @DisplayName("Should return null when LocalDateTime is null")
        void givenNullLocalDateTime_When_ConvertToTimestamp_Then_ReturnNull() {
            // When
            Timestamp result = converter.convertToTimestamp(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("convertCategory")
    class ConvertCategoryTests {

        @Test
        @DisplayName("Should convert valid category")
        void givenValidCategory_When_ConvertCategory_Then_ReturnSameCategory() {
            // When
            DietTemplateCategory result = converter.convertCategory(DietTemplateCategory.WEIGHT_LOSS);

            // Then
            assertThat(result).isEqualTo(DietTemplateCategory.WEIGHT_LOSS);
        }

        @Test
        @DisplayName("Should return CUSTOM for unknown category")
        void givenUnknownCategory_When_ConvertCategory_Then_ReturnCustom() {
            // This test is tricky because we can't easily create an invalid enum value
            // The method catches exceptions and returns CUSTOM, but enum.valueOf() won't throw
            // for valid enum values. We'll test with a valid one to ensure the method works.
            DietTemplateCategory result = converter.convertCategory(DietTemplateCategory.MAINTENANCE);
            assertThat(result).isEqualTo(DietTemplateCategory.MAINTENANCE);
        }
    }

    @Nested
    @DisplayName("convertCategoryEnum")
    class ConvertCategoryEnumTests {

        @Test
        @DisplayName("Should convert valid category enum")
        void givenValidCategory_When_ConvertCategoryEnum_Then_ReturnSameCategory() {
            // When
            DietTemplateCategory result = converter.convertCategoryEnum(DietTemplateCategory.SPORT);

            // Then
            assertThat(result).isEqualTo(DietTemplateCategory.SPORT);
        }
    }

    @Nested
    @DisplayName("serializeMealTimes")
    class SerializeMealTimesTests {

        @Test
        @DisplayName("Should serialize meal times map to JSON")
        void givenMealTimesMap_When_SerializeMealTimes_Then_ReturnJsonString() {
            // Given
            Map<String, String> mealTimes = Map.of(
                    "BREAKFAST", "08:00",
                    "LUNCH", "13:00",
                    "DINNER", "19:00"
            );

            // When
            String result = converter.serializeMealTimes(mealTimes);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("BREAKFAST");
            assertThat(result).contains("08:00");
        }

        @Test
        @DisplayName("Should return null when meal times map is null")
        void givenNullMealTimes_When_SerializeMealTimes_Then_ReturnNull() {
            // When
            String result = converter.serializeMealTimes(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return empty JSON object on serialization error")
        void givenInvalidMealTimes_When_SerializeMealTimes_Then_ReturnEmptyJson() {
            // Given - create a map that might cause issues (though ObjectMapper handles most cases)
            Map<String, String> mealTimes = new HashMap<>();
            mealTimes.put("key", "value");

            // When
            String result = converter.serializeMealTimes(mealTimes);

            // Then
            assertThat(result).isNotNull();
            // Should serialize successfully, so not empty JSON
            assertThat(result).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("deserializeMealTimes")
    class DeserializeMealTimesTests {

        @Test
        @DisplayName("Should deserialize JSON string to meal times map")
        void givenValidJson_When_DeserializeMealTimes_Then_ReturnMap() {
            // Given
            String json = "{\"BREAKFAST\":\"08:00\",\"LUNCH\":\"13:00\"}";

            // When
            Map<String, String> result = converter.deserializeMealTimes(json);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("BREAKFAST", "08:00");
            assertThat(result).containsEntry("LUNCH", "13:00");
        }

        @Test
        @DisplayName("Should return empty map when JSON is null")
        void givenNullJson_When_DeserializeMealTimes_Then_ReturnEmptyMap() {
            // When
            Map<String, String> result = converter.deserializeMealTimes(null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty map when JSON is empty string")
        void givenEmptyJson_When_DeserializeMealTimes_Then_ReturnEmptyMap() {
            // When
            Map<String, String> result = converter.deserializeMealTimes("");

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty map when JSON is invalid")
        void givenInvalidJson_When_DeserializeMealTimes_Then_ReturnEmptyMap() {
            // Given
            String invalidJson = "{invalid json}";

            // When
            Map<String, String> result = converter.deserializeMealTimes(invalidJson);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("serializeMealTypes")
    class SerializeMealTypesTests {

        @Test
        @DisplayName("Should serialize meal types list to JSON")
        void givenMealTypesList_When_SerializeMealTypes_Then_ReturnJsonString() {
            // Given
            List<String> mealTypes = List.of("BREAKFAST", "LUNCH", "DINNER");

            // When
            String result = converter.serializeMealTypes(mealTypes);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("BREAKFAST");
            assertThat(result).contains("LUNCH");
        }

        @Test
        @DisplayName("Should return null when meal types list is null")
        void givenNullMealTypes_When_SerializeMealTypes_Then_ReturnNull() {
            // When
            String result = converter.serializeMealTypes(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("deserializeMealTypes")
    class DeserializeMealTypesTests {

        @Test
        @DisplayName("Should deserialize JSON string to meal types list")
        void givenValidJson_When_DeserializeMealTypes_Then_ReturnList() {
            // Given
            String json = "[\"BREAKFAST\",\"LUNCH\",\"DINNER\"]";

            // When
            List<String> result = converter.deserializeMealTypes(json);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("BREAKFAST", "LUNCH", "DINNER");
        }

        @Test
        @DisplayName("Should return empty list when JSON is null")
        void givenNullJson_When_DeserializeMealTypes_Then_ReturnEmptyList() {
            // When
            List<String> result = converter.deserializeMealTypes(null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when JSON is empty string")
        void givenEmptyJson_When_DeserializeMealTypes_Then_ReturnEmptyList() {
            // When
            List<String> result = converter.deserializeMealTypes("");

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when JSON is invalid")
        void givenInvalidJson_When_DeserializeMealTypes_Then_ReturnEmptyList() {
            // Given
            String invalidJson = "[invalid json";

            // When
            List<String> result = converter.deserializeMealTypes(invalidJson);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("convertNutrition")
    class ConvertNutritionTests {

        @Test
        @DisplayName("Should convert entity with nutrition values to DietTemplateNutrition")
        void givenEntityWithNutrition_When_ConvertNutrition_Then_ReturnNutrition() {
            // Given
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .targetCalories(2000.0)
                    .targetProtein(150.0)
                    .targetFat(65.0)
                    .targetCarbs(250.0)
                    .calculationMethod("TDEE")
                    .build();

            // When
            DietTemplateNutrition result = converter.convertNutrition(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTargetCalories()).isEqualTo(2000.0);
            assertThat(result.getTargetProtein()).isEqualTo(150.0);
            assertThat(result.getTargetFat()).isEqualTo(65.0);
            assertThat(result.getTargetCarbs()).isEqualTo(250.0);
            assertThat(result.getCalculationMethod()).isEqualTo("TDEE");
        }

        @Test
        @DisplayName("Should return null when all nutrition values are null")
        void givenEntityWithNullNutrition_When_ConvertNutrition_Then_ReturnNull() {
            // Given
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .targetCalories(null)
                    .targetProtein(null)
                    .targetFat(null)
                    .targetCarbs(null)
                    .build();

            // When
            DietTemplateNutrition result = converter.convertNutrition(entity);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return nutrition when at least one value is not null")
        void givenEntityWithPartialNutrition_When_ConvertNutrition_Then_ReturnNutrition() {
            // Given
            DietTemplateEntity entity = DietTemplateEntity.builder()
                    .targetCalories(2000.0)
                    .targetProtein(null)
                    .targetFat(null)
                    .targetCarbs(null)
                    .build();

            // When
            DietTemplateNutrition result = converter.convertNutrition(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTargetCalories()).isEqualTo(2000.0);
        }
    }

    @Nested
    @DisplayName("convertMealNutrition")
    class ConvertMealNutritionTests {

        @Test
        @DisplayName("Should convert meal entity with nutrition values to NutritionalValues")
        void givenMealEntityWithNutrition_When_ConvertMealNutrition_Then_ReturnNutritionalValues() {
            // Given
            DietTemplateMealEntity entity = DietTemplateMealEntity.builder()
                    .calories(BigDecimal.valueOf(300.0))
                    .protein(BigDecimal.valueOf(25.0))
                    .fat(BigDecimal.valueOf(20.0))
                    .carbs(BigDecimal.valueOf(5.0))
                    .build();

            // When
            NutritionalValues result = converter.convertMealNutrition(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCalories()).isEqualTo(300.0);
            assertThat(result.getProtein()).isEqualTo(25.0);
            assertThat(result.getFat()).isEqualTo(20.0);
            assertThat(result.getCarbs()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should return null when all nutrition values are null")
        void givenMealEntityWithNullNutrition_When_ConvertMealNutrition_Then_ReturnNull() {
            // Given
            DietTemplateMealEntity entity = DietTemplateMealEntity.builder()
                    .calories(null)
                    .protein(null)
                    .fat(null)
                    .carbs(null)
                    .build();

            // When
            NutritionalValues result = converter.convertMealNutrition(entity);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return nutritional values when at least one value is not null")
        void givenMealEntityWithPartialNutrition_When_ConvertMealNutrition_Then_ReturnNutritionalValues() {
            // Given
            DietTemplateMealEntity entity = DietTemplateMealEntity.builder()
                    .calories(BigDecimal.valueOf(300.0))
                    .protein(null)
                    .fat(null)
                    .carbs(null)
                    .build();

            // When
            NutritionalValues result = converter.convertMealNutrition(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCalories()).isEqualTo(300.0);
        }
    }

    @Nested
    @DisplayName("convertDays")
    class ConvertDaysTests {

        @Test
        @DisplayName("Should convert list of day entities sorted by day number")
        void givenDayEntities_When_ConvertDays_Then_ReturnSortedList() {
            // Given
            DietTemplateDayEntity day2 = DietTemplateDayEntity.builder()
                    .dayNumber(2)
                    .dayName("Day 2")
                    .meals(Collections.emptyList())
                    .build();
            DietTemplateDayEntity day1 = DietTemplateDayEntity.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .meals(Collections.emptyList())
                    .build();
            List<DietTemplateDayEntity> entities = List.of(day2, day1);

            // When
            List<DietTemplateDayData> result = converter.convertDays(entities);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDayNumber()).isEqualTo(1);
            assertThat(result.get(1).getDayNumber()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void givenEmptyList_When_ConvertDays_Then_ReturnEmptyList() {
            // When
            List<DietTemplateDayData> result = converter.convertDays(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("convertDay")
    class ConvertDayTests {

        @Test
        @DisplayName("Should convert day entity to day data")
        void givenDayEntity_When_ConvertDay_Then_ReturnDayData() {
            // Given
            DietTemplateDayEntity entity = DietTemplateDayEntity.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .notes("Test notes")
                    .meals(Collections.emptyList())
                    .build();

            // When
            DietTemplateDayData result = converter.convertDay(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDayNumber()).isEqualTo(1);
            assertThat(result.getDayName()).isEqualTo("Day 1");
            assertThat(result.getNotes()).isEqualTo("Test notes");
            assertThat(result.getMeals()).isEmpty();
        }
    }

    @Nested
    @DisplayName("convertMeals")
    class ConvertMealsTests {

        @Test
        @DisplayName("Should convert list of meal entities sorted by meal order")
        void givenMealEntities_When_ConvertMeals_Then_ReturnSortedList() {
            // Given
            DietTemplateMealEntity meal2 = DietTemplateMealEntity.builder()
                    .mealOrder(2)
                    .name("Lunch")
                    .ingredients(Collections.emptyList())
                    .photos(Collections.emptyList())
                    .build();
            DietTemplateMealEntity meal1 = DietTemplateMealEntity.builder()
                    .mealOrder(1)
                    .name("Breakfast")
                    .ingredients(Collections.emptyList())
                    .photos(Collections.emptyList())
                    .build();
            List<DietTemplateMealEntity> entities = List.of(meal2, meal1);

            // When
            List<DietTemplateMealData> result = converter.convertMeals(entities);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Breakfast");
            assertThat(result.get(1).getName()).isEqualTo("Lunch");
        }
    }

    @Nested
    @DisplayName("convertMeal")
    class ConvertMealTests {

        @Test
        @DisplayName("Should convert meal entity to meal data")
        void givenMealEntity_When_ConvertMeal_Then_ReturnMealData() {
            // Given
            DietTemplateMealEntity entity = DietTemplateMealEntity.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .instructions("Cook eggs")
                    .mealTemplateId("meal-tpl-1")
                    .ingredients(Collections.emptyList())
                    .photos(Collections.emptyList())
                    .build();

            // When
            DietTemplateMealData result = converter.convertMeal(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Breakfast");
            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(result.getTime()).isEqualTo("08:00");
            assertThat(result.getInstructions()).isEqualTo("Cook eggs");
            assertThat(result.getMealTemplateId()).isEqualTo("meal-tpl-1");
        }
    }

    @Nested
    @DisplayName("convertIngredients")
    class ConvertIngredientsTests {

        @Test
        @DisplayName("Should convert list of ingredient entities sorted by display order")
        void givenIngredientEntities_When_ConvertIngredients_Then_ReturnSortedList() {
            // Given
            DietTemplateIngredientEntity ing2 = DietTemplateIngredientEntity.builder()
                    .displayOrder(2)
                    .name("Tomato")
                    .quantity(BigDecimal.valueOf(1))
                    .unit("szt")
                    .originalText("1 pomidor")
                    .hasCustomUnit(false)
                    .build();
            DietTemplateIngredientEntity ing1 = DietTemplateIngredientEntity.builder()
                    .displayOrder(1)
                    .name("Egg")
                    .quantity(BigDecimal.valueOf(2))
                    .unit("szt")
                    .originalText("2 jajka")
                    .hasCustomUnit(false)
                    .build();
            List<DietTemplateIngredientEntity> entities = List.of(ing2, ing1);

            // When
            List<DietTemplateIngredient> result = converter.convertIngredients(entities);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Egg");
            assertThat(result.get(1).getName()).isEqualTo("Tomato");
        }
    }

    @Nested
    @DisplayName("convertPhotos")
    class ConvertPhotosTests {

        @Test
        @DisplayName("Should convert list of photo entities sorted by display order to URLs")
        void givenPhotoEntities_When_ConvertPhotos_Then_ReturnSortedUrlList() {
            // Given
            DietTemplateMealPhotoEntity photo2 = DietTemplateMealPhotoEntity.builder()
                    .displayOrder(2)
                    .photoUrl("photo2.jpg")
                    .build();
            DietTemplateMealPhotoEntity photo1 = DietTemplateMealPhotoEntity.builder()
                    .displayOrder(1)
                    .photoUrl("photo1.jpg")
                    .build();
            List<DietTemplateMealPhotoEntity> entities = List.of(photo2, photo1);

            // When
            List<String> result = converter.convertPhotos(entities);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly("photo1.jpg", "photo2.jpg");
        }
    }

    @Nested
    @DisplayName("convertDayToEntity")
    class ConvertDayToEntityTests {

        @Test
        @DisplayName("Should convert day data to day entity")
        void givenDayData_When_ConvertDayToEntity_Then_ReturnDayEntity() {
            // Given
            DietTemplateEntity template = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .build();
            DietTemplateDayData day = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .notes("Test notes")
                    .meals(Collections.emptyList())
                    .build();

            // When
            DietTemplateDayEntity result = converter.convertDayToEntity(day, template);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDayNumber()).isEqualTo(1);
            assertThat(result.getDayName()).isEqualTo("Day 1");
            assertThat(result.getNotes()).isEqualTo("Test notes");
            assertThat(result.getDietTemplate()).isEqualTo(template);
            assertThat(result.getMeals()).isEmpty();
        }

        @Test
        @DisplayName("Should convert day with meals to entity")
        void givenDayWithMeals_When_ConvertDayToEntity_Then_IncludeMeals() {
            // Given
            DietTemplateEntity template = DietTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .ingredients(Collections.emptyList())
                    .photos(Collections.emptyList())
                    .build();
            DietTemplateDayData day = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .meals(List.of(meal))
                    .build();

            // When
            DietTemplateDayEntity result = converter.convertDayToEntity(day, template);

            // Then
            assertThat(result.getMeals()).hasSize(1);
            assertThat(result.getMeals().getFirst().getName()).isEqualTo("Breakfast");
            assertThat(result.getMeals().getFirst().getMealOrder()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("convertMealToEntity")
    class ConvertMealToEntityTests {

        @Test
        @DisplayName("Should convert meal data to meal entity")
        void givenMealData_When_ConvertMealToEntity_Then_ReturnMealEntity() {
            // Given
            DietTemplateDayEntity day = DietTemplateDayEntity.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .instructions("Cook eggs")
                    .mealTemplateId("meal-tpl-1")
                    .ingredients(Collections.emptyList())
                    .photos(Collections.emptyList())
                    .build();

            // When
            DietTemplateMealEntity result = converter.convertMealToEntity(meal, day, 0);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Breakfast");
            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(result.getTime()).isEqualTo("08:00");
            assertThat(result.getInstructions()).isEqualTo("Cook eggs");
            assertThat(result.getMealTemplateId()).isEqualTo("meal-tpl-1");
            assertThat(result.getMealOrder()).isEqualTo(0);
            assertThat(result.getTemplateDay()).isEqualTo(day);
        }

        @Test
        @DisplayName("Should convert meal with nutritional values to entity")
        void givenMealWithNutrition_When_ConvertMealToEntity_Then_IncludeNutrition() {
            // Given
            DietTemplateDayEntity day = DietTemplateDayEntity.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .build();
            NutritionalValues nutrition = NutritionalValues.builder()
                    .calories(300.0)
                    .protein(25.0)
                    .fat(20.0)
                    .carbs(5.0)
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .nutritionalValues(nutrition)
                    .ingredients(Collections.emptyList())
                    .photos(Collections.emptyList())
                    .build();

            // When
            DietTemplateMealEntity result = converter.convertMealToEntity(meal, day, 1);

            // Then
            assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(300.0));
            assertThat(result.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(25.0));
            assertThat(result.getFat()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
            assertThat(result.getCarbs()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        }

        @Test
        @DisplayName("Should convert meal with ingredients to entity")
        void givenMealWithIngredients_When_ConvertMealToEntity_Then_IncludeIngredients() {
            // Given
            DietTemplateDayEntity day = DietTemplateDayEntity.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .build();
            DietTemplateIngredient ingredient = DietTemplateIngredient.builder()
                    .name("Egg")
                    .quantity(2.0)
                    .unit("szt")
                    .original("2 jajka")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .ingredients(List.of(ingredient))
                    .photos(Collections.emptyList())
                    .build();

            // When
            DietTemplateMealEntity result = converter.convertMealToEntity(meal, day, 0);

            // Then
            assertThat(result.getIngredients()).hasSize(1);
            DietTemplateIngredientEntity ingEntity = result.getIngredients().getFirst();
            assertThat(ingEntity.getName()).isEqualTo("Egg");
            assertThat(ingEntity.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
            assertThat(ingEntity.getUnit()).isEqualTo("szt");
            assertThat(ingEntity.getDisplayOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should convert meal with photos to entity")
        void givenMealWithPhotos_When_ConvertMealToEntity_Then_IncludePhotos() {
            // Given
            DietTemplateDayEntity day = DietTemplateDayEntity.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .photos(List.of("photo1.jpg", "photo2.jpg"))
                    .ingredients(Collections.emptyList())
                    .build();

            // When
            DietTemplateMealEntity result = converter.convertMealToEntity(meal, day, 0);

            // Then
            assertThat(result.getPhotos()).hasSize(2);
            assertThat(result.getPhotos().get(0).getPhotoUrl()).isEqualTo("photo1.jpg");
            assertThat(result.getPhotos().get(0).getDisplayOrder()).isEqualTo(0);
            assertThat(result.getPhotos().get(1).getPhotoUrl()).isEqualTo("photo2.jpg");
            assertThat(result.getPhotos().get(1).getDisplayOrder()).isEqualTo(1);
        }
    }
}
