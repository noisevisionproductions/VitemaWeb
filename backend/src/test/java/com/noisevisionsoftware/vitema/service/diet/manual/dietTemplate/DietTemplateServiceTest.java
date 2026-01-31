package com.noisevisionsoftware.vitema.service.diet.manual.dietTemplate;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.DietTemplateRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.DietTemplateResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.DietTemplateStatsResponse;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.mapper.diet.DietTemplateMapper;
import com.noisevisionsoftware.vitema.model.diet.template.DietTemplate;
import com.noisevisionsoftware.vitema.model.diet.template.DietTemplateCategory;
import com.noisevisionsoftware.vitema.repository.diet.DietTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DietTemplateServiceTest {

    @Mock
    private DietTemplateMapper dietTemplateMapper;

    @Mock
    private DietTemplateRepository dietTemplateRepository;

    @Mock
    private DietTemplateConverter dietTemplateConverter;

    @InjectMocks
    private DietTemplateService dietTemplateService;

    private DietTemplate template;
    private DietTemplateResponse templateResponse;
    private DietTemplateRequest templateRequest;
    private String userId;
    private String templateId;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        templateId = "template-123";

        template = DietTemplate.builder()
                .id(templateId)
                .name("Test Template")
                .description("Test Description")
                .category(DietTemplateCategory.WEIGHT_LOSS)
                .createdBy(userId)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .version(1)
                .duration(7)
                .mealsPerDay(3)
                .usageCount(5)
                .build();

        templateResponse = DietTemplateResponse.builder()
                .id(templateId)
                .name("Test Template")
                .description("Test Description")
                .category("WEIGHT_LOSS")
                .createdBy(userId)
                .version(1)
                .duration(7)
                .mealsPerDay(3)
                .usageCount(5)
                .build();

        templateRequest = DietTemplateRequest.builder()
                .name("New Template")
                .description("New Description")
                .category("WEIGHT_LOSS")
                .duration(14)
                .mealsPerDay(4)
                .build();
    }

    @Nested
    @DisplayName("saveTemplate")
    class SaveTemplateTests {

        @Test
        @DisplayName("Should generate ID and set timestamps when saving new template")
        void givenTemplateWithoutId_When_SaveTemplate_Then_GenerateIdAndSetTimestamps() {
            // Given
            DietTemplate newTemplate = DietTemplate.builder()
                    .name("New Template")
                    .description("New Description")
                    .category(DietTemplateCategory.CUSTOM)
                    .createdBy(userId)
                    .build();
            newTemplate.setId(null);

            when(dietTemplateRepository.save(any(DietTemplate.class))).thenAnswer(invocation -> {
                DietTemplate saved = invocation.getArgument(0);
                return saved;
            });

            // When
            DietTemplate result = dietTemplateService.saveTemplate(newTemplate);

            // Then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
            assertThat(result.getVersion()).isEqualTo(1);
            assertThat(result.getUsageCount()).isEqualTo(0);
            verify(dietTemplateRepository).save(any(DietTemplate.class));
        }

        @Test
        @DisplayName("Should update timestamp when saving existing template")
        void givenTemplateWithId_When_SaveTemplate_Then_UpdateTimestampOnly() {
            // Given
            Timestamp originalCreatedAt = Timestamp.now();
            template.setCreatedAt(originalCreatedAt);
            template.setUpdatedAt(originalCreatedAt);

            when(dietTemplateRepository.save(any(DietTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DietTemplate result = dietTemplateService.saveTemplate(template);

            // Then
            assertThat(result.getId()).isEqualTo(templateId);
            assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(result.getUpdatedAt()).isNotNull();
            assertThat(result.getUpdatedAt().compareTo(originalCreatedAt)).isGreaterThanOrEqualTo(0);
            verify(dietTemplateRepository).save(template);
        }
    }

    @Nested
    @DisplayName("incrementUsageCount")
    class IncrementUsageCountTests {

        @Test
        @DisplayName("Should increment usage count successfully")
        void givenValidTemplateId_When_IncrementUsageCount_Then_CallRepository() {
            // Given
            doNothing().when(dietTemplateRepository).incrementUsageCount(templateId);

            // When
            dietTemplateService.incrementUsageCount(templateId);

            // Then
            verify(dietTemplateRepository).incrementUsageCount(templateId);
        }

        @Test
        @DisplayName("Should throw RuntimeException when repository throws exception")
        void givenRepositoryException_When_IncrementUsageCount_Then_ThrowRuntimeException() {
            // Given
            doThrow(new RuntimeException("Database error")).when(dietTemplateRepository).incrementUsageCount(templateId);

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> dietTemplateService.incrementUsageCount(templateId))
                    .withMessageContaining("Nie udało się zaktualizować licznika użyć");
            verify(dietTemplateRepository).incrementUsageCount(templateId);
        }
    }

    @Nested
    @DisplayName("getAllTemplatesForUser")
    class GetAllTemplatesForUserTests {

        @Test
        @DisplayName("Should return all templates for user")
        void givenUserId_When_GetAllTemplatesForUser_Then_ReturnAllTemplates() {
            // Given
            List<DietTemplate> templates = List.of(template);
            when(dietTemplateRepository.findByCreatedBy(userId)).thenReturn(templates);
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            List<DietTemplateResponse> result = dietTemplateService.getAllTemplatesForUser(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(dietTemplateRepository).findByCreatedBy(userId);
            verify(dietTemplateMapper).toResponse(template);
        }

        @Test
        @DisplayName("Should return empty list when user has no templates")
        void givenUserIdWithNoTemplates_When_GetAllTemplatesForUser_Then_ReturnEmptyList() {
            // Given
            when(dietTemplateRepository.findByCreatedBy(userId)).thenReturn(Collections.emptyList());

            // When
            List<DietTemplateResponse> result = dietTemplateService.getAllTemplatesForUser(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(dietTemplateRepository).findByCreatedBy(userId);
            verify(dietTemplateMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("getTemplateById")
    class GetTemplateByIdTests {

        @Test
        @DisplayName("Should return template when found")
        void givenValidTemplateId_When_GetTemplateById_Then_ReturnTemplate() {
            // Given
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(dietTemplateMapper.toResponse(template)).thenReturn(templateResponse);

            // When
            DietTemplateResponse result = dietTemplateService.getTemplateById(templateId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateMapper).toResponse(template);
        }

        @Test
        @DisplayName("Should throw NotFoundException when template not found")
        void givenInvalidTemplateId_When_GetTemplateById_Then_ThrowNotFoundException() {
            // Given
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> dietTemplateService.getTemplateById(templateId))
                    .withMessageContaining("Szablon nie został znaleziony");
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("getTemplatesByCategory")
    class GetTemplatesByCategoryTests {

        @Test
        @DisplayName("Should return templates for specific category")
        void givenCategoryAndUserId_When_GetTemplatesByCategory_Then_ReturnTemplates() {
            // Given
            String categoryStr = "weight_loss";
            List<DietTemplate> templates = List.of(template);
            when(dietTemplateRepository.findByCategoryAndCreatedBy(DietTemplateCategory.WEIGHT_LOSS, userId))
                    .thenReturn(templates);
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            List<DietTemplateResponse> result = dietTemplateService.getTemplatesByCategory(categoryStr, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(dietTemplateRepository).findByCategoryAndCreatedBy(DietTemplateCategory.WEIGHT_LOSS, userId);
            verify(dietTemplateMapper).toResponse(template);
        }

        @Test
        @DisplayName("Should handle uppercase category string")
        void givenUppercaseCategory_When_GetTemplatesByCategory_Then_ConvertToEnum() {
            // Given
            String categoryStr = "WEIGHT_LOSS";
            when(dietTemplateRepository.findByCategoryAndCreatedBy(DietTemplateCategory.WEIGHT_LOSS, userId))
                    .thenReturn(Collections.emptyList());

            // When
            dietTemplateService.getTemplatesByCategory(categoryStr, userId);

            // Then
            verify(dietTemplateRepository).findByCategoryAndCreatedBy(DietTemplateCategory.WEIGHT_LOSS, userId);
        }
    }

    @Nested
    @DisplayName("getMostUsedTemplates")
    class GetMostUsedTemplatesTests {

        @Test
        @DisplayName("Should return most used templates")
        void givenUserIdAndLimit_When_GetMostUsedTemplates_Then_ReturnTemplates() {
            // Given
            int limit = 5;
            List<DietTemplate> templates = List.of(template);
            when(dietTemplateRepository.findTopByCreatedByOrderByUsageCountDesc(userId, limit))
                    .thenReturn(templates);
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            List<DietTemplateResponse> result = dietTemplateService.getMostUsedTemplates(userId, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(dietTemplateRepository).findTopByCreatedByOrderByUsageCountDesc(userId, limit);
            verify(dietTemplateMapper).toResponse(template);
        }
    }

    @Nested
    @DisplayName("searchTemplates")
    class SearchTemplatesTests {

        @Test
        @DisplayName("Should return templates matching search query")
        void givenQueryAndUserId_When_SearchTemplates_Then_ReturnMatchingTemplates() {
            // Given
            String query = "test";
            int limit = 10;
            List<DietTemplate> templates = List.of(template);
            when(dietTemplateRepository.searchByNameOrDescription(query, userId, limit)).thenReturn(templates);
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            List<DietTemplateResponse> result = dietTemplateService.searchTemplates(query, userId, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(dietTemplateRepository).searchByNameOrDescription(query, userId, limit);
            verify(dietTemplateMapper).toResponse(template);
        }
    }

    @Nested
    @DisplayName("createTemplate")
    class CreateTemplateTests {

        @Test
        @DisplayName("Should create template successfully")
        void givenValidRequest_When_CreateTemplate_Then_CreateAndReturnTemplate() {
            // Given
            when(dietTemplateMapper.fromRequest(templateRequest, userId)).thenReturn(template);
            when(dietTemplateRepository.save(any(DietTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            DietTemplateResponse result = dietTemplateService.createTemplate(templateRequest, userId);

            // Then
            assertThat(result).isNotNull();
            verify(dietTemplateMapper).fromRequest(templateRequest, userId);
            verify(dietTemplateRepository).save(any(DietTemplate.class));
            verify(dietTemplateMapper).toResponse(any(DietTemplate.class));
        }
    }

    @Nested
    @DisplayName("createTemplateFromManualDiet")
    class CreateTemplateFromManualDietTests {

        @Test
        @DisplayName("Should create template from manual diet successfully")
        void givenValidRequestWithDietData_When_CreateTemplateFromManualDiet_Then_CreateTemplate() {
            // Given
            ManualDietRequest dietData = ManualDietRequest.builder()
                    .days(new ArrayList<>())
                    .mealsPerDay(3)
                    .duration(7)
                    .build();

            DietTemplateRequest requestWithDietData = DietTemplateRequest.builder()
                    .name("Template from Diet")
                    .description("Description")
                    .category("WEIGHT_LOSS")
                    .duration(7)
                    .mealsPerDay(3)
                    .dietData(dietData)
                    .build();

            when(dietTemplateConverter.convertFromManualDiet(
                    any(ManualDietRequest.class), anyString(), anyString(), any(DietTemplateCategory.class), anyString()))
                    .thenReturn(template);
            when(dietTemplateRepository.save(any(DietTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            DietTemplateResponse result = dietTemplateService.createTemplateFromManualDiet(requestWithDietData, userId);

            // Then
            assertThat(result).isNotNull();
            verify(dietTemplateConverter).convertFromManualDiet(
                    eq(dietData), eq("Template from Diet"), eq("Description"),
                    eq(DietTemplateCategory.WEIGHT_LOSS), eq(userId));
            verify(dietTemplateRepository).save(any(DietTemplate.class));
            verify(dietTemplateMapper).toResponse(any(DietTemplate.class));
        }

        @Test
        @DisplayName("Should set notes when provided")
        void givenRequestWithNotes_When_CreateTemplateFromManualDiet_Then_SetNotes() {
            // Given
            ManualDietRequest dietData = ManualDietRequest.builder()
                    .days(new ArrayList<>())
                    .mealsPerDay(3)
                    .duration(7)
                    .build();

            DietTemplateRequest requestWithNotes = DietTemplateRequest.builder()
                    .name("Template")
                    .description("Description")
                    .category("WEIGHT_LOSS")
                    .duration(7)
                    .mealsPerDay(3)
                    .dietData(dietData)
                    .notes("Test notes")
                    .build();

            when(dietTemplateConverter.convertFromManualDiet(any(), anyString(), anyString(), any(), anyString()))
                    .thenReturn(template);
            when(dietTemplateRepository.save(any(DietTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            dietTemplateService.createTemplateFromManualDiet(requestWithNotes, userId);

            // Then
            ArgumentCaptor<DietTemplate> captor = ArgumentCaptor.forClass(DietTemplate.class);
            verify(dietTemplateRepository).save(captor.capture());
            assertThat(captor.getValue().getNotes()).isEqualTo("Test notes");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when dietData is null")
        void givenRequestWithoutDietData_When_CreateTemplateFromManualDiet_Then_ThrowIllegalArgumentException() {
            // Given
            DietTemplateRequest requestWithoutDietData = DietTemplateRequest.builder()
                    .name("Template")
                    .description("Description")
                    .category("WEIGHT_LOSS")
                    .duration(7)
                    .mealsPerDay(3)
                    .dietData(null)
                    .build();

            // When & Then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> dietTemplateService.createTemplateFromManualDiet(requestWithoutDietData, userId))
                    .withMessage("Dane diety są wymagane");
            verify(dietTemplateConverter, never()).convertFromManualDiet(any(), anyString(), anyString(), any(), anyString());
            verify(dietTemplateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateTemplate")
    class UpdateTemplateTests {

        @Test
        @DisplayName("Should update template when user is owner")
        void givenValidRequestAndOwner_When_UpdateTemplate_Then_UpdateTemplate() {
            // Given
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(dietTemplateMapper.updateFromRequest(template, templateRequest)).thenReturn(template);
            when(dietTemplateRepository.save(any(DietTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            DietTemplateResponse result = dietTemplateService.updateTemplate(templateId, templateRequest, userId);

            // Then
            assertThat(result).isNotNull();
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateMapper).updateFromRequest(template, templateRequest);
            verify(dietTemplateRepository).save(any(DietTemplate.class));
            verify(dietTemplateMapper).toResponse(any(DietTemplate.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when template not found")
        void givenInvalidTemplateId_When_UpdateTemplate_Then_ThrowNotFoundException() {
            // Given
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> dietTemplateService.updateTemplate(templateId, templateRequest, userId))
                    .withMessageContaining("Szablon nie został znaleziony");
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateMapper, never()).updateFromRequest(any(), any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not owner")
        void givenDifferentUserId_When_UpdateTemplate_Then_ThrowAccessDeniedException() {
            // Given
            String differentUserId = "user-456";
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

            // When & Then
            assertThatExceptionOfType(AccessDeniedException.class)
                    .isThrownBy(() -> dietTemplateService.updateTemplate(templateId, templateRequest, differentUserId))
                    .withMessage("Brak uprawnień do edycji tego szablonu");
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateMapper, never()).updateFromRequest(any(), any());
            verify(dietTemplateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteTemplate")
    class DeleteTemplateTests {

        @Test
        @DisplayName("Should delete template when user is owner")
        void givenValidTemplateIdAndOwner_When_DeleteTemplate_Then_DeleteTemplate() {
            // Given
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));
            doNothing().when(dietTemplateRepository).deleteById(templateId);

            // When
            dietTemplateService.deleteTemplate(templateId, userId);

            // Then
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateRepository).deleteById(templateId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when template not found")
        void givenInvalidTemplateId_When_DeleteTemplate_Then_ThrowNotFoundException() {
            // Given
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> dietTemplateService.deleteTemplate(templateId, userId))
                    .withMessageContaining("Szablon nie został znaleziony");
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user is not owner")
        void givenDifferentUserId_When_DeleteTemplate_Then_ThrowAccessDeniedException() {
            // Given
            String differentUserId = "user-456";
            when(dietTemplateRepository.findById(templateId)).thenReturn(Optional.of(template));

            // When & Then
            assertThatExceptionOfType(AccessDeniedException.class)
                    .isThrownBy(() -> dietTemplateService.deleteTemplate(templateId, differentUserId))
                    .withMessage("Brak uprawnień do usunięcia tego szablonu");
            verify(dietTemplateRepository).findById(templateId);
            verify(dietTemplateRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getTemplateStats")
    class GetTemplateStatsTests {

        @Test
        @DisplayName("Should return stats with all templates")
        void givenUserId_When_GetTemplateStats_Then_ReturnStats() {
            // Given
            DietTemplate template1 = DietTemplate.builder()
                    .id("template-1")
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .usageCount(10)
                    .createdAt(Timestamp.now())
                    .build();

            DietTemplate template2 = DietTemplate.builder()
                    .id("template-2")
                    .category(DietTemplateCategory.WEIGHT_GAIN)
                    .usageCount(5)
                    .createdAt(Timestamp.now())
                    .build();

            DietTemplate template3 = DietTemplate.builder()
                    .id("template-3")
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .usageCount(15)
                    .createdAt(Timestamp.now())
                    .build();

            List<DietTemplate> templates = List.of(template1, template2, template3);
            when(dietTemplateRepository.findByCreatedBy(userId)).thenReturn(templates);
            when(dietTemplateMapper.toResponse(any(DietTemplate.class))).thenReturn(templateResponse);

            // When
            DietTemplateStatsResponse result = dietTemplateService.getTemplateStats(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalTemplates()).isEqualTo(3);
            assertThat(result.getTemplatesByCategory()).containsKeys("WEIGHT_LOSS", "WEIGHT_GAIN");
            assertThat(result.getTemplatesByCategory().get("WEIGHT_LOSS")).isEqualTo(2L);
            assertThat(result.getTemplatesByCategory().get("WEIGHT_GAIN")).isEqualTo(1L);
            assertThat(result.getTotalUsageCount()).isEqualTo(30);
            verify(dietTemplateRepository).findByCreatedBy(userId);
        }

        @Test
        @DisplayName("Should return stats with most used template")
        void givenTemplatesWithDifferentUsageCounts_When_GetTemplateStats_Then_ReturnMostUsed() {
            // Given
            Timestamp olderDate = Timestamp.ofTimeMicroseconds(1000000);
            Timestamp newerDate = Timestamp.ofTimeMicroseconds(2000000);
            
            DietTemplate mostUsed = DietTemplate.builder()
                    .id("most-used")
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .usageCount(100)
                    .createdAt(olderDate)
                    .build();

            DietTemplate lessUsed = DietTemplate.builder()
                    .id("less-used")
                    .category(DietTemplateCategory.WEIGHT_GAIN)
                    .usageCount(10)
                    .createdAt(newerDate)
                    .build();

            List<DietTemplate> templates = List.of(mostUsed, lessUsed);
            when(dietTemplateRepository.findByCreatedBy(userId)).thenReturn(templates);
            when(dietTemplateMapper.toResponse(mostUsed)).thenReturn(templateResponse);
            when(dietTemplateMapper.toResponse(lessUsed)).thenReturn(templateResponse);

            // When
            DietTemplateStatsResponse result = dietTemplateService.getTemplateStats(userId);

            // Then
            assertThat(result.getMostUsedTemplate()).isNotNull();
            verify(dietTemplateMapper).toResponse(mostUsed);
        }

        @Test
        @DisplayName("Should return stats with newest template")
        void givenTemplatesWithDifferentDates_When_GetTemplateStats_Then_ReturnNewest() {
            // Given
            Timestamp olderDate = Timestamp.ofTimeMicroseconds(1000000);
            Timestamp newerDate = Timestamp.ofTimeMicroseconds(2000000);

            DietTemplate older = DietTemplate.builder()
                    .id("older")
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .usageCount(100)
                    .createdAt(olderDate)
                    .build();

            DietTemplate newer = DietTemplate.builder()
                    .id("newer")
                    .category(DietTemplateCategory.WEIGHT_GAIN)
                    .usageCount(10)
                    .createdAt(newerDate)
                    .build();

            List<DietTemplate> templates = List.of(older, newer);
            when(dietTemplateRepository.findByCreatedBy(userId)).thenReturn(templates);
            when(dietTemplateMapper.toResponse(older)).thenReturn(templateResponse);
            when(dietTemplateMapper.toResponse(newer)).thenReturn(templateResponse);

            // When
            DietTemplateStatsResponse result = dietTemplateService.getTemplateStats(userId);

            // Then
            assertThat(result.getNewestTemplate()).isNotNull();
            verify(dietTemplateMapper).toResponse(newer);
        }

        @Test
        @DisplayName("Should return null for most used and newest when no templates exist")
        void givenNoTemplates_When_GetTemplateStats_Then_ReturnNullForMostUsedAndNewest() {
            // Given
            when(dietTemplateRepository.findByCreatedBy(userId)).thenReturn(Collections.emptyList());

            // When
            DietTemplateStatsResponse result = dietTemplateService.getTemplateStats(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTotalTemplates()).isEqualTo(0);
            assertThat(result.getMostUsedTemplate()).isNull();
            assertThat(result.getNewestTemplate()).isNull();
            assertThat(result.getTotalUsageCount()).isEqualTo(0);
            assertThat(result.getTemplatesByCategory()).isEmpty();
        }
    }
}
