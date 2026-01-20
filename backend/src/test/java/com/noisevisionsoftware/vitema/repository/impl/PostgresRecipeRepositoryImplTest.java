package com.noisevisionsoftware.vitema.repository.impl;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeJpaConverter;
import com.noisevisionsoftware.vitema.mapper.recipe.RecipeReferenceJpaConverter;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeEntity;
import com.noisevisionsoftware.vitema.model.recipe.jpa.RecipeReferenceEntity;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeJpaRepository;
import com.noisevisionsoftware.vitema.repository.jpa.recipe.RecipeReferenceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresRecipeRepositoryImplTest {

    @Mock
    private RecipeJpaRepository recipeJpaRepository;

    @Mock
    private RecipeJpaConverter recipeJpaConverter;

    @Mock
    private RecipeReferenceJpaRepository recipeReferenceJpaRepository;

    @Mock
    private RecipeReferenceJpaConverter recipeReferenceJpaConverter;

    @InjectMocks
    private PostgresRecipeRepositoryImpl repository;

    @Captor
    private ArgumentCaptor<RecipeEntity> recipeEntityCaptor;

    private RecipeEntity mockEntity;
    private Recipe mockRecipe;
    private static final String TEST_RECIPE_ID = "testRecipeId123";

    @BeforeEach
    void setUp() {
        // Przygotowanie danych testowych
        mockEntity = new RecipeEntity();
        mockEntity.setId(1L);
        mockEntity.setExternalId(TEST_RECIPE_ID);
        mockEntity.setName("Testowy przepis");
        mockEntity.setInstructions("Instrukcje testowe");
        mockEntity.setCreatedAt(LocalDateTime.now());

        mockRecipe = Recipe.builder()
                .id(TEST_RECIPE_ID)
                .name("Testowy przepis")
                .instructions("Instrukcje testowe")
                .createdAt(Timestamp.now())
                .build();
    }

    @Test
    void findById_ShouldReturnRecipe_WhenRecipeExists() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenReturn(Optional.of(mockEntity));
        when(recipeJpaConverter.toModel(mockEntity)).thenReturn(mockRecipe);

        // when
        Optional<Recipe> result = repository.findById(TEST_RECIPE_ID);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_RECIPE_ID);
        assertThat(result.get().getName()).isEqualTo("Testowy przepis");
        verify(recipeJpaRepository).findByExternalId(TEST_RECIPE_ID);
        verify(recipeJpaConverter).toModel(mockEntity);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenRecipeDoesNotExist() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when
        Optional<Recipe> result = repository.findById(TEST_RECIPE_ID);

        // then
        assertThat(result).isEmpty();
        verify(recipeJpaRepository).findByExternalId(TEST_RECIPE_ID);
        verify(recipeJpaConverter, never()).toModel(any());
    }

    @Test
    void findById_ShouldThrowException_WhenRepositoryThrowsException() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenThrow(new RuntimeException("DB error"));

        // when/then
        assertThatThrownBy(() -> repository.findById(TEST_RECIPE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch recipe");
    }

    @Test
    void findByName_ShouldReturnRecipe_WhenNameExists() {
        // given
        String recipeName = "Testowy przepis";
        when(recipeJpaRepository.findFirstByNameIgnoreCaseOrderByCreatedAtDesc(recipeName))
                .thenReturn(Optional.of(mockEntity));
        when(recipeJpaConverter.toModel(mockEntity)).thenReturn(mockRecipe);

        // when
        Optional<Recipe> result = repository.findByName(recipeName);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(recipeName);
        verify(recipeJpaRepository).findFirstByNameIgnoreCaseOrderByCreatedAtDesc(recipeName);
        verify(recipeJpaConverter).toModel(mockEntity);
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // given
        String recipeName = "Nieistniejący przepis";
        when(recipeJpaRepository.findFirstByNameIgnoreCaseOrderByCreatedAtDesc(recipeName))
                .thenReturn(Optional.empty());

        // when
        Optional<Recipe> result = repository.findByName(recipeName);

        // then
        assertThat(result).isEmpty();
        verify(recipeJpaRepository).findFirstByNameIgnoreCaseOrderByCreatedAtDesc(recipeName);
        verify(recipeJpaConverter, never()).toModel(any());
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenRepositoryThrowsException() {
        // given
        String recipeName = "Problematyczny przepis";
        when(recipeJpaRepository.findFirstByNameIgnoreCaseOrderByCreatedAtDesc(recipeName))
                .thenThrow(new RuntimeException("DB error"));

        // when
        Optional<Recipe> result = repository.findByName(recipeName);

        // then
        assertThat(result).isEmpty();
        verify(recipeJpaRepository).findFirstByNameIgnoreCaseOrderByCreatedAtDesc(recipeName);
    }

    @Test
    void findAllByIds_ShouldReturnRecipes_WhenIdsExist() {
        // given
        List<String> ids = Arrays.asList(TEST_RECIPE_ID, "anotherId");
        List<RecipeEntity> entities = Arrays.asList(mockEntity, createAnotherRecipeEntity());

        when(recipeJpaRepository.findAllByExternalIdIn(anyList())).thenReturn(entities);
        when(recipeJpaConverter.toModel(any(RecipeEntity.class))).thenReturn(mockRecipe);

        // when
        List<Recipe> results = repository.findAllByIds(ids);

        // then
        assertThat(results).hasSize(2);
        verify(recipeJpaRepository).findAllByExternalIdIn(anyList());
        verify(recipeJpaConverter, times(2)).toModel(any(RecipeEntity.class));
    }

    @Test
    void findAllByIds_ShouldReturnEmptyList_WhenIdsIsEmpty() {
        // given
        List<String> emptyList = Collections.emptyList();

        // when
        List<Recipe> results = repository.findAllByIds(emptyList);

        // then
        assertThat(results).isEmpty();
        verify(recipeJpaRepository, never()).findAllByExternalIdIn(anyList());
    }

    @Test
    void findAllByIds_ShouldThrowException_WhenRepositoryThrowsException() {
        // given
        List<String> ids = Arrays.asList(TEST_RECIPE_ID, "anotherId");
        when(recipeJpaRepository.findAllByExternalIdIn(anyList())).thenThrow(new RuntimeException("DB error"));

        // when/then
        assertThatThrownBy(() -> repository.findAllByIds(ids))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch recipes");
    }

    @Test
    void findAll_ShouldReturnAllRecipes() {
        // given
        List<RecipeEntity> entities = Arrays.asList(mockEntity, createAnotherRecipeEntity());
        when(recipeJpaRepository.findAll()).thenReturn(entities);
        when(recipeJpaConverter.toModel(any(RecipeEntity.class))).thenReturn(mockRecipe);

        // when
        List<Recipe> results = repository.findAll();

        // then
        assertThat(results).hasSize(2);
        verify(recipeJpaRepository).findAll();
        verify(recipeJpaConverter, times(2)).toModel(any(RecipeEntity.class));
    }

    @Test
    void findAllWithPageable_ShouldReturnPageOfRecipes() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<RecipeEntity> entities = Arrays.asList(mockEntity, createAnotherRecipeEntity());
        Page<RecipeEntity> entityPage = new PageImpl<>(entities, pageable, entities.size());

        when(recipeJpaRepository.findAll(pageable)).thenReturn(entityPage);
        when(recipeJpaConverter.toModel(any(RecipeEntity.class))).thenReturn(mockRecipe);

        // when
        Page<Recipe> result = repository.findAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(recipeJpaRepository).findAll(pageable);
        verify(recipeJpaConverter, times(2)).toModel(any(RecipeEntity.class));
    }

    @Test
    void update_ShouldUpdateAndReturnRecipe_WhenRecipeExists() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenReturn(Optional.of(mockEntity));
        when(recipeJpaConverter.toJpaEntity(mockRecipe)).thenReturn(mockEntity);
        when(recipeJpaRepository.save(any(RecipeEntity.class))).thenReturn(mockEntity);
        when(recipeJpaConverter.toModel(mockEntity)).thenReturn(mockRecipe);

        // when
        Recipe result = repository.update(TEST_RECIPE_ID, mockRecipe);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_RECIPE_ID);
        verify(recipeJpaRepository).findByExternalId(TEST_RECIPE_ID);
        verify(recipeJpaConverter).toJpaEntity(mockRecipe);
        verify(recipeJpaRepository).save(mockEntity);
        verify(recipeJpaConverter).toModel(mockEntity);
    }

    @Test
    void update_ShouldThrowException_WhenRecipeDoesNotExist() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> repository.update(TEST_RECIPE_ID, mockRecipe))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update recipe");
    }

    @Test
    void search_ShouldReturnMatchingRecipes() {
        // given
        String query = "testowy";
        List<RecipeEntity> entities = Collections.singletonList(mockEntity);
        when(recipeJpaRepository.search(query)).thenReturn(entities);
        when(recipeJpaConverter.toModel(mockEntity)).thenReturn(mockRecipe);

        // when
        List<Recipe> results = repository.search(query);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).contains("Testowy");
        verify(recipeJpaRepository).search(query);
        verify(recipeJpaConverter).toModel(mockEntity);
    }

    @Test
    void delete_ShouldDeleteRecipe_WhenRecipeExists() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenReturn(Optional.of(mockEntity));
        doNothing().when(recipeJpaRepository).delete(mockEntity);

        // when
        repository.delete(TEST_RECIPE_ID);

        // then
        verify(recipeJpaRepository).findByExternalId(TEST_RECIPE_ID);
        verify(recipeJpaRepository).delete(mockEntity);
    }

    @Test
    void delete_ShouldThrowException_WhenRecipeDoesNotExist() {
        // given
        when(recipeJpaRepository.findByExternalId(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> repository.delete(TEST_RECIPE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to delete recipe");
    }

    @Test
    void save_ShouldGenerateId_WhenIdIsNull() {
        // given
        Recipe recipeWithoutId = Recipe.builder()
                .name("Nowy przepis")
                .instructions("Nowe instrukcje")
                .build();

        RecipeEntity newEntity = new RecipeEntity();
        newEntity.setName("Nowy przepis");
        newEntity.setInstructions("Nowe instrukcje");

        when(recipeJpaConverter.toJpaEntity(any(Recipe.class))).thenReturn(newEntity);
        when(recipeJpaRepository.save(any(RecipeEntity.class))).thenReturn(mockEntity);
        when(recipeJpaConverter.toModel(mockEntity)).thenReturn(mockRecipe);

        // when
        Recipe result = repository.save(recipeWithoutId);

        // then
        verify(recipeJpaRepository).save(recipeEntityCaptor.capture());

        assertThat(recipeWithoutId.getId()).isNotNull();
        assertThat(recipeWithoutId.getId()).hasSize(20);
        assertThat(result).isNotNull();
    }

    @Test
    void save_ShouldUseExistingId_WhenIdIsProvided() {
        // given
        when(recipeJpaConverter.toJpaEntity(mockRecipe)).thenReturn(mockEntity);
        when(recipeJpaRepository.save(mockEntity)).thenReturn(mockEntity);
        when(recipeJpaConverter.toModel(mockEntity)).thenReturn(mockRecipe);

        // when
        Recipe result = repository.save(mockRecipe);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_RECIPE_ID);
        verify(recipeJpaConverter).toJpaEntity(mockRecipe);
        verify(recipeJpaRepository).save(mockEntity);
        verify(recipeJpaConverter).toModel(mockEntity);
    }

    @Test
    void findByParentRecipeId_ShouldReturnChildRecipes() {
        // given
        String parentId = "parentRecipeId";
        List<RecipeEntity> childEntities = Arrays.asList(mockEntity, createAnotherRecipeEntity());
        when(recipeJpaRepository.findByParentRecipeId(parentId)).thenReturn(childEntities);
        when(recipeJpaConverter.toModel(any(RecipeEntity.class))).thenReturn(mockRecipe);

        // when
        List<Recipe> results = repository.findByParentRecipeId(parentId);

        // then
        assertThat(results).hasSize(2);
        verify(recipeJpaRepository).findByParentRecipeId(parentId);
        verify(recipeJpaConverter, times(2)).toModel(any(RecipeEntity.class));
    }

    @Test
    void saveReference_ShouldSaveRecipeReference() {
        // given
        RecipeReference reference = RecipeReference.builder()
                .recipeId(TEST_RECIPE_ID)
                .dietId("dietId")
                .userId("userId")
                .mealType(MealType.BREAKFAST)
                .addedAt(Timestamp.now())
                .build();

        RecipeReferenceEntity referenceEntity = new RecipeReferenceEntity();
        referenceEntity.setRecipeId(TEST_RECIPE_ID);

        when(recipeReferenceJpaConverter.toJpaEntity(reference)).thenReturn(referenceEntity);
        when(recipeReferenceJpaRepository.save(referenceEntity)).thenReturn(referenceEntity);

        // when
        repository.saveReference(reference);

        // then
        verify(recipeReferenceJpaConverter).toJpaEntity(reference);
        verify(recipeReferenceJpaRepository).save(referenceEntity);
    }

    @Test
    void saveReference_ShouldThrowException_WhenRepositoryThrowsException() {
        // given
        RecipeReference reference = RecipeReference.builder()
                .recipeId(TEST_RECIPE_ID)
                .dietId("dietId")
                .userId("userId")
                .mealType(MealType.BREAKFAST)
                .addedAt(Timestamp.now())
                .build();

        RecipeReferenceEntity referenceEntity = new RecipeReferenceEntity();

        when(recipeReferenceJpaConverter.toJpaEntity(reference)).thenReturn(referenceEntity);
        when(recipeReferenceJpaRepository.save(referenceEntity)).thenThrow(new RuntimeException("DB error"));

        // when/then
        assertThatThrownBy(() -> repository.saveReference(reference))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to save recipe reference");
    }

    private RecipeEntity createAnotherRecipeEntity() {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(2L);
        entity.setExternalId("anotherId");
        entity.setName("Inny przepis");
        entity.setInstructions("Inne instrukcje");
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }
}