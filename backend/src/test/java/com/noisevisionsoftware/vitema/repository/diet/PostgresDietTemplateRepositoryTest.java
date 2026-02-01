package com.noisevisionsoftware.vitema.repository.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.diet.DietTemplateJpaConverter;
import com.noisevisionsoftware.vitema.model.diet.template.DietTemplate;
import com.noisevisionsoftware.vitema.model.diet.template.DietTemplateCategory;
import com.noisevisionsoftware.vitema.model.diet.template.jpa.DietTemplateEntity;
import com.noisevisionsoftware.vitema.repository.jpa.diet.DietTemplateJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class PostgresDietTemplateRepositoryTest {

    @Mock
    private DietTemplateJpaRepository jpaRepository;

    @Mock
    private DietTemplateJpaConverter converter;

    @InjectMocks
    private PostgresDietTemplateRepository repository;

    private DietTemplateEntity mockEntity;
    private DietTemplate mockTemplate;
    private static final String TEST_TEMPLATE_ID = "dt_test123456789";
    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        mockEntity = DietTemplateEntity.builder()
                .id(1L)
                .externalId(TEST_TEMPLATE_ID)
                .name("Testowy szablon")
                .description("Opis testowy")
                .category(DietTemplateCategory.WEIGHT_LOSS)
                .createdBy(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .duration(7)
                .mealsPerDay(3)
                .usageCount(0)
                .build();

        mockTemplate = DietTemplate.builder()
                .id(TEST_TEMPLATE_ID)
                .name("Testowy szablon")
                .description("Opis testowy")
                .category(DietTemplateCategory.WEIGHT_LOSS)
                .createdBy(TEST_USER_ID)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .version(1)
                .duration(7)
                .mealsPerDay(3)
                .usageCount(0)
                .build();
    }

    @Test
    void findById_ShouldReturnTemplate_WhenTemplateExists() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        Optional<DietTemplate> result = repository.findById(TEST_TEMPLATE_ID);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(TEST_TEMPLATE_ID);
        assertThat(result.get().getName()).isEqualTo("Testowy szablon");
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenTemplateDoesNotExist() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());

        // when
        Optional<DietTemplate> result = repository.findById(TEST_TEMPLATE_ID);

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
        Optional<DietTemplate> result = repository.findById(TEST_TEMPLATE_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findByCreatedBy_ShouldReturnListOfTemplates_WhenTemplatesExist() {
        // given
        List<DietTemplateEntity> entities = Arrays.asList(mockEntity, createAnotherEntity());
        when(jpaRepository.findByCreatedByOrderByCreatedAtDesc(TEST_USER_ID)).thenReturn(entities);
        when(converter.toModel(any(DietTemplateEntity.class))).thenReturn(mockTemplate);

        // when
        List<DietTemplate> result = repository.findByCreatedBy(TEST_USER_ID);

        // then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findByCreatedByOrderByCreatedAtDesc(TEST_USER_ID);
        verify(converter, times(2)).toModel(any(DietTemplateEntity.class));
    }

    @Test
    void findByCreatedBy_ShouldReturnEmptyList_WhenNoTemplatesExist() {
        // given
        when(jpaRepository.findByCreatedByOrderByCreatedAtDesc(TEST_USER_ID))
                .thenReturn(Collections.emptyList());

        // when
        List<DietTemplate> result = repository.findByCreatedBy(TEST_USER_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByCreatedByOrderByCreatedAtDesc(TEST_USER_ID);
        verify(converter, never()).toModel(any());
    }

    @Test
    void findByCreatedBy_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByCreatedByOrderByCreatedAtDesc(TEST_USER_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<DietTemplate> result = repository.findByCreatedBy(TEST_USER_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByCreatedByOrderByCreatedAtDesc(TEST_USER_ID);
    }

    @Test
    void findByCategoryAndCreatedBy_ShouldReturnListOfTemplates_WhenTemplatesExist() {
        // given
        DietTemplateCategory category = DietTemplateCategory.WEIGHT_LOSS;
        List<DietTemplateEntity> entities = Collections.singletonList(mockEntity);
        when(jpaRepository.findByCategoryAndCreatedByOrderByCreatedAtDesc(category, TEST_USER_ID))
                .thenReturn(entities);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        List<DietTemplate> result = repository.findByCategoryAndCreatedBy(category, TEST_USER_ID);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCategory()).isEqualTo(category);
        verify(jpaRepository).findByCategoryAndCreatedByOrderByCreatedAtDesc(category, TEST_USER_ID);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void findByCategoryAndCreatedBy_ShouldReturnEmptyList_WhenNoTemplatesExist() {
        // given
        DietTemplateCategory category = DietTemplateCategory.WEIGHT_LOSS;
        when(jpaRepository.findByCategoryAndCreatedByOrderByCreatedAtDesc(category, TEST_USER_ID))
                .thenReturn(Collections.emptyList());

        // when
        List<DietTemplate> result = repository.findByCategoryAndCreatedBy(category, TEST_USER_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByCategoryAndCreatedByOrderByCreatedAtDesc(category, TEST_USER_ID);
    }

    @Test
    void findByCategoryAndCreatedBy_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        DietTemplateCategory category = DietTemplateCategory.WEIGHT_LOSS;
        when(jpaRepository.findByCategoryAndCreatedByOrderByCreatedAtDesc(category, TEST_USER_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<DietTemplate> result = repository.findByCategoryAndCreatedBy(category, TEST_USER_ID);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByCategoryAndCreatedByOrderByCreatedAtDesc(category, TEST_USER_ID);
    }

    @Test
    void findTopByCreatedByOrderByUsageCountDesc_ShouldReturnListOfTemplates_WhenTemplatesExist() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);
        List<DietTemplateEntity> entities = Arrays.asList(mockEntity, createAnotherEntity());
        when(jpaRepository.findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, pageable))
                .thenReturn(entities);
        when(converter.toModel(any(DietTemplateEntity.class))).thenReturn(mockTemplate);

        // when
        List<DietTemplate> result = repository.findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, limit);

        // then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, pageable);
        verify(converter, times(2)).toModel(any(DietTemplateEntity.class));
    }

    @Test
    void findTopByCreatedByOrderByUsageCountDesc_ShouldReturnEmptyList_WhenNoTemplatesExist() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);
        when(jpaRepository.findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, pageable))
                .thenReturn(Collections.emptyList());

        // when
        List<DietTemplate> result = repository.findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, pageable);
    }

    @Test
    void findTopByCreatedByOrderByUsageCountDesc_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);
        when(jpaRepository.findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<DietTemplate> result = repository.findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).findTopByCreatedByOrderByUsageCountDesc(TEST_USER_ID, pageable);
    }

    @Test
    void searchByNameOrDescription_ShouldReturnMatchingTemplates_WhenQueryMatches() {
        // given
        String query = "testowy";
        int limit = 10;
        Pageable pageable = PageRequest.of(0, limit);
        List<DietTemplateEntity> entities = Collections.singletonList(mockEntity);
        when(jpaRepository.searchByNameOrDescription(query, TEST_USER_ID, pageable)).thenReturn(entities);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        List<DietTemplate> result = repository.searchByNameOrDescription(query, TEST_USER_ID, limit);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).containsIgnoringCase("testowy");
        verify(jpaRepository).searchByNameOrDescription(query, TEST_USER_ID, pageable);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void searchByNameOrDescription_ShouldReturnEmptyList_WhenNoMatches() {
        // given
        String query = "nieistniejący";
        int limit = 10;
        Pageable pageable = PageRequest.of(0, limit);
        when(jpaRepository.searchByNameOrDescription(query, TEST_USER_ID, pageable))
                .thenReturn(Collections.emptyList());

        // when
        List<DietTemplate> result = repository.searchByNameOrDescription(query, TEST_USER_ID, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).searchByNameOrDescription(query, TEST_USER_ID, pageable);
    }

    @Test
    void searchByNameOrDescription_ShouldReturnEmptyList_WhenRepositoryThrowsException() {
        // given
        String query = "testowy";
        int limit = 10;
        Pageable pageable = PageRequest.of(0, limit);
        when(jpaRepository.searchByNameOrDescription(query, TEST_USER_ID, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // when
        List<DietTemplate> result = repository.searchByNameOrDescription(query, TEST_USER_ID, limit);

        // then
        assertThat(result).isEmpty();
        verify(jpaRepository).searchByNameOrDescription(query, TEST_USER_ID, pageable);
    }

    @Test
    void save_ShouldSaveNewTemplate_WhenIdIsNull() {
        // given
        DietTemplate templateWithoutId = DietTemplate.builder()
                .name("Nowy szablon")
                .description("Nowy opis")
                .category(DietTemplateCategory.MAINTENANCE)
                .createdBy(TEST_USER_ID)
                .build();

        DietTemplateEntity newEntity = DietTemplateEntity.builder()
                .name("Nowy szablon")
                .description("Nowy opis")
                .category(DietTemplateCategory.MAINTENANCE)
                .createdBy(TEST_USER_ID)
                .build();

        when(converter.toEntity(templateWithoutId)).thenReturn(newEntity);
        when(jpaRepository.save(newEntity)).thenReturn(mockEntity);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        DietTemplate result = repository.save(templateWithoutId);

        // then
        assertThat(result).isNotNull();
        verify(converter).toEntity(templateWithoutId);
        verify(jpaRepository).save(newEntity);
        verify(converter).toModel(mockEntity);
    }

    @Test
    void save_ShouldUpdateExistingTemplate_WhenIdExists() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        doNothing().when(converter).updateEntity(mockEntity, mockTemplate);
        when(jpaRepository.save(mockEntity)).thenReturn(mockEntity);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        DietTemplate result = repository.save(mockTemplate);

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
    void save_ShouldCreateNewTemplate_WhenIdExistsButEntityNotFound() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());
        when(converter.toEntity(mockTemplate)).thenReturn(mockEntity);
        when(jpaRepository.save(mockEntity)).thenReturn(mockEntity);
        when(converter.toModel(mockEntity)).thenReturn(mockTemplate);

        // when
        DietTemplate result = repository.save(mockTemplate);

        // then
        assertThat(result).isNotNull();
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(converter).toEntity(mockTemplate);
        verify(jpaRepository).save(mockEntity);
        verify(converter).toModel(mockEntity);
        verify(converter, never()).updateEntity(any(), any());
    }

    @Test
    void save_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());
        when(converter.toEntity(mockTemplate)).thenReturn(mockEntity);
        when(jpaRepository.save(mockEntity)).thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.save(mockTemplate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zapisać szablonu");
        verify(jpaRepository).save(mockEntity);
    }

    @Test
    void deleteById_ShouldDeleteTemplate_WhenTemplateExists() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.of(mockEntity));
        doNothing().when(jpaRepository).delete(mockEntity);

        // when
        repository.deleteById(TEST_TEMPLATE_ID);

        // then
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(jpaRepository).delete(mockEntity);
    }

    @Test
    void deleteById_ShouldThrowNotFoundException_WhenTemplateDoesNotExist() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID)).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> repository.deleteById(TEST_TEMPLATE_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Template not found: " + TEST_TEMPLATE_ID);
        verify(jpaRepository).findByExternalId(TEST_TEMPLATE_ID);
        verify(jpaRepository, never()).delete(any());
    }

    @Test
    void deleteById_ShouldThrowRuntimeException_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.findByExternalId(TEST_TEMPLATE_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when/then
        assertThatThrownBy(() -> repository.deleteById(TEST_TEMPLATE_ID))
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

        repository.incrementUsageCount(TEST_TEMPLATE_ID);

        // then
        verify(jpaRepository).incrementUsageCount(TEST_TEMPLATE_ID);
    }

    @Test
    void countByCreatedBy_ShouldReturnCount_WhenTemplatesExist() {
        // given
        long expectedCount = 5L;
        when(jpaRepository.countByCreatedBy(TEST_USER_ID)).thenReturn(expectedCount);

        // when
        long result = repository.countByCreatedBy(TEST_USER_ID);

        // then
        assertThat(result).isEqualTo(expectedCount);
        verify(jpaRepository).countByCreatedBy(TEST_USER_ID);
    }

    @Test
    void countByCreatedBy_ShouldReturnZero_WhenNoTemplatesExist() {
        // given
        when(jpaRepository.countByCreatedBy(TEST_USER_ID)).thenReturn(0L);

        // when
        long result = repository.countByCreatedBy(TEST_USER_ID);

        // then
        assertThat(result).isEqualTo(0L);
        verify(jpaRepository).countByCreatedBy(TEST_USER_ID);
    }

    @Test
    void countByCreatedBy_ShouldReturnZero_WhenRepositoryThrowsException() {
        // given
        when(jpaRepository.countByCreatedBy(TEST_USER_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when
        long result = repository.countByCreatedBy(TEST_USER_ID);

        // then
        assertThat(result).isEqualTo(0L);
        verify(jpaRepository).countByCreatedBy(TEST_USER_ID);
    }

    private DietTemplateEntity createAnotherEntity() {
        return DietTemplateEntity.builder()
                .id(2L)
                .externalId("dt_another123456")
                .name("Inny szablon")
                .description("Inny opis")
                .category(DietTemplateCategory.WEIGHT_GAIN)
                .createdBy(TEST_USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .duration(14)
                .mealsPerDay(4)
                .usageCount(5)
                .build();
    }
}
