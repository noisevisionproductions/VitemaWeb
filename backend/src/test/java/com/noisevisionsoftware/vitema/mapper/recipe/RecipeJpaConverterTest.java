package com.noisevisionsoftware.vitema.mapper.recipe;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.jpa.NutritionalValuesEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

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
}