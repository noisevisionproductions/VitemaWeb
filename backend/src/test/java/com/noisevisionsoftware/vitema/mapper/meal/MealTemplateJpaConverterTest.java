package com.noisevisionsoftware.vitema.mapper.meal;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.meal.MealIngredient;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateEntity;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateIngredientEntity;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplatePhotoEntity;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MealTemplateJpaConverterTest {

    private MealTemplateJpaConverter converter;
    private static final String SAMPLE_ID = "mt_abc123def4567890";
    private static final String SAMPLE_NAME = "Test Meal Template";
    private static final String SAMPLE_INSTRUCTIONS = "Test instructions";
    private static final String SAMPLE_CREATED_BY = "user-123";

    @BeforeEach
    void setUp() {
        converter = new MealTemplateJpaConverter();
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
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .instructions(SAMPLE_INSTRUCTIONS)
                    .mealType("BREAKFAST")
                    .category("Quick")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .lastUsed(now)
                    .usageCount(5)
                    .photos(Collections.emptyList())
                    .ingredients(Collections.emptyList())
                    .build();

            // When
            MealTemplate result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getName()).isEqualTo(SAMPLE_NAME);
            assertThat(result.getInstructions()).isEqualTo(SAMPLE_INSTRUCTIONS);
            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(result.getCategory()).isEqualTo("Quick");
            assertThat(result.getCreatedBy()).isEqualTo(SAMPLE_CREATED_BY);
            assertThat(result.getUsageCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should convert entity with nutritional values to model")
        void givenEntityWithNutrition_When_ToModel_Then_IncludeNutrition() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .mealType("LUNCH")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(0)
                    .calories(BigDecimal.valueOf(500.0))
                    .protein(BigDecimal.valueOf(30.0))
                    .fat(BigDecimal.valueOf(20.0))
                    .carbs(BigDecimal.valueOf(50.0))
                    .photos(Collections.emptyList())
                    .ingredients(Collections.emptyList())
                    .build();

            // When
            MealTemplate result = converter.toModel(entity);

            // Then
            assertThat(result.getNutritionalValues()).isNotNull();
            assertThat(result.getNutritionalValues().getCalories()).isEqualTo(500.0);
            assertThat(result.getNutritionalValues().getProtein()).isEqualTo(30.0);
            assertThat(result.getNutritionalValues().getFat()).isEqualTo(20.0);
            assertThat(result.getNutritionalValues().getCarbs()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should convert entity with photos to model")
        void givenEntityWithPhotos_When_ToModel_Then_IncludePhotos() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .mealType("DINNER")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(0)
                    .ingredients(Collections.emptyList())
                    .build();

            List<MealTemplatePhotoEntity> photos = List.of(
                    MealTemplatePhotoEntity.builder()
                            .mealTemplate(entity)
                            .photoUrl("photo1.jpg")
                            .displayOrder(0)
                            .build(),
                    MealTemplatePhotoEntity.builder()
                            .mealTemplate(entity)
                            .photoUrl("photo2.jpg")
                            .displayOrder(1)
                            .build()
            );
            entity.setPhotos(photos);

            // When
            MealTemplate result = converter.toModel(entity);

            // Then
            assertThat(result.getPhotos()).hasSize(2);
            assertThat(result.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
        }

        @Test
        @DisplayName("Should convert entity with ingredients to model")
        void givenEntityWithIngredients_When_ToModel_Then_IncludeIngredients() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .mealType("SNACK")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(0)
                    .photos(Collections.emptyList())
                    .build();

            List<MealTemplateIngredientEntity> ingredients = List.of(
                    MealTemplateIngredientEntity.builder()
                            .id(1L)
                            .mealTemplate(entity)
                            .name("Egg")
                            .quantity(BigDecimal.valueOf(2.0))
                            .unit("szt")
                            .originalText("2 eggs")
                            .categoryId("cat-1")
                            .hasCustomUnit(false)
                            .displayOrder(0)
                            .build()
            );
            entity.setIngredients(ingredients);

            // When
            MealTemplate result = converter.toModel(entity);

            // Then
            assertThat(result.getIngredients()).hasSize(1);
            MealIngredient ingredient = result.getIngredients().getFirst();
            assertThat(ingredient.getId()).isEqualTo("1");
            assertThat(ingredient.getName()).isEqualTo("Egg");
            assertThat(ingredient.getQuantity()).isEqualTo(2.0);
            assertThat(ingredient.getUnit()).isEqualTo("szt");
            assertThat(ingredient.getOriginal()).isEqualTo("2 eggs");
            assertThat(ingredient.getCategoryId()).isEqualTo("cat-1");
            assertThat(ingredient.isHasCustomUnit()).isFalse();
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
            MealTemplate model = MealTemplate.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .instructions(SAMPLE_INSTRUCTIONS)
                    .mealType(MealType.BREAKFAST)
                    .category("Quick")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .lastUsed(now)
                    .usageCount(3)
                    .photos(Collections.emptyList())
                    .ingredients(Collections.emptyList())
                    .build();

            // When
            MealTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getExternalId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getName()).isEqualTo(SAMPLE_NAME);
            assertThat(result.getInstructions()).isEqualTo(SAMPLE_INSTRUCTIONS);
            assertThat(result.getMealType()).isEqualTo("BREAKFAST");
            assertThat(result.getCategory()).isEqualTo("Quick");
            assertThat(result.getCreatedBy()).isEqualTo(SAMPLE_CREATED_BY);
            assertThat(result.getUsageCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should convert model with nutritional values to entity")
        void givenModelWithNutrition_When_ToEntity_Then_IncludeNutrition() {
            // Given
            NutritionalValues nutrition = NutritionalValues.builder()
                    .calories(600.0)
                    .protein(40.0)
                    .fat(25.0)
                    .carbs(60.0)
                    .build();
            MealTemplate model = createModelWithNutrition(nutrition);

            // When
            MealTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(600.0));
            assertThat(result.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
            assertThat(result.getFat()).isEqualByComparingTo(BigDecimal.valueOf(25.0));
            assertThat(result.getCarbs()).isEqualByComparingTo(BigDecimal.valueOf(60.0));
        }

        @Test
        @DisplayName("Should convert model with photos to entity")
        void givenModelWithPhotos_When_ToEntity_Then_IncludePhotos() {
            // Given
            MealTemplate model = createModelWithPhotos(List.of("photo1.jpg", "photo2.jpg"));

            // When
            MealTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result.getPhotos()).hasSize(2);
            assertThat(result.getPhotos().get(0).getPhotoUrl()).isEqualTo("photo1.jpg");
            assertThat(result.getPhotos().get(0).getDisplayOrder()).isEqualTo(0);
            assertThat(result.getPhotos().get(1).getPhotoUrl()).isEqualTo("photo2.jpg");
            assertThat(result.getPhotos().get(1).getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should convert model with ingredients to entity")
        void givenModelWithIngredients_When_ToEntity_Then_IncludeIngredients() {
            // Given
            MealIngredient ingredient = MealIngredient.builder()
                    .name("Tomato")
                    .quantity(3.0)
                    .unit("szt")
                    .original("3 tomatoes")
                    .categoryId("cat-2")
                    .hasCustomUnit(false)
                    .build();
            MealTemplate model = createModelWithIngredients(List.of(ingredient));

            // When
            MealTemplateEntity result = converter.toEntity(model);

            // Then
            assertThat(result.getIngredients()).hasSize(1);
            MealTemplateIngredientEntity ingEntity = result.getIngredients().getFirst();
            assertThat(ingEntity.getName()).isEqualTo("Tomato");
            assertThat(ingEntity.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
            assertThat(ingEntity.getUnit()).isEqualTo("szt");
            assertThat(ingEntity.getOriginalText()).isEqualTo("3 tomatoes");
            assertThat(ingEntity.getCategoryId()).isEqualTo("cat-2");
            assertThat(ingEntity.getDisplayOrder()).isEqualTo(0);
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
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .id(1L)
                    .externalId(SAMPLE_ID)
                    .name("Old Name")
                    .instructions("Old instructions")
                    .mealType("BREAKFAST")
                    .category("Old Category")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(10)
                    .photos(new ArrayList<>())
                    .ingredients(new ArrayList<>())
                    .build();

            MealTemplate model = MealTemplate.builder()
                    .id(SAMPLE_ID)
                    .name("New Name")
                    .instructions("New instructions")
                    .mealType(MealType.LUNCH)
                    .category("New Category")
                    .photos(Collections.emptyList())
                    .ingredients(Collections.emptyList())
                    .build();

            // When
            converter.updateEntity(entity, model);

            // Then
            assertThat(entity.getName()).isEqualTo("New Name");
            assertThat(entity.getInstructions()).isEqualTo("New instructions");
            assertThat(entity.getMealType()).isEqualTo("LUNCH");
            assertThat(entity.getCategory()).isEqualTo("New Category");
        }

        @Test
        @DisplayName("Should update nutritional values")
        void givenEntityAndModelWithNutrition_When_UpdateEntity_Then_UpdateNutrition() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .id(1L)
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .mealType("BREAKFAST")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(0)
                    .calories(BigDecimal.valueOf(400.0))
                    .protein(BigDecimal.valueOf(20.0))
                    .photos(new ArrayList<>())
                    .ingredients(new ArrayList<>())
                    .build();

            NutritionalValues nutrition = NutritionalValues.builder()
                    .calories(700.0)
                    .protein(50.0)
                    .fat(30.0)
                    .carbs(70.0)
                    .build();
            MealTemplate model = createModelWithNutrition(nutrition);

            // When
            converter.updateEntity(entity, model);

            // Then
            assertThat(entity.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(700.0));
            assertThat(entity.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
            assertThat(entity.getFat()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
            assertThat(entity.getCarbs()).isEqualByComparingTo(BigDecimal.valueOf(70.0));
        }

        @Test
        @DisplayName("Should replace photos when updating")
        void givenEntityWithPhotos_When_UpdateEntity_Then_ReplacePhotos() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .id(1L)
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .mealType("DINNER")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(0)
                    .photos(new ArrayList<>())
                    .ingredients(new ArrayList<>())
                    .build();
            entity.getPhotos().add(MealTemplatePhotoEntity.builder()
                    .mealTemplate(entity)
                    .photoUrl("old_photo.jpg")
                    .displayOrder(0)
                    .build());

            MealTemplate model = createModelWithPhotos(List.of("new_photo1.jpg", "new_photo2.jpg"));

            // When
            converter.updateEntity(entity, model);

            // Then
            assertThat(entity.getPhotos()).hasSize(2);
            assertThat(entity.getPhotos().get(0).getPhotoUrl()).isEqualTo("new_photo1.jpg");
            assertThat(entity.getPhotos().get(1).getPhotoUrl()).isEqualTo("new_photo2.jpg");
        }

        @Test
        @DisplayName("Should replace ingredients when updating")
        void givenEntityWithIngredients_When_UpdateEntity_Then_ReplaceIngredients() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .id(1L)
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .mealType("SNACK")
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .usageCount(0)
                    .photos(new ArrayList<>())
                    .ingredients(new ArrayList<>())
                    .build();
            entity.getIngredients().add(MealTemplateIngredientEntity.builder()
                    .id(1L)
                    .mealTemplate(entity)
                    .name("Old Ingredient")
                    .quantity(BigDecimal.ONE)
                    .unit("szt")
                    .displayOrder(0)
                    .build());

            MealIngredient newIngredient = MealIngredient.builder()
                    .name("New Ingredient")
                    .quantity(5.0)
                    .unit("g")
                    .original("5g new ingredient")
                    .categoryId("cat-3")
                    .hasCustomUnit(false)
                    .build();
            MealTemplate model = createModelWithIngredients(List.of(newIngredient));

            // When
            converter.updateEntity(entity, model);

            // Then
            assertThat(entity.getIngredients()).hasSize(1);
            assertThat(entity.getIngredients().getFirst().getName()).isEqualTo("New Ingredient");
            assertThat(entity.getIngredients().getFirst().getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
        }
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
    @DisplayName("convertNutritionalValues")
    class ConvertNutritionalValuesTests {

        @Test
        @DisplayName("Should convert entity with nutritional values to NutritionalValues")
        void givenEntityWithNutrition_When_ConvertNutritionalValues_Then_ReturnNutritionalValues() {
            // Given
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .calories(BigDecimal.valueOf(500.0))
                    .protein(BigDecimal.valueOf(30.0))
                    .fat(BigDecimal.valueOf(20.0))
                    .carbs(BigDecimal.valueOf(50.0))
                    .build();

            // When
            NutritionalValues result = converter.convertNutritionalValues(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCalories()).isEqualTo(500.0);
            assertThat(result.getProtein()).isEqualTo(30.0);
            assertThat(result.getFat()).isEqualTo(20.0);
            assertThat(result.getCarbs()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Should return null when all nutritional values are null")
        void givenEntityWithNullNutrition_When_ConvertNutritionalValues_Then_ReturnNull() {
            // Given
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .calories(null)
                    .protein(null)
                    .fat(null)
                    .carbs(null)
                    .build();

            // When
            NutritionalValues result = converter.convertNutritionalValues(entity);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return nutritional values when at least one value is not null")
        void givenEntityWithPartialNutrition_When_ConvertNutritionalValues_Then_ReturnNutritionalValues() {
            // Given
            MealTemplateEntity entity = MealTemplateEntity.builder()
                    .calories(BigDecimal.valueOf(500.0))
                    .protein(null)
                    .fat(null)
                    .carbs(null)
                    .build();

            // When
            NutritionalValues result = converter.convertNutritionalValues(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCalories()).isEqualTo(500.0);
        }
    }

    @Nested
    @DisplayName("convertPhotos")
    class ConvertPhotosTests {

        @Test
        @DisplayName("Should convert list of photo entities sorted by display order to URLs")
        void givenPhotoEntities_When_ConvertPhotos_Then_ReturnSortedUrlList() {
            // Given
            MealTemplatePhotoEntity photo2 = MealTemplatePhotoEntity.builder()
                    .photoUrl("photo2.jpg")
                    .displayOrder(1)
                    .build();
            MealTemplatePhotoEntity photo1 = MealTemplatePhotoEntity.builder()
                    .photoUrl("photo1.jpg")
                    .displayOrder(0)
                    .build();
            MealTemplatePhotoEntity photo3 = MealTemplatePhotoEntity.builder()
                    .photoUrl("photo3.jpg")
                    .displayOrder(2)
                    .build();
            List<MealTemplatePhotoEntity> entities = List.of(photo2, photo1, photo3);

            // When
            List<String> result = converter.convertPhotos(entities);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("photo1.jpg", "photo2.jpg", "photo3.jpg");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void givenEmptyList_When_ConvertPhotos_Then_ReturnEmptyList() {
            // When
            List<String> result = converter.convertPhotos(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("convertIngredients")
    class ConvertIngredientsTests {

        @Test
        @DisplayName("Should convert list of ingredient entities sorted by display order")
        void givenIngredientEntities_When_ConvertIngredients_Then_ReturnSortedList() {
            // Given
            MealTemplateIngredientEntity ing2 = MealTemplateIngredientEntity.builder()
                    .id(2L)
                    .name("Tomato")
                    .quantity(BigDecimal.valueOf(1))
                    .unit("szt")
                    .originalText("1 tomato")
                    .categoryId("cat-2")
                    .hasCustomUnit(false)
                    .displayOrder(1)
                    .build();
            MealTemplateIngredientEntity ing1 = MealTemplateIngredientEntity.builder()
                    .id(1L)
                    .name("Egg")
                    .quantity(BigDecimal.valueOf(2))
                    .unit("szt")
                    .originalText("2 eggs")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .displayOrder(0)
                    .build();
            List<MealTemplateIngredientEntity> entities = List.of(ing2, ing1);

            // When
            List<MealIngredient> result = converter.convertIngredients(entities);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Egg");
            assertThat(result.get(0).getId()).isEqualTo("1");
            assertThat(result.get(1).getName()).isEqualTo("Tomato");
            assertThat(result.get(1).getId()).isEqualTo("2");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void givenEmptyList_When_ConvertIngredients_Then_ReturnEmptyList() {
            // When
            List<MealIngredient> result = converter.convertIngredients(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }
    }

    // Helper methods
    private MealTemplate createModelWithNutrition(NutritionalValues nutrition) {
        Timestamp now = Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond(), 0);
        return MealTemplate.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .instructions(SAMPLE_INSTRUCTIONS)
                .mealType(MealType.BREAKFAST)
                .category("Test")
                .createdBy(SAMPLE_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .usageCount(0)
                .nutritionalValues(nutrition)
                .photos(Collections.emptyList())
                .ingredients(Collections.emptyList())
                .build();
    }

    private MealTemplate createModelWithPhotos(List<String> photos) {
        Timestamp now = Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond(), 0);
        return MealTemplate.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .mealType(MealType.BREAKFAST)
                .createdBy(SAMPLE_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .usageCount(0)
                .photos(photos)
                .ingredients(Collections.emptyList())
                .build();
    }

    private MealTemplate createModelWithIngredients(List<MealIngredient> ingredients) {
        Timestamp now = Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond(), 0);
        return MealTemplate.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .mealType(MealType.BREAKFAST)
                .createdBy(SAMPLE_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .usageCount(0)
                .photos(Collections.emptyList())
                .ingredients(ingredients)
                .build();
    }
}
