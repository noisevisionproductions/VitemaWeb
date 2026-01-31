package com.noisevisionsoftware.vitema.repository.impl;

import com.noisevisionsoftware.vitema.mapper.recipe.RecipeImageReferenceJpaConverter;
import com.noisevisionsoftware.vitema.model.recipe.RecipeImageReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeImageReferenceEntity;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeImageReferenceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresRecipeImageRepositoryImplTest {

    @Mock
    private RecipeImageReferenceJpaRepository jpaRepository;

    @Mock
    private RecipeImageReferenceJpaConverter converter;

    @InjectMocks
    private PostgresRecipeImageRepositoryImpl repository;

    private RecipeImageReferenceEntity mockEntity;
    private RecipeImageReference mockModel;
    private static final String TEST_IMAGE_URL = "https://example.com/image.jpg";
    private static final String TEST_STORAGE_PATH = "/storage/images/image.jpg";
    private static final Long TEST_ENTITY_ID = 1L;

    @BeforeEach
    void setUp() {
        mockEntity = RecipeImageReferenceEntity.builder()
                .id(TEST_ENTITY_ID)
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockModel = RecipeImageReference.builder()
                .id(TEST_ENTITY_ID.toString())
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(5)
                .build();
    }

    @Test
    void save_ShouldReturnSavedRecipeImageReference_WhenSaveSucceeds() {
        // given
        RecipeImageReference inputModel = RecipeImageReference.builder()
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(0)
                .build();

        RecipeImageReferenceEntity inputEntity = RecipeImageReferenceEntity.builder()
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(0)
                .build();

        when(converter.toJpaEntity(inputModel)).thenReturn(inputEntity);
        when(jpaRepository.save(inputEntity)).thenReturn(mockEntity);
        when(converter.toModel(mockEntity)).thenReturn(mockModel);

        // when
        RecipeImageReference result = repository.save(inputModel);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_ENTITY_ID.toString());
        assertThat(result.getImageUrl()).isEqualTo(TEST_IMAGE_URL);
        assertThat(result.getStoragePath()).isEqualTo(TEST_STORAGE_PATH);
        assertThat(result.getReferenceCount()).isEqualTo(5);

        verify(converter).toJpaEntity(inputModel);
        verify(jpaRepository).save(inputEntity);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenConverterThrowsException() {
        // given
        RecipeImageReference inputModel = RecipeImageReference.builder()
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(0)
                .build();

        when(converter.toJpaEntity(inputModel)).thenThrow(new RuntimeException("Converter error"));

        // when/then
        assertThatThrownBy(() -> repository.save(inputModel))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zapisać referencji zdjęcia");

        verify(converter).toJpaEntity(inputModel);
        verify(jpaRepository, never()).save(any());
        verify(converter, never()).toModel(any());
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenJpaRepositoryThrowsException() {
        // given
        RecipeImageReference inputModel = RecipeImageReference.builder()
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(0)
                .build();

        RecipeImageReferenceEntity inputEntity = RecipeImageReferenceEntity.builder()
                .imageUrl(TEST_IMAGE_URL)
                .storagePath(TEST_STORAGE_PATH)
                .referenceCount(0)
                .build();

        when(converter.toJpaEntity(inputModel)).thenReturn(inputEntity);
        when(jpaRepository.save(inputEntity)).thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.save(inputModel))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zapisać referencji zdjęcia");

        verify(converter).toJpaEntity(inputModel);
        verify(jpaRepository).save(inputEntity);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findByImageUrl_ShouldReturnRecipeImageReference_WhenImageExists() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        when(converter.toModel(mockEntity)).thenReturn(mockModel);

        // when
        Optional<RecipeImageReference> result = repository.findByImageUrl(TEST_IMAGE_URL);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getImageUrl()).isEqualTo(TEST_IMAGE_URL);
        assertThat(result.get().getStoragePath()).isEqualTo(TEST_STORAGE_PATH);
        assertThat(result.get().getReferenceCount()).isEqualTo(5);

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void findByImageUrl_ShouldReturnEmpty_WhenImageDoesNotExist() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.empty());

        // when
        Optional<RecipeImageReference> result = repository.findByImageUrl(TEST_IMAGE_URL);

        // then
        assertThat(result).isEmpty();

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findByImageUrl_ShouldReturnEmpty_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenThrow(new RuntimeException("Database error"));

        // when
        Optional<RecipeImageReference> result = repository.findByImageUrl(TEST_IMAGE_URL);

        // then
        assertThat(result).isEmpty();

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(converter, never()).toModel(any());
    }

    @Test
    void incrementReferenceCount_ShouldIncrementCount_WhenImageExists() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        doNothing().when(jpaRepository).incrementReferenceCount(TEST_IMAGE_URL);

        // when
        repository.incrementReferenceCount(TEST_IMAGE_URL);

        // then
        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository).incrementReferenceCount(TEST_IMAGE_URL);
    }

    @Test
    void incrementReferenceCount_ShouldNotIncrement_WhenImageDoesNotExist() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.empty());

        // when
        repository.incrementReferenceCount(TEST_IMAGE_URL);

        // then
        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository, never()).incrementReferenceCount(anyString());
    }

    @Test
    void incrementReferenceCount_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.incrementReferenceCount(TEST_IMAGE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zwiększyć licznika referencji");

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository, never()).incrementReferenceCount(anyString());
    }

    @Test
    void incrementReferenceCount_ShouldThrowRuntimeException_WhenIncrementThrowsException() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        doThrow(new RuntimeException("Update error")).when(jpaRepository).incrementReferenceCount(TEST_IMAGE_URL);

        // when/then
        assertThatThrownBy(() -> repository.incrementReferenceCount(TEST_IMAGE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zwiększyć licznika referencji");

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository).incrementReferenceCount(TEST_IMAGE_URL);
    }

    @Test
    void decrementReferenceCount_ShouldReturnUpdatedCount_WhenImageExists() {
        // given
        int updatedCount = 4;
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        doNothing().when(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        when(jpaRepository.findReferenceCount(TEST_IMAGE_URL)).thenReturn(Optional.of(updatedCount));

        // when
        int result = repository.decrementReferenceCount(TEST_IMAGE_URL);

        // then
        assertThat(result).isEqualTo(updatedCount);

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        verify(jpaRepository).findReferenceCount(TEST_IMAGE_URL);
    }

    @Test
    void decrementReferenceCount_ShouldReturnZero_WhenImageDoesNotExist() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.empty());

        // when
        int result = repository.decrementReferenceCount(TEST_IMAGE_URL);

        // then
        assertThat(result).isEqualTo(0);

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository, never()).decrementReferenceCount(anyString());
        verify(jpaRepository, never()).findReferenceCount(anyString());
    }

    @Test
    void decrementReferenceCount_ShouldReturnZero_WhenReferenceCountNotFound() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        doNothing().when(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        when(jpaRepository.findReferenceCount(TEST_IMAGE_URL)).thenReturn(Optional.empty());

        // when
        int result = repository.decrementReferenceCount(TEST_IMAGE_URL);

        // then
        assertThat(result).isEqualTo(0);

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        verify(jpaRepository).findReferenceCount(TEST_IMAGE_URL);
    }

    @Test
    void decrementReferenceCount_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.decrementReferenceCount(TEST_IMAGE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zmniejszyć licznika referencji");

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository, never()).decrementReferenceCount(anyString());
        verify(jpaRepository, never()).findReferenceCount(anyString());
    }

    @Test
    void decrementReferenceCount_ShouldThrowRuntimeException_WhenDecrementThrowsException() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        doThrow(new RuntimeException("Update error")).when(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);

        // when/then
        assertThatThrownBy(() -> repository.decrementReferenceCount(TEST_IMAGE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zmniejszyć licznika referencji");

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        verify(jpaRepository, never()).findReferenceCount(anyString());
    }

    @Test
    void decrementReferenceCount_ShouldThrowRuntimeException_WhenFindReferenceCountThrowsException() {
        // given
        when(jpaRepository.findByImageUrl(TEST_IMAGE_URL)).thenReturn(Optional.of(mockEntity));
        doNothing().when(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        when(jpaRepository.findReferenceCount(TEST_IMAGE_URL)).thenThrow(new RuntimeException("Query error"));

        // when/then
        assertThatThrownBy(() -> repository.decrementReferenceCount(TEST_IMAGE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zmniejszyć licznika referencji");

        verify(jpaRepository).findByImageUrl(TEST_IMAGE_URL);
        verify(jpaRepository).decrementReferenceCount(TEST_IMAGE_URL);
        verify(jpaRepository).findReferenceCount(TEST_IMAGE_URL);
    }

    @Test
    void deleteByImageUrl_ShouldDeleteImage_WhenImageExists() {
        // given
        doNothing().when(jpaRepository).deleteByImageUrl(TEST_IMAGE_URL);

        // when
        repository.deleteByImageUrl(TEST_IMAGE_URL);

        // then
        verify(jpaRepository).deleteByImageUrl(TEST_IMAGE_URL);
    }

    @Test
    void deleteByImageUrl_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        doThrow(new RuntimeException("Database error")).when(jpaRepository).deleteByImageUrl(TEST_IMAGE_URL);

        // when/then
        assertThatThrownBy(() -> repository.deleteByImageUrl(TEST_IMAGE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się usunąć referencji zdjęcia");

        verify(jpaRepository).deleteByImageUrl(TEST_IMAGE_URL);
    }

    @Test
    void findAllWithZeroReferences_ShouldReturnList_WhenImagesWithZeroReferencesExist() {
        // given
        RecipeImageReferenceEntity entity1 = RecipeImageReferenceEntity.builder()
                .id(1L)
                .imageUrl("https://example.com/image1.jpg")
                .storagePath("/storage/images/image1.jpg")
                .referenceCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        RecipeImageReferenceEntity entity2 = RecipeImageReferenceEntity.builder()
                .id(2L)
                .imageUrl("https://example.com/image2.jpg")
                .storagePath("/storage/images/image2.jpg")
                .referenceCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        RecipeImageReference model1 = RecipeImageReference.builder()
                .id("1")
                .imageUrl("https://example.com/image1.jpg")
                .storagePath("/storage/images/image1.jpg")
                .referenceCount(0)
                .build();

        RecipeImageReference model2 = RecipeImageReference.builder()
                .id("2")
                .imageUrl("https://example.com/image2.jpg")
                .storagePath("/storage/images/image2.jpg")
                .referenceCount(0)
                .build();

        List<RecipeImageReferenceEntity> entities = Arrays.asList(entity1, entity2);
        when(jpaRepository.findByReferenceCount(0)).thenReturn(entities);
        when(converter.toModel(entity1)).thenReturn(model1);
        when(converter.toModel(entity2)).thenReturn(model2);

        // when
        List<RecipeImageReference> result = repository.findAllWithZeroReferences();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReferenceCount()).isEqualTo(0);
        assertThat(result.get(1).getReferenceCount()).isEqualTo(0);

        verify(jpaRepository).findByReferenceCount(0);
        verify(converter).toModel(entity1);
        verify(converter).toModel(entity2);
    }

    @Test
    void findAllWithZeroReferences_ShouldReturnEmptyList_WhenNoImagesWithZeroReferencesExist() {
        // given
        when(jpaRepository.findByReferenceCount(0)).thenReturn(Collections.emptyList());

        // when
        List<RecipeImageReference> result = repository.findAllWithZeroReferences();

        // then
        assertThat(result).isEmpty();

        verify(jpaRepository).findByReferenceCount(0);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findAllWithZeroReferences_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByReferenceCount(0)).thenThrow(new RuntimeException("Database error"));

        // when
        List<RecipeImageReference> result = repository.findAllWithZeroReferences();

        // then
        assertThat(result).isEmpty();

        verify(jpaRepository).findByReferenceCount(0);
        verify(converter, never()).toModel(any());
    }
}
