package com.noisevisionsoftware.vitema.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeReferenceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeReferenceJpaConverterTest {

    private RecipeReferenceJpaConverter converter;
    private static final String SAMPLE_ID = "ref-123";
    private static final String SAMPLE_RECIPE_ID = "recipe-456";
    private static final String SAMPLE_DIET_ID = "diet-789";
    private static final String SAMPLE_USER_ID = "user-abc";

    @BeforeEach
    void setUp() {
        converter = new RecipeReferenceJpaConverter();
    }

    @Nested
    @DisplayName("toJpaEntity")
    class ToJpaEntityTests {

        @Test
        @DisplayName("Should return null when reference is null")
        void givenNullReference_When_ToJpaEntity_Then_ReturnNull() {
            // When
            RecipeReferenceEntity result = converter.toJpaEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should convert reference to entity with all fields")
        void givenReferenceWithAllFields_When_ToJpaEntity_Then_ConvertSuccessfully() {
            // Given
            LocalDateTime expectedDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            Timestamp addedAt = Timestamp.ofTimeSecondsAndNanos(
                    expectedDateTime.toEpochSecond(ZoneOffset.UTC),
                    expectedDateTime.getNano()
            );

            RecipeReference reference = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.BREAKFAST)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getRecipeId()).isEqualTo(SAMPLE_RECIPE_ID);
            assertThat(result.getDietId()).isEqualTo(SAMPLE_DIET_ID);
            assertThat(result.getUserId()).isEqualTo(SAMPLE_USER_ID);
            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(result.getAddedAt()).isEqualTo(expectedDateTime);
        }

        @Test
        @DisplayName("Should generate UUID when id is null")
        void givenReferenceWithoutId_When_ToJpaEntity_Then_GenerateUUID() {
            // Given
            Timestamp addedAt = Timestamp.now();
            RecipeReference reference = RecipeReference.builder()
                    .id(null)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.LUNCH)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId()).isNotEmpty();
            assertThat(result.getRecipeId()).isEqualTo(SAMPLE_RECIPE_ID);
        }

        @Test
        @DisplayName("Should use current time when addedAt is null")
        void givenReferenceWithoutAddedAt_When_ToJpaEntity_Then_UseCurrentTime() {
            // Given
            LocalDateTime beforeConversion = LocalDateTime.now();
            RecipeReference reference = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.DINNER)
                    .addedAt(null)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);
            LocalDateTime afterConversion = LocalDateTime.now();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAddedAt()).isNotNull();
            assertThat(result.getAddedAt()).isAfterOrEqualTo(beforeConversion.minusSeconds(1));
            assertThat(result.getAddedAt()).isBeforeOrEqualTo(afterConversion.plusSeconds(1));
        }

        @Test
        @DisplayName("Should handle different meal types")
        void givenReferenceWithDifferentMealTypes_When_ToJpaEntity_Then_ConvertCorrectly() {
            // Given
            Timestamp addedAt = Timestamp.now();

            for (MealType mealType : MealType.values()) {
                RecipeReference reference = RecipeReference.builder()
                        .id(SAMPLE_ID + "-" + mealType)
                        .recipeId(SAMPLE_RECIPE_ID)
                        .dietId(SAMPLE_DIET_ID)
                        .userId(SAMPLE_USER_ID)
                        .mealType(mealType)
                        .addedAt(addedAt)
                        .build();

                // When
                RecipeReferenceEntity result = converter.toJpaEntity(reference);

                // Then
                assertThat(result.getMealType()).isEqualTo(mealType);
            }
        }

        @Test
        @DisplayName("Should handle null meal type")
        void givenReferenceWithNullMealType_When_ToJpaEntity_Then_ConvertWithNull() {
            // Given
            Timestamp addedAt = Timestamp.now();
            RecipeReference reference = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(null)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getMealType()).isNull();
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void givenReferenceWithNullOptionalFields_When_ToJpaEntity_Then_ConvertWithNulls() {
            // Given
            Timestamp addedAt = Timestamp.now();
            RecipeReference reference = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(null)
                    .userId(null)
                    .mealType(null)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getRecipeId()).isEqualTo(SAMPLE_RECIPE_ID);
            assertThat(result.getDietId()).isNull();
            assertThat(result.getUserId()).isNull();
            assertThat(result.getMealType()).isNull();
        }

        @Test
        @DisplayName("Should preserve timestamp precision")
        void givenReferenceWithPreciseTimestamp_When_ToJpaEntity_Then_PreservePrecision() {
            // Given
            long seconds = 1705318245L;
            int nanos = 123456789;
            Timestamp addedAt = Timestamp.ofTimeSecondsAndNanos(seconds, nanos);

            RecipeReference reference = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.BREAKFAST)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);

            // Then
            assertThat(result.getAddedAt()).isNotNull();
            long resultSeconds = result.getAddedAt().toEpochSecond(ZoneOffset.UTC);
            int resultNanos = result.getAddedAt().getNano();
            assertThat(resultSeconds).isEqualTo(seconds);
            assertThat(resultNanos).isEqualTo(nanos);
        }
    }

    @Nested
    @DisplayName("toModel")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void givenNullEntity_When_ToModel_Then_ReturnNull() {
            // When
            RecipeReference result = converter.toModel(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should convert entity to model with all fields")
        void givenEntityWithAllFields_When_ToModel_Then_ConvertSuccessfully() {
            // Given
            LocalDateTime addedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.BREAKFAST)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getRecipeId()).isEqualTo(SAMPLE_RECIPE_ID);
            assertThat(result.getDietId()).isEqualTo(SAMPLE_DIET_ID);
            assertThat(result.getUserId()).isEqualTo(SAMPLE_USER_ID);
            assertThat(result.getMealType()).isEqualTo(MealType.BREAKFAST);
            assertThat(result.getAddedAt()).isNotNull();
            assertThat(result.getAddedAt().getSeconds()).isEqualTo(addedAt.toEpochSecond(ZoneOffset.UTC));
        }

        @Test
        @DisplayName("Should handle null addedAt")
        void givenEntityWithNullAddedAt_When_ToModel_Then_ReturnNullTimestamp() {
            // Given
            RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.LUNCH)
                    .addedAt(null)
                    .build();

            // When
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAddedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null optional fields")
        void givenEntityWithNullOptionalFields_When_ToModel_Then_ConvertWithNulls() {
            // Given
            LocalDateTime addedAt = LocalDateTime.now();
            RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(null)
                    .userId(null)
                    .mealType(null)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getRecipeId()).isEqualTo(SAMPLE_RECIPE_ID);
            assertThat(result.getDietId()).isNull();
            assertThat(result.getUserId()).isNull();
            assertThat(result.getMealType()).isNull();
        }

        @Test
        @DisplayName("Should convert different meal types correctly")
        void givenEntityWithDifferentMealTypes_When_ToModel_Then_ConvertCorrectly() {
            // Given
            LocalDateTime addedAt = LocalDateTime.now();

            for (MealType mealType : MealType.values()) {
                RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                        .id(SAMPLE_ID + "-" + mealType)
                        .recipeId(SAMPLE_RECIPE_ID)
                        .dietId(SAMPLE_DIET_ID)
                        .userId(SAMPLE_USER_ID)
                        .mealType(mealType)
                        .addedAt(addedAt)
                        .build();

                // When
                RecipeReference result = converter.toModel(entity);

                // Then
                assertThat(result.getMealType()).isEqualTo(mealType);
            }
        }

        @Test
        @DisplayName("Should preserve timestamp precision when converting")
        void givenEntityWithPreciseTimestamp_When_ToModel_Then_PreservePrecision() {
            // Given
            LocalDateTime addedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 45, 123456789);
            RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.BREAKFAST)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result.getAddedAt()).isNotNull();
            assertThat(result.getAddedAt().getSeconds()).isEqualTo(addedAt.toEpochSecond(ZoneOffset.UTC));
            assertThat(result.getAddedAt().getNanos()).isEqualTo(addedAt.getNano());
        }
    }

    @Nested
    @DisplayName("round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data in entity-to-model-to-entity round trip")
        void givenEntity_When_ToModelThenToEntity_Then_PreserveFields() {
            // Given
            LocalDateTime addedAt = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
            RecipeReferenceEntity original = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.BREAKFAST)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReference model = converter.toModel(original);
            RecipeReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(original.getId());
            assertThat(result.getRecipeId()).isEqualTo(original.getRecipeId());
            assertThat(result.getDietId()).isEqualTo(original.getDietId());
            assertThat(result.getUserId()).isEqualTo(original.getUserId());
            assertThat(result.getMealType()).isEqualTo(original.getMealType());
            assertThat(result.getAddedAt()).isEqualTo(original.getAddedAt());
        }

        @Test
        @DisplayName("Should preserve data in model-to-entity-to-model round trip")
        void givenModel_When_ToEntityThenToModel_Then_PreserveFields() {
            // Given
            Timestamp addedAt = Timestamp.ofTimeSecondsAndNanos(
                    LocalDateTime.of(2024, 1, 15, 10, 30, 45).toEpochSecond(ZoneOffset.UTC),
                    123456789
            );
            RecipeReference original = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.DINNER)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity entity = converter.toJpaEntity(original);
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(original.getId());
            assertThat(result.getRecipeId()).isEqualTo(original.getRecipeId());
            assertThat(result.getDietId()).isEqualTo(original.getDietId());
            assertThat(result.getUserId()).isEqualTo(original.getUserId());
            assertThat(result.getMealType()).isEqualTo(original.getMealType());
            assertThat(result.getAddedAt().getSeconds()).isEqualTo(original.getAddedAt().getSeconds());
            assertThat(result.getAddedAt().getNanos()).isEqualTo(original.getAddedAt().getNanos());
        }

        @Test
        @DisplayName("Should handle round trip with null optional fields")
        void givenModelWithNullFields_When_RoundTrip_Then_PreserveNulls() {
            // Given
            Timestamp addedAt = Timestamp.now();
            RecipeReference original = RecipeReference.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(null)
                    .userId(null)
                    .mealType(null)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity entity = converter.toJpaEntity(original);
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDietId()).isNull();
            assertThat(result.getUserId()).isNull();
            assertThat(result.getMealType()).isNull();
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very old date")
        void givenVeryOldDate_When_Convert_Then_HandleCorrectly() {
            // Given
            LocalDateTime oldDate = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.BREAKFAST)
                    .addedAt(oldDate)
                    .build();

            // When
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result.getAddedAt()).isNotNull();
            assertThat(result.getAddedAt().getSeconds()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle future date")
        void givenFutureDate_When_Convert_Then_HandleCorrectly() {
            // Given
            LocalDateTime futureDate = LocalDateTime.of(2100, 12, 31, 23, 59, 59);
            RecipeReferenceEntity entity = RecipeReferenceEntity.builder()
                    .id(SAMPLE_ID)
                    .recipeId(SAMPLE_RECIPE_ID)
                    .dietId(SAMPLE_DIET_ID)
                    .userId(SAMPLE_USER_ID)
                    .mealType(MealType.DINNER)
                    .addedAt(futureDate)
                    .build();

            // When
            RecipeReference result = converter.toModel(entity);

            // Then
            assertThat(result.getAddedAt()).isNotNull();
            assertThat(result.getAddedAt().getSeconds()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle empty string IDs")
        void givenEmptyStringIds_When_ToJpaEntity_Then_ConvertSuccessfully() {
            // Given
            Timestamp addedAt = Timestamp.now();
            RecipeReference reference = RecipeReference.builder()
                    .id("")
                    .recipeId("")
                    .dietId("")
                    .userId("")
                    .mealType(MealType.SNACK)
                    .addedAt(addedAt)
                    .build();

            // When
            RecipeReferenceEntity result = converter.toJpaEntity(reference);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEmpty();
            assertThat(result.getRecipeId()).isEmpty();
            assertThat(result.getDietId()).isEmpty();
            assertThat(result.getUserId()).isEmpty();
        }
    }
}
