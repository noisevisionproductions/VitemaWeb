package com.noisevisionsoftware.vitema.repository.meal;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.meal.MealTemplateJpaConverter;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.meal.jpa.MealTemplateEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class PostgresMealTemplateRepositoryTest {

    @Mock
    private MealTemplateJpaRepository jpaRepository;

    @Mock
    private MealTemplateJpaConverter converter;

    @InjectMocks
    private PostgresMealTemplateRepository repository;

    private MealTemplateEntity mockEntity;
    private MealTemplate mockTemplate;
    private static final String TEST_TEMPLATE_ID = "mt_test123456789";
    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        // Given - przygotowanie danych testowych
        mockEntity = MealTemplateEntity.builder()
                .id(1L)
                .externalId(TEST_TEMPLATE_ID)
                .name("Testowy szablon posiłku")
                .instructions("Instrukcje testowe")
                .mealType("BREAKFAST")
                .category("PROTEIN")
                .createdBy(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastUsed(LocalDateTime.now())
                .usageCount(5)
                .calories(BigDecimal.valueOf(500))
                .protein(BigDecimal.valueOf(30))
                .fat(BigDecimal.valueOf(20))
                .carbs(BigDecimal.valueOf(50))
                .isPublic(true)
                .build();

        mockTemplate = MealTemplate.builder()
                .id(TEST_TEMPLATE_ID)
                .name("Testowy szablon posiłku")
                .instructions("Instrukcje testowe")
                .mealType(MealType.BREAKFAST)
                .category("PROTEIN")
                .createdBy(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .lastUsed(Timestamp.now())
                .usageCount(5)
                .build();
    }

    @Test
    void findById_ShouldReturnTemplate_WhenTemplateExists() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        Optional<MealTemplate> result = repository.findById(TEST_TEMPLATE_ID);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_TEMPLATE_ID);
        assertThat(result.get().getName()).isEqualTo("Testowy szablon posiłku");
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenTemplateDoesNotExist() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());

        // when
        Optional<MealTemplate> result = repository.findById(TEST_TEMPLATE_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when
        Optional<MealTemplate> result = repository.findById(TEST_TEMPLATE_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter, never()).toModel(any());
    }

    @Test
    void searchByName_ShouldReturnMatchingTemplates_WhenQueryMatches() {
        // given
        String query = "testowy";
        int limit = 10;
        Pageable pageable = PageRequest.of(1, limit);
        List<MealTemplateEntity> entities = Collections.singletonList(mockEntity);
        when(jpaRepository.searchByNameOrInstructions(query, pageable)).thenReturn(entities);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        List<MealTemplate> result = repository.searchByName(query, limit);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("testowy");
        verify(jpaRepository).searchByNameOrInstructions(query, pageable);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void searchByName_ShouldReturnEmptyList_WhenNoMatches() {
        // given
        String query = "nieistniejący";
        int limit = 10;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.searchByNameOrInstructions(query, pageable))
                .thenReturn(Collections.emptyList());

        // when
        List<MealTemplate> result = repository.searchByName(query, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).searchByNameOrInstructions(query, pageable);
        verify(converter, never()).toModel(any());
    }

    @Test
    void searchByName_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        String query = "testowy";
        int limit = 10;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.searchByNameOrInstructions(query, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<MealTemplate> result = repository.searchByName(query, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).searchByNameOrInstructions(query, pageable);
    }

    @Test
    void findTopByUsageCount_ShouldReturnListOfTemplates_WhenTemplatesExist() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(1, limit);
        List<MealTemplateEntity> entities = Arrays.asList(mockEntity, createAnotherEntity());
        when(jpaRepository.findTopByOrderByUsageCountDescLastUsedDesc(pageable)).thenReturn(entities);
        when(converter.toModel(any(MealTemplateEntity.class))).thenReturn(mockTemplate);

        // when
        List<MealTemplate> result = repository.findTopByUsageCount(limit);

        // then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findTopByOrderByUsageCountDescLastUsedDesc(pageable);
        verify(converter, times(2)).toModel(any(MealTemplateEntity.class));
    }

    @Test
    void findTopByUsageCount_ShouldReturnEmptyList_WhenNoTemplatesExist() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.findTopByOrderByUsageCountDescLastUsedDesc(pageable))
                .thenReturn(Collections.emptyList());

        // when
        List<MealTemplate> result = repository.findTopByUsageCount(limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findTopByOrderByUsageCountDescLastUsedDesc(pageable);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findTopByUsageCount_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.findTopByOrderByUsageCountDescLastUsedDesc(pageable))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<MealTemplate> result = repository.findTopByUsageCount(limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findTopByOrderByUsageCountDescLastUsedDesc(pageable);
    }

    @Test
    void findRecentlyUsed_ShouldReturnListOfTemplates_WhenTemplatesExist() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(1, limit);
        List<MealTemplateEntity> entities = Arrays.asList(mockEntity, createAnotherEntity());
        when(jpaRepository.findRecentlyUsed(pageable)).thenReturn(entities);
        when(converter.toModel(any(MealTemplateEntity.class))).thenReturn(mockTemplate);

        // when
        List<MealTemplate> result = repository.findRecentlyUsed(limit);

        // then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findRecentlyUsed(pageable);
        verify(converter, times(2)).toModel(any(MealTemplateEntity.class));
    }

    @Test
    void findRecentlyUsed_ShouldReturnEmptyList_WhenNoTemplatesExist() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.findRecentlyUsed(pageable))
                .thenReturn(Collections.emptyList());

        // when
        List<MealTemplate> result = repository.findRecentlyUsed(limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findRecentlyUsed(pageable);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findRecentlyUsed_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.findRecentlyUsed(pageable))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<MealTemplate> result = repository.findRecentlyUsed(limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findRecentlyUsed(pageable);
    }

    @Test
    void save_ShouldSaveNewTemplate_WhenIdIsNull() {
        // given
        MealTemplate templateWithoutId = MealTemplate.builder()
                .name("Nowy szablon")
                .instructions("Nowe instrukcje")
                .mealType(MealType.LUNCH)
                .category("VEGETARIAN")
                .createdBy(TEST_USER_ID)
                .build();

        MealTemplateEntity newEntity = MealTemplateEntity.builder()
                .name("Nowy szablon")
                .instructions("Nowe instrukcje")
                .mealType("LUNCH")
                .category("VEGETARIAN")
                .createdBy(TEST_USER_ID)
                .build();

        when(converter.toEntity(templateWithoutId)).thenReturn(newEntity);
        when(jpaRepository.save(newEntity)).thenReturn(mockEntity);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        MealTemplate result = repository.save(templateWithoutId);

        // then
        assertThat(result).isNotNull();
        verify(converter).toEntity(templateWithoutId);
        verify(jpaRepository).save(newEntity);
        verify(converter).toModel(mockEntity);
        verify(jpaRepository, never()).findByExternalId(anyString());
    }

    @Test
    void save_ShouldUpdateExistingTemplate_WhenIdExists() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        doNothing().when(converter).updateEntity(mockEntity, mockTemplate);
        when(jpaRepository.save(mockEntity)).thenReturn(mockEntity);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        MealTemplate result = repository.save(mockTemplate);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TEST_TEMPLATE_ID);
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter).updateEntity(mockEntity, mockTemplate);
        verify(jpaRepository).save(mockEntity);
        verify(converter).toModel(mockEntity);
        verify(converter, never()).toEntity(any());
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenIdExistsButEntityNotFound() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());

        // when/then - NotFoundException jest przechwytywany i opakowywany w RuntimeException
        assertThatThrownBy(() -> repository.save(mockTemplate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zapisać szablonu")
                .hasCauseInstanceOf(NotFoundException.class);
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter, never()).toEntity(any());
        verify(converter, never()).updateEntity(any(), any());
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        doNothing().when(converter).updateEntity(mockEntity, mockTemplate);
        when(jpaRepository.save(mockEntity)).thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.save(mockTemplate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zapisać szablonu");
        verify(jpaRepository).save(mockEntity);
    }

    @Test
    void delete_ShouldDeleteTemplate_WhenTemplateExists() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        doNothing().when(jpaRepository).delete(mockEntity);

        // when
        repository.delete(TEST_TEMPLATE_ID);

        // then
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(jpaRepository).delete(mockEntity);
    }

    @Test
    void delete_ShouldThrowRuntimeException_WhenTemplateDoesNotExist() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());

        // when/then - NotFoundException jest przechwytywany i opakowywany w RuntimeException
        assertThatThrownBy(() -> repository.delete(TEST_TEMPLATE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się usunąć szablonu")
                .hasCauseInstanceOf(NotFoundException.class);
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(jpaRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.delete(TEST_TEMPLATE_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się usunąć szablonu");
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
    }

    @Test
    void incrementUsageCount_ShouldIncrementCount_WhenTemplateExists() {
        // given
        doNothing().when(jpaRepository).incrementUsageCount(TEST_TEMPLATE_ID);

        // when
        repository.incrementUsageCount(TEST_TEMPLATE_ID);

        // then
        verify(jpaRepository).incrementUsageCount(TEST_TEMPLATE_ID);
    }

    @Test
    void incrementUsageCount_ShouldNotThrowException_WhenRepositoryThrowsException() {
        // given
        doThrow(new RuntimeException("Database error"))
                .when(jpaRepository).incrementUsageCount(TEST_TEMPLATE_ID);

        // when/then - metoda nie powinna rzucać wyjątku, tylko logować błąd
        repository.incrementUsageCount(TEST_TEMPLATE_ID);

        // then
        verify(jpaRepository).incrementUsageCount(TEST_TEMPLATE_ID);
    }

    @Test
    void searchAccessibleTemplates_ShouldReturnMatchingTemplates_WhenQueryMatches() {
        // given
        String query = "testowy";
        int limit = 10;
        Pageable pageable = PageRequest.of(1, limit);
        List<MealTemplateEntity> entities = Collections.singletonList(mockEntity);
        when(jpaRepository.searchAccessibleTemplates(query, TEST_USER_ID, pageable)).thenReturn(entities);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        List<MealTemplate> result = repository.searchAccessibleTemplates(query, TEST_USER_ID, limit);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("testowy");
        verify(jpaRepository).searchAccessibleTemplates(query, TEST_USER_ID, pageable);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void searchAccessibleTemplates_ShouldReturnEmptyList_WhenNoMatches() {
        // given
        String query = "nieistniejący";
        int limit = 10;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.searchAccessibleTemplates(query, TEST_USER_ID, pageable))
                .thenReturn(Collections.emptyList());

        // when
        List<MealTemplate> result = repository.searchAccessibleTemplates(query, TEST_USER_ID, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).searchAccessibleTemplates(query, TEST_USER_ID, pageable);
        verify(converter, never()).toModel(any());
    }

    @Test
    void searchAccessibleTemplates_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        String query = "testowy";
        int limit = 10;
        Pageable pageable = PageRequest.of(1, limit);
        when(jpaRepository.searchAccessibleTemplates(query, TEST_USER_ID, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<MealTemplate> result = repository.searchAccessibleTemplates(query, TEST_USER_ID, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).searchAccessibleTemplates(query, TEST_USER_ID, pageable);
    }

    private MealTemplateEntity createAnotherEntity() {
        return MealTemplateEntity.builder()
                .id(2L)
                .externalId("mt_another123456")
                .name("Inny szablon posiłku")
                .instructions("Inne instrukcje")
                .mealType("DINNER")
                .category("LOW_CARB")
                .createdBy(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastUsed(LocalDateTime.now())
                .usageCount(10)
                .calories(BigDecimal.valueOf(600))
                .protein(BigDecimal.valueOf(40))
                .fat(BigDecimal.valueOf(25))
                .carbs(BigDecimal.valueOf(60))
                .isPublic(false)
                .build();
    }
}
