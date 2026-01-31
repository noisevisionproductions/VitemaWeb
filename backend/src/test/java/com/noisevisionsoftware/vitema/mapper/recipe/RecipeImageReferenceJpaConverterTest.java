package com.noisevisionsoftware.vitema.mapper.recipe;

import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeImageReferenceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeImageReferenceJpaConverterTest {

    private RecipeImageReferenceJpaConverter converter;

    @BeforeEach
    void setUp() {
        converter = new RecipeImageReferenceJpaConverter();
    }

    @Nested
    @DisplayName("toJpaEntity")
    class ToJpaEntityTests {

        @Test
        @DisplayName("Should return null when model is null")
        void givenNullModel_When_ToJpaEntity_Then_ReturnNull() {
            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should convert model to entity with all fields")
        void givenModelWithAllFields_When_ToJpaEntity_Then_ConvertSuccessfully() {
            // Given
            RecipeImageReference model = RecipeImageReference.builder()
                    .id("123")
                    .imageUrl("https://example.com/image.jpg")
                    .storagePath("/storage/images/image.jpg")
                    .referenceCount(5)
                    .build();

            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(123L);
            assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.getStoragePath()).isEqualTo("/storage/images/image.jpg");
            assertThat(result.getReferenceCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should convert model without id")
        void givenModelWithoutId_When_ToJpaEntity_Then_EntityHasNoId() {
            // Given
            RecipeImageReference model = RecipeImageReference.builder()
                    .imageUrl("https://example.com/image.jpg")
                    .storagePath("/storage/images/image.jpg")
                    .referenceCount(1)
                    .build();

            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.getStoragePath()).isEqualTo("/storage/images/image.jpg");
            assertThat(result.getReferenceCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle empty string id")
        void givenModelWithEmptyId_When_ToJpaEntity_Then_EntityHasNoId() {
            // Given
            RecipeImageReference model = RecipeImageReference.builder()
                    .id("")
                    .imageUrl("https://example.com/image.jpg")
                    .storagePath("/storage/images/image.jpg")
                    .referenceCount(2)
                    .build();

            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
        }

        @Test
        @DisplayName("Should handle invalid id format gracefully")
        void givenModelWithInvalidId_When_ToJpaEntity_Then_IgnoreIdError() {
            // Given
            RecipeImageReference model = RecipeImageReference.builder()
                    .id("not-a-number")
                    .imageUrl("https://example.com/image.jpg")
                    .storagePath("/storage/images/image.jpg")
                    .referenceCount(3)
                    .build();

            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNull();
            assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("Should handle zero reference count")
        void givenModelWithZeroReferenceCount_When_ToJpaEntity_Then_ConvertSuccessfully() {
            // Given
            RecipeImageReference model = RecipeImageReference.builder()
                    .id("456")
                    .imageUrl("https://example.com/image2.jpg")
                    .storagePath("/storage/images/image2.jpg")
                    .referenceCount(0)
                    .build();

            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(456L);
            assertThat(result.getReferenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle null fields in model")
        void givenModelWithNullFields_When_ToJpaEntity_Then_ConvertWithNulls() {
            // Given
            RecipeImageReference model = RecipeImageReference.builder()
                    .id("789")
                    .imageUrl(null)
                    .storagePath(null)
                    .referenceCount(0)
                    .build();

            // When
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(789L);
            assertThat(result.getImageUrl()).isNull();
            assertThat(result.getStoragePath()).isNull();
            assertThat(result.getReferenceCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toModel")
    class ToModelTests {

        @Test
        @DisplayName("Should return null when entity is null")
        void givenNullEntity_When_ToModel_Then_ReturnNull() {
            // When
            RecipeImageReference result = converter.toModel(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should convert entity to model with all fields")
        void givenEntityWithAllFields_When_ToModel_Then_ConvertSuccessfully() {
            // Given
            RecipeImageReferenceEntity entity = RecipeImageReferenceEntity.builder()
                    .id(123L)
                    .imageUrl("https://example.com/image.jpg")
                    .storagePath("/storage/images/image.jpg")
                    .referenceCount(5)
                    .build();

            // When
            RecipeImageReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("123");
            assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.jpg");
            assertThat(result.getStoragePath()).isEqualTo("/storage/images/image.jpg");
            assertThat(result.getReferenceCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should convert entity with zero reference count")
        void givenEntityWithZeroReferenceCount_When_ToModel_Then_ConvertSuccessfully() {
            // Given
            RecipeImageReferenceEntity entity = RecipeImageReferenceEntity.builder()
                    .id(456L)
                    .imageUrl("https://example.com/image2.jpg")
                    .storagePath("/storage/images/image2.jpg")
                    .referenceCount(0)
                    .build();

            // When
            RecipeImageReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("456");
            assertThat(result.getReferenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle null fields in entity")
        void givenEntityWithNullFields_When_ToModel_Then_ConvertWithNulls() {
            // Given
            RecipeImageReferenceEntity entity = RecipeImageReferenceEntity.builder()
                    .id(789L)
                    .imageUrl(null)
                    .storagePath(null)
                    .referenceCount(0)
                    .build();

            // When
            RecipeImageReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("789");
            assertThat(result.getImageUrl()).isNull();
            assertThat(result.getStoragePath()).isNull();
            assertThat(result.getReferenceCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should convert large id correctly")
        void givenEntityWithLargeId_When_ToModel_Then_ConvertIdToString() {
            // Given
            RecipeImageReferenceEntity entity = RecipeImageReferenceEntity.builder()
                    .id(9999999999L)
                    .imageUrl("https://example.com/image3.jpg")
                    .storagePath("/storage/images/image3.jpg")
                    .referenceCount(10)
                    .build();

            // When
            RecipeImageReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("9999999999");
        }
    }

    @Nested
    @DisplayName("round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data in entity-to-model-to-entity round trip")
        void givenEntity_When_ToModelThenToEntity_Then_PreserveFields() {
            // Given
            RecipeImageReferenceEntity original = RecipeImageReferenceEntity.builder()
                    .id(123L)
                    .imageUrl("https://example.com/image.jpg")
                    .storagePath("/storage/images/image.jpg")
                    .referenceCount(5)
                    .build();

            // When
            RecipeImageReference model = converter.toModel(original);
            RecipeImageReferenceEntity result = converter.toJpaEntity(model);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(original.getId());
            assertThat(result.getImageUrl()).isEqualTo(original.getImageUrl());
            assertThat(result.getStoragePath()).isEqualTo(original.getStoragePath());
            assertThat(result.getReferenceCount()).isEqualTo(original.getReferenceCount());
        }

        @Test
        @DisplayName("Should preserve data in model-to-entity-to-model round trip")
        void givenModel_When_ToEntityThenToModel_Then_PreserveFields() {
            // Given
            RecipeImageReference original = RecipeImageReference.builder()
                    .id("456")
                    .imageUrl("https://example.com/image2.jpg")
                    .storagePath("/storage/images/image2.jpg")
                    .referenceCount(3)
                    .build();

            // When
            RecipeImageReferenceEntity entity = converter.toJpaEntity(original);
            RecipeImageReference result = converter.toModel(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(original.getId());
            assertThat(result.getImageUrl()).isEqualTo(original.getImageUrl());
            assertThat(result.getStoragePath()).isEqualTo(original.getStoragePath());
            assertThat(result.getReferenceCount()).isEqualTo(original.getReferenceCount());
        }
    }
}
