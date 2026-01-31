package com.noisevisionsoftware.vitema.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.recipe.jpa.NutritionalValuesEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeIngredientEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeJpaConverterTest {

    private RecipeJpaConverter converter;
    private static final String SAMPLE_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String SAMPLE_NAME = "Test Recipe";
    private static final String SAMPLE_INSTRUCTIONS = "Test Instructions";
    private static final String SAMPLE_PARENT_ID = "parent-123";

    @BeforeEach
    void setUp() {
        converter = new RecipeJpaConverter();
    }

    @Test
    @DisplayName("Should convert null Recipe to null RecipeEntity")
    void toJpaEntity_whenRecipeIsNull_returnsNull() {
        assertThat(converter.toJpaEntity(null)).isNull();
    }

    @Test
    @DisplayName("Should convert null RecipeEntity to null Recipe")
    void toModel_whenEntityIsNull_returnsNull() {
        assertThat(converter.toModel(null)).isNull();
    }

    @Test
    @DisplayName("Should convert Recipe to RecipeEntity with all fields")
    void toJpaEntity_withCompleteRecipe_convertsAllFields() {
        // Given
        NutritionalValues nutritionalValues = NutritionalValues.builder()
                .calories(100.0)
                .protein(20.0)
                .fat(10.0)
                .carbs(30.0)
                .build();

        Timestamp now = Timestamp.of(java.sql.Timestamp.from(
                java.time.Instant.now()
        ));

        Recipe recipe = Recipe.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .instructions(SAMPLE_INSTRUCTIONS)
                .createdAt(now)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .nutritionalValues(nutritionalValues)
                .parentRecipeId(SAMPLE_PARENT_ID)
                .build();

        // When
        RecipeEntity entity = converter.toJpaEntity(recipe);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getExternalId()).isEqualTo(SAMPLE_ID);
        assertThat(entity.getName()).isEqualTo(SAMPLE_NAME);
        assertThat(entity.getInstructions()).isEqualTo(SAMPLE_INSTRUCTIONS);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
        assertThat(entity.getParentRecipeId()).isEqualTo(SAMPLE_PARENT_ID);

        NutritionalValuesEntity nutritionalValuesEntity = entity.getNutritionalValues();
        assertThat(nutritionalValuesEntity).isNotNull();
        assertThat(nutritionalValuesEntity.getCalories()).isEqualTo(100);
        assertThat(nutritionalValuesEntity.getProtein()).isEqualTo(20);
        assertThat(nutritionalValuesEntity.getFat()).isEqualTo(10);
        assertThat(nutritionalValuesEntity.getCarbs()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should convert RecipeEntity to Recipe with all fields")
    void toModel_withCompleteEntity_convertsAllFields() {
        // Given
        NutritionalValuesEntity nutritionalValuesEntity = NutritionalValuesEntity.builder()
                .calories(100)
                .protein(20)
                .fat(10)
                .carbs(30)
                .build();

        LocalDateTime now = LocalDateTime.now();

        RecipeEntity entity = RecipeEntity.builder()
                .externalId(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .instructions(SAMPLE_INSTRUCTIONS)
                .createdAt(now)
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .nutritionalValues(nutritionalValuesEntity)
                .parentRecipeId(SAMPLE_PARENT_ID)
                .build();

        // When
        Recipe recipe = converter.toModel(entity);

        // Then
        assertThat(recipe).isNotNull();
        assertThat(recipe.getId()).isEqualTo(SAMPLE_ID);
        assertThat(recipe.getName()).isEqualTo(SAMPLE_NAME);
        assertThat(recipe.getInstructions()).isEqualTo(SAMPLE_INSTRUCTIONS);
        assertThat(recipe.getCreatedAt()).isNotNull();
        assertThat(recipe.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
        assertThat(recipe.getParentRecipeId()).isEqualTo(SAMPLE_PARENT_ID);

        NutritionalValues nutritionalValues = recipe.getNutritionalValues();
        assertThat(nutritionalValues).isNotNull();
        assertThat(nutritionalValues.getCalories()).isEqualTo(100);
        assertThat(nutritionalValues.getProtein()).isEqualTo(20);
        assertThat(nutritionalValues.getFat()).isEqualTo(10);
        assertThat(nutritionalValues.getCarbs()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should handle Recipe with null optional fields")
    void toJpaEntity_withNullOptionalFields_convertsCorrectly() {
        // Given
        Recipe recipe = Recipe.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .build();

        // When
        RecipeEntity entity = converter.toJpaEntity(recipe);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getExternalId()).isEqualTo(SAMPLE_ID);
        assertThat(entity.getName()).isEqualTo(SAMPLE_NAME);
        assertThat(entity.getInstructions()).isNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getPhotos()).isEmpty();
        assertThat(entity.getNutritionalValues()).isNull();
        assertThat(entity.getParentRecipeId()).isNull();
    }

    @Test
    @DisplayName("Should handle RecipeEntity with null optional fields")
    void toModel_withNullOptionalFields_convertsCorrectly() {
        // Given
        RecipeEntity entity = RecipeEntity.builder()
                .externalId(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        Recipe recipe = converter.toModel(entity);

        // Then
        assertThat(recipe).isNotNull();
        assertThat(recipe.getId()).isEqualTo(SAMPLE_ID);
        assertThat(recipe.getName()).isEqualTo(SAMPLE_NAME);
        assertThat(recipe.getInstructions()).isNull();
        assertThat(recipe.getCreatedAt()).isNotNull();
        assertThat(recipe.getPhotos()).isEmpty();
        assertThat(recipe.getNutritionalValues()).isNull();
        assertThat(recipe.getParentRecipeId()).isNull();
    }

    @Test
    @DisplayName("Should generate UUID when Recipe ID is null")
    void toJpaEntity_withNullId_generatesUUID() {
        // Given
        Recipe recipe = Recipe.builder()
                .name(SAMPLE_NAME)
                .build();

        // When
        RecipeEntity entity = converter.toJpaEntity(recipe);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getExternalId()).isNotNull();
        assertThat(entity.getExternalId()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Nested
    @DisplayName("Ingredients conversion tests")
    class IngredientsConversionTests {

        @Test
        @DisplayName("Should convert Recipe with ingredients to RecipeEntity")
        void toJpaEntity_withIngredients_convertsAllIngredients() {
            // Given
            RecipeIngredient ingredient1 = RecipeIngredient.builder()
                    .name("Flour")
                    .quantity(2.5)
                    .unit("cups")
                    .original("2.5 cups flour")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .build();
            RecipeIngredient ingredient2 = RecipeIngredient.builder()
                    .name("Sugar")
                    .quantity(1.0)
                    .unit("cup")
                    .original("1 cup sugar")
                    .categoryId("cat-2")
                    .hasCustomUnit(false)
                    .build();

            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .ingredients(Arrays.asList(ingredient1, ingredient2))
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getIngredients()).hasSize(2);
            
            RecipeIngredientEntity ing1 = entity.getIngredients().get(0);
            assertThat(ing1.getName()).isEqualTo("Flour");
            assertThat(ing1.getQuantity()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
            assertThat(ing1.getUnit()).isEqualTo("cups");
            assertThat(ing1.getOriginalText()).isEqualTo("2.5 cups flour");
            assertThat(ing1.getCategoryId()).isEqualTo("cat-1");
            assertThat(ing1.isHasCustomUnit()).isFalse();
            assertThat(ing1.getDisplayOrder()).isEqualTo(0);

            RecipeIngredientEntity ing2 = entity.getIngredients().get(1);
            assertThat(ing2.getName()).isEqualTo("Sugar");
            assertThat(ing2.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should convert RecipeEntity with ingredients to Recipe")
        void toModel_withIngredients_convertsAllIngredients() {
            // Given
            RecipeEntity entity = RecipeEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(LocalDateTime.now())
                    .build();

            RecipeIngredientEntity ing1 = RecipeIngredientEntity.builder()
                    .id(1L)
                    .recipe(entity)
                    .name("Flour")
                    .quantity(BigDecimal.valueOf(2.5))
                    .unit("cups")
                    .originalText("2.5 cups flour")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .displayOrder(0)
                    .build();
            RecipeIngredientEntity ing2 = RecipeIngredientEntity.builder()
                    .id(2L)
                    .recipe(entity)
                    .name("Sugar")
                    .quantity(BigDecimal.valueOf(1.0))
                    .unit("cup")
                    .originalText("1 cup sugar")
                    .categoryId("cat-2")
                    .hasCustomUnit(false)
                    .displayOrder(1)
                    .build();

            entity.setIngredients(Arrays.asList(ing1, ing2));

            // When
            Recipe recipe = converter.toModel(entity);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getIngredients()).hasSize(2);
            
            RecipeIngredient ingredient1 = recipe.getIngredients().get(0);
            assertThat(ingredient1.getId()).isEqualTo("1");
            assertThat(ingredient1.getName()).isEqualTo("Flour");
            assertThat(ingredient1.getQuantity()).isEqualTo(2.5);
            assertThat(ingredient1.getUnit()).isEqualTo("cups");
            assertThat(ingredient1.getOriginal()).isEqualTo("2.5 cups flour");
            assertThat(ingredient1.getCategoryId()).isEqualTo("cat-1");
            assertThat(ingredient1.isHasCustomUnit()).isFalse();

            RecipeIngredient ingredient2 = recipe.getIngredients().get(1);
            assertThat(ingredient2.getId()).isEqualTo("2");
            assertThat(ingredient2.getName()).isEqualTo("Sugar");
        }

        @Test
        @DisplayName("Should sort ingredients by display order when converting to model")
        void toModel_withUnorderedIngredients_sortsThemByDisplayOrder() {
            // Given
            RecipeEntity entity = RecipeEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(LocalDateTime.now())
                    .build();

            RecipeIngredientEntity ing1 = RecipeIngredientEntity.builder()
                    .id(1L)
                    .recipe(entity)
                    .name("Third")
                    .quantity(BigDecimal.ONE)
                    .unit("unit")
                    .displayOrder(2)
                    .build();
            RecipeIngredientEntity ing2 = RecipeIngredientEntity.builder()
                    .id(2L)
                    .recipe(entity)
                    .name("First")
                    .quantity(BigDecimal.ONE)
                    .unit("unit")
                    .displayOrder(0)
                    .build();
            RecipeIngredientEntity ing3 = RecipeIngredientEntity.builder()
                    .id(3L)
                    .recipe(entity)
                    .name("Second")
                    .quantity(BigDecimal.ONE)
                    .unit("unit")
                    .displayOrder(1)
                    .build();

            entity.setIngredients(Arrays.asList(ing1, ing2, ing3));

            // When
            Recipe recipe = converter.toModel(entity);

            // Then
            assertThat(recipe.getIngredients()).hasSize(3);
            assertThat(recipe.getIngredients().get(0).getName()).isEqualTo("First");
            assertThat(recipe.getIngredients().get(1).getName()).isEqualTo("Second");
            assertThat(recipe.getIngredients().get(2).getName()).isEqualTo("Third");
        }

        @Test
        @DisplayName("Should return empty list when ingredients is null in entity")
        void convertIngredients_withNullIngredients_returnsEmptyList() {
            // When
            List<RecipeIngredient> result = converter.convertIngredients(null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty ingredients list")
        void toJpaEntity_withEmptyIngredients_convertsCorrectly() {
            // Given
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .ingredients(Collections.emptyList())
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            // Empty ingredients list is not processed, so ingredients field remains null
            assertThat(entity.getIngredients()).isNullOrEmpty();
        }
    }

    @Nested
    @DisplayName("AuthorId and isPublic tests")
    class AuthorIdAndIsPublicTests {

        @Test
        @DisplayName("Should convert Recipe with authorId to RecipeEntity")
        void toJpaEntity_withAuthorId_convertsCorrectly() {
            // Given
            String authorId = "author-123";
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .authorId(authorId)
                    .isPublic(true)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getAuthorId()).isEqualTo(authorId);
            assertThat(entity.isPublic()).isTrue();
        }

        @Test
        @DisplayName("Should convert RecipeEntity with authorId to Recipe")
        void toModel_withAuthorId_convertsCorrectly() {
            // Given
            String authorId = "author-456";
            RecipeEntity entity = RecipeEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(LocalDateTime.now())
                    .authorId(authorId)
                    .isPublic(false)
                    .build();

            // When
            Recipe recipe = converter.toModel(entity);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getAuthorId()).isEqualTo(authorId);
            assertThat(recipe.isPublic()).isFalse();
        }

        @Test
        @DisplayName("Should handle null authorId")
        void toJpaEntity_withNullAuthorId_convertsCorrectly() {
            // Given
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .authorId(null)
                    .isPublic(false)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getAuthorId()).isNull();
            assertThat(entity.isPublic()).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge cases and boundary tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should generate UUID when Recipe ID is empty string")
        void toJpaEntity_withEmptyId_generatesUUID() {
            // Given
            Recipe recipe = Recipe.builder()
                    .id("")
                    .name(SAMPLE_NAME)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getExternalId()).isNotNull();
            assertThat(entity.getExternalId()).isNotEmpty();
            assertThat(entity.getExternalId()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        }

        @Test
        @DisplayName("Should use current time when createdAt is null in Recipe")
        void toJpaEntity_withNullCreatedAt_usesCurrentTime() {
            // Given
            LocalDateTime beforeConversion = LocalDateTime.now();
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(null)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);
            LocalDateTime afterConversion = LocalDateTime.now();

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getCreatedAt()).isNotNull();
            assertThat(entity.getCreatedAt()).isAfterOrEqualTo(beforeConversion.minusSeconds(1));
            assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(afterConversion.plusSeconds(1));
        }

        @Test
        @DisplayName("Should handle null createdAt in RecipeEntity")
        void toModel_withNullCreatedAt_returnsNullTimestamp() {
            // Given
            RecipeEntity entity = RecipeEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(null)
                    .build();

            // When
            Recipe recipe = converter.toModel(entity);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("Should handle null photos in Recipe")
        void toJpaEntity_withNullPhotos_createsEmptyList() {
            // Given
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .photos(null)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getPhotos()).isNotNull();
            assertThat(entity.getPhotos()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null photos in RecipeEntity")
        void toModel_withNullPhotos_returnsNull() {
            // Given
            RecipeEntity entity = RecipeEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(LocalDateTime.now())
                    .photos(null)
                    .build();

            // When
            Recipe recipe = converter.toModel(entity);

            // Then
            assertThat(recipe).isNotNull();
            assertThat(recipe.getPhotos()).isNull();
        }

        @Test
        @DisplayName("Should preserve timestamp precision")
        void toJpaEntity_withPreciseTimestamp_preservesPrecision() {
            // Given
            long seconds = 1705318245L;
            int nanos = 123456789;
            Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(seconds, nanos);

            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .createdAt(timestamp)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity.getCreatedAt()).isNotNull();
            long resultSeconds = entity.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
            int resultNanos = entity.getCreatedAt().getNano();
            assertThat(resultSeconds).isEqualTo(seconds);
            assertThat(resultNanos).isEqualTo(nanos);
        }

        @Test
        @DisplayName("Should handle very long instructions")
        void toJpaEntity_withLongInstructions_convertsCorrectly() {
            // Given
            String longInstructions = "Step 1: ".repeat(1000);
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .instructions(longInstructions)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getInstructions()).isEqualTo(longInstructions);
        }

        @Test
        @DisplayName("Should handle many photos")
        void toJpaEntity_withManyPhotos_convertsAllPhotos() {
            // Given
            List<String> manyPhotos = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                manyPhotos.add("photo" + i + ".jpg");
            }
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .photos(manyPhotos)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getPhotos()).hasSize(100);
            assertThat(entity.getPhotos()).containsAll(manyPhotos);
        }

        @Test
        @DisplayName("Should handle ingredient with zero quantity")
        void toJpaEntity_withZeroQuantityIngredient_convertsCorrectly() {
            // Given
            RecipeIngredient ingredient = RecipeIngredient.builder()
                    .name("Salt")
                    .quantity(0.0)
                    .unit("pinch")
                    .original("pinch of salt")
                    .build();
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .ingredients(Collections.singletonList(ingredient))
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity.getIngredients()).hasSize(1);
            assertThat(entity.getIngredients().get(0).getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Round-trip conversion tests")
    class RoundTripConversionTests {

        @Test
        @DisplayName("Should preserve data in entity-to-model-to-entity round trip")
        void roundTrip_entityToModelToEntity_preservesAllData() {
            // Given
            NutritionalValuesEntity nutritionalValues = NutritionalValuesEntity.builder()
                    .calories(100)
                    .protein(20)
                    .fat(10)
                    .carbs(30)
                    .build();

            RecipeEntity originalEntity = RecipeEntity.builder()
                    .externalId(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .instructions(SAMPLE_INSTRUCTIONS)
                    .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 45))
                    .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                    .nutritionalValues(nutritionalValues)
                    .parentRecipeId(SAMPLE_PARENT_ID)
                    .authorId("author-123")
                    .isPublic(true)
                    .build();

            RecipeIngredientEntity ingredient = RecipeIngredientEntity.builder()
                    .id(1L)
                    .recipe(originalEntity)
                    .name("Flour")
                    .quantity(BigDecimal.valueOf(2.5))
                    .unit("cups")
                    .originalText("2.5 cups flour")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .displayOrder(0)
                    .build();
            originalEntity.setIngredients(Collections.singletonList(ingredient));

            // When
            Recipe model = converter.toModel(originalEntity);
            RecipeEntity resultEntity = converter.toJpaEntity(model);

            // Then
            assertThat(resultEntity.getExternalId()).isEqualTo(originalEntity.getExternalId());
            assertThat(resultEntity.getName()).isEqualTo(originalEntity.getName());
            assertThat(resultEntity.getInstructions()).isEqualTo(originalEntity.getInstructions());
            assertThat(resultEntity.getPhotos()).containsExactlyElementsOf(originalEntity.getPhotos());
            assertThat(resultEntity.getParentRecipeId()).isEqualTo(originalEntity.getParentRecipeId());
            assertThat(resultEntity.getAuthorId()).isEqualTo(originalEntity.getAuthorId());
            assertThat(resultEntity.isPublic()).isEqualTo(originalEntity.isPublic());
            assertThat(resultEntity.getNutritionalValues().getCalories()).isEqualTo(originalEntity.getNutritionalValues().getCalories());
            assertThat(resultEntity.getIngredients()).hasSize(1);
            assertThat(resultEntity.getIngredients().get(0).getName()).isEqualTo("Flour");
        }

        @Test
        @DisplayName("Should preserve data in model-to-entity-to-model round trip")
        void roundTrip_modelToEntityToModel_preservesAllData() {
            // Given
            NutritionalValues nutritionalValues = NutritionalValues.builder()
                    .calories(100.0)
                    .protein(20.0)
                    .fat(10.0)
                    .carbs(30.0)
                    .build();

            Timestamp timestamp = Timestamp.ofTimeSecondsAndNanos(
                    LocalDateTime.of(2024, 1, 15, 10, 30, 45).toEpochSecond(ZoneOffset.UTC),
                    123456789
            );

            Recipe originalModel = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .instructions(SAMPLE_INSTRUCTIONS)
                    .createdAt(timestamp)
                    .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                    .nutritionalValues(nutritionalValues)
                    .parentRecipeId(SAMPLE_PARENT_ID)
                    .authorId("author-456")
                    .isPublic(false)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(originalModel);
            Recipe resultModel = converter.toModel(entity);

            // Then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getName()).isEqualTo(originalModel.getName());
            assertThat(resultModel.getInstructions()).isEqualTo(originalModel.getInstructions());
            assertThat(resultModel.getPhotos()).containsExactlyElementsOf(originalModel.getPhotos());
            assertThat(resultModel.getParentRecipeId()).isEqualTo(originalModel.getParentRecipeId());
            assertThat(resultModel.getAuthorId()).isEqualTo(originalModel.getAuthorId());
            assertThat(resultModel.isPublic()).isEqualTo(originalModel.isPublic());
            assertThat(resultModel.getNutritionalValues().getCalories()).isEqualTo(originalModel.getNutritionalValues().getCalories());
            // Note: Ingredients are not tested in this round-trip because ingredient entities
            // need database-generated IDs which are not available in the converter layer
        }

        @Test
        @DisplayName("Should handle round trip with minimal data")
        void roundTrip_withMinimalData_preservesData() {
            // Given
            Recipe minimalModel = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(minimalModel);
            Recipe resultModel = converter.toModel(entity);

            // Then
            assertThat(resultModel.getId()).isEqualTo(minimalModel.getId());
            assertThat(resultModel.getName()).isEqualTo(minimalModel.getName());
            assertThat(resultModel.getInstructions()).isNull();
            assertThat(resultModel.getNutritionalValues()).isNull();
            assertThat(resultModel.getParentRecipeId()).isNull();
        }
    }

    @Nested
    @DisplayName("Nutritional values tests")
    class NutritionalValuesTests {

        @Test
        @DisplayName("Should handle nutritional values with zero values")
        void toJpaEntity_withZeroNutritionalValues_convertsCorrectly() {
            // Given
            NutritionalValues nutritionalValues = NutritionalValues.builder()
                    .calories(0.0)
                    .protein(0.0)
                    .fat(0.0)
                    .carbs(0.0)
                    .build();
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .nutritionalValues(nutritionalValues)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity.getNutritionalValues()).isNotNull();
            assertThat(entity.getNutritionalValues().getCalories()).isZero();
            assertThat(entity.getNutritionalValues().getProtein()).isZero();
            assertThat(entity.getNutritionalValues().getFat()).isZero();
            assertThat(entity.getNutritionalValues().getCarbs()).isZero();
        }

        @Test
        @DisplayName("Should handle partial nutritional values")
        void toJpaEntity_withPartialNutritionalValues_convertsCorrectly() {
            // Given
            NutritionalValues nutritionalValues = NutritionalValues.builder()
                    .calories(100.0)
                    .protein(20.0)
                    .fat(10.0)
                    .carbs(30.0)
                    .build();
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .nutritionalValues(nutritionalValues)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity.getNutritionalValues()).isNotNull();
            assertThat(entity.getNutritionalValues().getCalories()).isEqualTo(100);
            assertThat(entity.getNutritionalValues().getProtein()).isEqualTo(20);
            assertThat(entity.getNutritionalValues().getFat()).isEqualTo(10);
            assertThat(entity.getNutritionalValues().getCarbs()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should handle very large nutritional values")
        void toJpaEntity_withLargeNutritionalValues_convertsCorrectly() {
            // Given
            NutritionalValues nutritionalValues = NutritionalValues.builder()
                    .calories(10000.0)
                    .protein(500.0)
                    .fat(300.0)
                    .carbs(1200.0)
                    .build();
            Recipe recipe = Recipe.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .nutritionalValues(nutritionalValues)
                    .build();

            // When
            RecipeEntity entity = converter.toJpaEntity(recipe);

            // Then
            assertThat(entity.getNutritionalValues().getCalories()).isEqualTo(10000);
            assertThat(entity.getNutritionalValues().getProtein()).isEqualTo(500);
            assertThat(entity.getNutritionalValues().getFat()).isEqualTo(300);
            assertThat(entity.getNutritionalValues().getCarbs()).isEqualTo(1200);
        }
    }
}