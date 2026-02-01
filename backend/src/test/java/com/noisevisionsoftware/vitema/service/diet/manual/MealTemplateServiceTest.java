package com.noisevisionsoftware.vitema.service.diet.manual;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.exception.NotFoundException;
import com.noisevisionsoftware.vitema.model.meal.MealIngredient;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.repository.meal.MealTemplateRepository;
import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MealTemplateService Unit Tests")
class MealTemplateServiceTest {

    @Mock
    private MealTemplateRepository mealTemplateRepository;

    @Mock
    private ProductCategorizationService categorizationService;

    @InjectMocks
    private MealTemplateService mealTemplateService;

    private MealTemplate testTemplate;
    private String testTemplateId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testTemplateId = "template-123";
        testUserId = "user-456";

        testTemplate = MealTemplate.builder()
                .id(testTemplateId)
                .name("Chicken Caesar Salad")
                .instructions("Mix ingredients and serve")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(400.0)
                        .protein(35.0)
                        .carbs(20.0)
                        .fat(22.0)
                        .build())
                .photos(Arrays.asList("photo1.jpg", "photo2.jpg"))
                .ingredients(new ArrayList<>())
                .usageCount(5)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .lastUsed(Timestamp.now())
                .build();
    }

    @Nested
    @DisplayName("getById() tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return meal template when found")
        void shouldReturnTemplate_When_TemplateExists() {
            // Given
            when(mealTemplateRepository.findById(testTemplateId)).thenReturn(Optional.of(testTemplate));

            // When
            MealTemplate result = mealTemplateService.getById(testTemplateId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testTemplateId);
            assertThat(result.getName()).isEqualTo("Chicken Caesar Salad");
            verify(mealTemplateRepository).findById(testTemplateId);
        }

        @Test
        @DisplayName("Should throw NotFoundException when template not found")
        void shouldThrowNotFoundException_When_TemplateDoesNotExist() {
            // Given
            String nonExistentId = "non-existent-id";
            when(mealTemplateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> mealTemplateService.getById(nonExistentId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Meal template not found with id: " + nonExistentId);

            verify(mealTemplateRepository).findById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null id gracefully")
        void shouldHandleNullId_When_GetById() {
            // Given
            when(mealTemplateRepository.findById(null)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> mealTemplateService.getById(null))
                    .isInstanceOf(NotFoundException.class);

            verify(mealTemplateRepository).findById(null);
        }
    }

    @Nested
    @DisplayName("searchAccessibleTemplates() tests")
    class SearchAccessibleTemplatesTests {

        @Test
        @DisplayName("Should return list of accessible templates")
        void shouldReturnTemplates_When_SearchIsSuccessful() {
            // Given
            String query = "chicken";
            int limit = 10;
            List<MealTemplate> expectedTemplates = Arrays.asList(testTemplate, createTemplate("template-2", "Chicken Soup"));

            when(mealTemplateRepository.searchAccessibleTemplates(query, testUserId, limit))
                    .thenReturn(expectedTemplates);

            // When
            List<MealTemplate> result = mealTemplateService.searchAccessibleTemplates(query, testUserId, limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(testTemplate, expectedTemplates.get(1));
            verify(mealTemplateRepository).searchAccessibleTemplates(query, testUserId, limit);
        }

        @Test
        @DisplayName("Should return empty list when no templates found")
        void shouldReturnEmptyList_When_NoTemplatesFound() {
            // Given
            String query = "nonexistent";
            int limit = 10;
            when(mealTemplateRepository.searchAccessibleTemplates(query, testUserId, limit))
                    .thenReturn(Collections.emptyList());

            // When
            List<MealTemplate> result = mealTemplateService.searchAccessibleTemplates(query, testUserId, limit);

            // Then
            assertThat(result).isEmpty();
            verify(mealTemplateRepository).searchAccessibleTemplates(query, testUserId, limit);
        }

        @Test
        @DisplayName("Should return empty list when exception occurs")
        void shouldReturnEmptyList_When_ExceptionOccurs() {
            // Given
            String query = "chicken";
            int limit = 10;
            when(mealTemplateRepository.searchAccessibleTemplates(query, testUserId, limit))
                    .thenThrow(new RuntimeException("Database error"));

            // When
            List<MealTemplate> result = mealTemplateService.searchAccessibleTemplates(query, testUserId, limit);

            // Then
            assertThat(result).isEmpty();
            verify(mealTemplateRepository).searchAccessibleTemplates(query, testUserId, limit);
        }

        @Test
        @DisplayName("Should handle null query gracefully")
        void shouldReturnEmptyList_When_QueryIsNull() {
            // Given
            when(mealTemplateRepository.searchAccessibleTemplates(null, testUserId, 10))
                    .thenReturn(Collections.emptyList());

            // When
            List<MealTemplate> result = mealTemplateService.searchAccessibleTemplates(null, testUserId, 10);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchTemplates() tests")
    class SearchTemplatesTests {

        @Test
        @DisplayName("Should return list of templates matching query")
        void shouldReturnTemplates_When_SearchByNameIsSuccessful() {
            // Given
            String query = "salad";
            int limit = 5;
            List<MealTemplate> expectedTemplates = Collections.singletonList(testTemplate);

            when(mealTemplateRepository.searchByName(query, limit)).thenReturn(expectedTemplates);

            // When
            List<MealTemplate> result = mealTemplateService.searchTemplates(query, limit);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(testTemplate);
            verify(mealTemplateRepository).searchByName(query, limit);
        }

        @Test
        @DisplayName("Should return empty list when no templates match")
        void shouldReturnEmptyList_When_NoMatchingTemplates() {
            // Given
            String query = "pizza";
            int limit = 5;
            when(mealTemplateRepository.searchByName(query, limit)).thenReturn(Collections.emptyList());

            // When
            List<MealTemplate> result = mealTemplateService.searchTemplates(query, limit);

            // Then
            assertThat(result).isEmpty();
            verify(mealTemplateRepository).searchByName(query, limit);
        }

        @Test
        @DisplayName("Should return empty list when exception occurs")
        void shouldReturnEmptyList_When_SearchThrowsException() {
            // Given
            String query = "error";
            int limit = 5;
            when(mealTemplateRepository.searchByName(query, limit))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When
            List<MealTemplate> result = mealTemplateService.searchTemplates(query, limit);

            // Then
            assertThat(result).isEmpty();
            verify(mealTemplateRepository).searchByName(query, limit);
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimit_When_Searching() {
            // Given
            String query = "chicken";
            int limit = 3;
            List<MealTemplate> templates = Arrays.asList(
                    createTemplate("t1", "Chicken 1"),
                    createTemplate("t2", "Chicken 2"),
                    createTemplate("t3", "Chicken 3")
            );

            when(mealTemplateRepository.searchByName(query, limit)).thenReturn(templates);

            // When
            List<MealTemplate> result = mealTemplateService.searchTemplates(query, limit);

            // Then
            assertThat(result).hasSize(3);
            verify(mealTemplateRepository).searchByName(query, limit);
        }
    }

    @Nested
    @DisplayName("save() tests")
    class SaveTests {

        @Test
        @DisplayName("Should save template successfully with new timestamps")
        void shouldSaveTemplate_When_AllFieldsAreValid() {
            // Given
            MealTemplate newTemplate = MealTemplate.builder()
                    .name("New Salad")
                    .instructions("New instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .ingredients(new ArrayList<>())
                    .build();

            MealTemplate savedTemplate = MealTemplate.builder()
                    .id("new-template-id")
                    .name("New Salad")
                    .instructions("New instructions")
                    .nutritionalValues(NutritionalValues.builder().calories(300.0).build())
                    .ingredients(new ArrayList<>())
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .build();

            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(savedTemplate);

            // When
            MealTemplate result = mealTemplateService.save(newTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-template-id");
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();

            ArgumentCaptor<MealTemplate> templateCaptor = ArgumentCaptor.forClass(MealTemplate.class);
            verify(mealTemplateRepository).save(templateCaptor.capture());
            verify(mealTemplateRepository).incrementUsageCount("new-template-id");

            MealTemplate capturedTemplate = templateCaptor.getValue();
            assertThat(capturedTemplate.getCreatedAt()).isNotNull();
            assertThat(capturedTemplate.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should preserve createdAt but update updatedAt for existing template")
        void shouldPreserveCreatedAt_When_UpdatingExistingTemplate() {
            // Given
            Timestamp originalCreatedAt = Timestamp.ofTimeSecondsAndNanos(1000000, 0);
            testTemplate.setCreatedAt(originalCreatedAt);

            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(testTemplate);

            // When
            MealTemplate result = mealTemplateService.save(testTemplate);

            // Then
            assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(result.getUpdatedAt()).isNotNull();

            ArgumentCaptor<MealTemplate> templateCaptor = ArgumentCaptor.forClass(MealTemplate.class);
            verify(mealTemplateRepository).save(templateCaptor.capture());

            MealTemplate capturedTemplate = templateCaptor.getValue();
            assertThat(capturedTemplate.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(capturedTemplate.getUpdatedAt()).isNotEqualTo(originalCreatedAt);
        }

        @Test
        @DisplayName("Should auto-categorize ingredients without categoryId")
        void shouldAutoCategorizeIngredients_When_CategoryIdIsNull() {
            // Given
            MealIngredient ingredient1 = MealIngredient.builder()
                    .name("Chicken Breast")
                    .original("200g chicken breast")
                    .categoryId(null)
                    .build();

            MealIngredient ingredient2 = MealIngredient.builder()
                    .name("Lettuce")
                    .original("100g lettuce")
                    .categoryId("existing-category")
                    .build();

            testTemplate.setIngredients(Arrays.asList(ingredient1, ingredient2));
            testTemplate.setCreatedAt(null);

            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("suggested-category-id");
            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(testTemplate);

            // When
            MealTemplate result = mealTemplateService.save(testTemplate);

            // Then
            assertThat(result.getIngredients().get(0).getCategoryId()).isEqualTo("suggested-category-id");
            assertThat(result.getIngredients().get(1).getCategoryId()).isEqualTo("existing-category");

            ArgumentCaptor<ParsedProduct> productCaptor = ArgumentCaptor.forClass(ParsedProduct.class);
            verify(categorizationService).suggestCategory(productCaptor.capture());

            ParsedProduct capturedProduct = productCaptor.getValue();
            assertThat(capturedProduct.getName()).isEqualTo("Chicken Breast");
            assertThat(capturedProduct.getOriginal()).isEqualTo("200g chicken breast");
        }

        @Test
        @DisplayName("Should handle template with null ingredients")
        void shouldHandleNullIngredients_When_Saving() {
            // Given
            testTemplate.setIngredients(null);
            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(testTemplate);

            // When
            MealTemplate result = mealTemplateService.save(testTemplate);

            // Then
            assertThat(result).isNotNull();
            verify(mealTemplateRepository).save(any(MealTemplate.class));
            verify(categorizationService, never()).suggestCategory(any());
        }

        @Test
        @DisplayName("Should handle template with empty ingredients list")
        void shouldHandleEmptyIngredients_When_Saving() {
            // Given
            testTemplate.setIngredients(new ArrayList<>());
            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(testTemplate);

            // When
            MealTemplate result = mealTemplateService.save(testTemplate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getIngredients()).isEmpty();
            verify(mealTemplateRepository).save(any(MealTemplate.class));
            verify(categorizationService, never()).suggestCategory(any());
        }

        @Test
        @DisplayName("Should increment usage count after successful save")
        void shouldIncrementUsageCount_When_SaveIsSuccessful() {
            // Given
            MealTemplate savedTemplate = MealTemplate.builder()
                    .id("saved-id")
                    .name("Test")
                    .build();

            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(savedTemplate);

            // When
            mealTemplateService.save(testTemplate);

            // Then
            verify(mealTemplateRepository).incrementUsageCount("saved-id");
        }

        @Test
        @DisplayName("Should not increment usage count when saved template has null id")
        void shouldNotIncrementUsageCount_When_SavedTemplateIdIsNull() {
            // Given
            MealTemplate savedTemplate = MealTemplate.builder()
                    .id(null)
                    .name("Test")
                    .build();

            when(mealTemplateRepository.save(any(MealTemplate.class))).thenReturn(savedTemplate);

            // When
            mealTemplateService.save(testTemplate);

            // Then
            verify(mealTemplateRepository, never()).incrementUsageCount(any());
        }

        @Test
        @DisplayName("Should throw RuntimeException when save fails")
        void shouldThrowRuntimeException_When_SaveFails() {
            // Given
            when(mealTemplateRepository.save(any(MealTemplate.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> mealTemplateService.save(testTemplate))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Nie udało się zapisać szablonu posiłku");

            verify(mealTemplateRepository).save(any(MealTemplate.class));
        }

        @Test
        @DisplayName("Should wrap and rethrow exception with user-friendly message")
        void shouldWrapException_When_SaveThrowsException() {
            // Given
            RuntimeException originalException = new RuntimeException("Connection timeout");
            when(mealTemplateRepository.save(any(MealTemplate.class))).thenThrow(originalException);

            // When & Then
            assertThatThrownBy(() -> mealTemplateService.save(testTemplate))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Nie udało się zapisać szablonu posiłku")
                    .hasCause(originalException);
        }
    }

    @Nested
    @DisplayName("incrementUsageCount() tests")
    class IncrementUsageCountTests {

        @Test
        @DisplayName("Should increment usage count successfully")
        void shouldIncrementUsageCount_When_TemplateExists() {
            // Given
            doNothing().when(mealTemplateRepository).incrementUsageCount(testTemplateId);

            // When
            mealTemplateService.incrementUsageCount(testTemplateId);

            // Then
            verify(mealTemplateRepository).incrementUsageCount(testTemplateId);
        }

        @Test
        @DisplayName("Should not throw exception when increment fails")
        void shouldNotThrowException_When_IncrementFails() {
            // Given
            doThrow(new RuntimeException("Database error"))
                    .when(mealTemplateRepository).incrementUsageCount(testTemplateId);

            // When & Then - should not throw exception
            mealTemplateService.incrementUsageCount(testTemplateId);

            verify(mealTemplateRepository).incrementUsageCount(testTemplateId);
        }

        @Test
        @DisplayName("Should handle null id gracefully")
        void shouldHandleNullId_When_IncrementingUsageCount() {
            // Given
            doThrow(new RuntimeException("Invalid id"))
                    .when(mealTemplateRepository).incrementUsageCount(null);

            // When & Then - should not throw exception
            mealTemplateService.incrementUsageCount(null);

            verify(mealTemplateRepository).incrementUsageCount(null);
        }
    }

    @Nested
    @DisplayName("getPopularTemplates() tests")
    class GetPopularTemplatesTests {

        @Test
        @DisplayName("Should return list of popular templates")
        void shouldReturnPopularTemplates_When_TemplatesExist() {
            // Given
            int limit = 10;
            MealTemplate popular1 = createTemplate("pop-1", "Popular 1");
            popular1.setUsageCount(100);
            MealTemplate popular2 = createTemplate("pop-2", "Popular 2");
            popular2.setUsageCount(75);

            List<MealTemplate> popularTemplates = Arrays.asList(popular1, popular2);

            when(mealTemplateRepository.findTopByUsageCount(limit)).thenReturn(popularTemplates);

            // When
            List<MealTemplate> result = mealTemplateService.getPopularTemplates(limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUsageCount()).isEqualTo(100);
            assertThat(result.get(1).getUsageCount()).isEqualTo(75);
            verify(mealTemplateRepository).findTopByUsageCount(limit);
        }

        @Test
        @DisplayName("Should return empty list when no templates exist")
        void shouldReturnEmptyList_When_NoPopularTemplates() {
            // Given
            int limit = 10;
            when(mealTemplateRepository.findTopByUsageCount(limit)).thenReturn(Collections.emptyList());

            // When
            List<MealTemplate> result = mealTemplateService.getPopularTemplates(limit);

            // Then
            assertThat(result).isEmpty();
            verify(mealTemplateRepository).findTopByUsageCount(limit);
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimit_When_GettingPopularTemplates() {
            // Given
            int limit = 5;
            List<MealTemplate> templates = Arrays.asList(
                    createTemplate("1", "T1"),
                    createTemplate("2", "T2"),
                    createTemplate("3", "T3"),
                    createTemplate("4", "T4"),
                    createTemplate("5", "T5")
            );

            when(mealTemplateRepository.findTopByUsageCount(limit)).thenReturn(templates);

            // When
            List<MealTemplate> result = mealTemplateService.getPopularTemplates(limit);

            // Then
            assertThat(result).hasSize(5);
            verify(mealTemplateRepository).findTopByUsageCount(limit);
        }

        @Test
        @DisplayName("Should handle limit of 1")
        void shouldReturnOneTemplate_When_LimitIsOne() {
            // Given
            int limit = 1;
            List<MealTemplate> templates = Collections.singletonList(testTemplate);

            when(mealTemplateRepository.findTopByUsageCount(limit)).thenReturn(templates);

            // When
            List<MealTemplate> result = mealTemplateService.getPopularTemplates(limit);

            // Then
            assertThat(result).hasSize(1);
            verify(mealTemplateRepository).findTopByUsageCount(limit);
        }
    }

    @Nested
    @DisplayName("getRecentTemplates() tests")
    class GetRecentTemplatesTests {

        @Test
        @DisplayName("Should return list of recent templates")
        void shouldReturnRecentTemplates_When_TemplatesExist() {
            // Given
            int limit = 10;
            List<MealTemplate> recentTemplates = Arrays.asList(
                    createTemplate("recent-1", "Recent 1"),
                    createTemplate("recent-2", "Recent 2")
            );

            when(mealTemplateRepository.findRecentlyUsed(limit)).thenReturn(recentTemplates);

            // When
            List<MealTemplate> result = mealTemplateService.getRecentTemplates(limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(recentTemplates.get(0), recentTemplates.get(1));
            verify(mealTemplateRepository).findRecentlyUsed(limit);
        }

        @Test
        @DisplayName("Should return empty list when no recent templates")
        void shouldReturnEmptyList_When_NoRecentTemplates() {
            // Given
            int limit = 10;
            when(mealTemplateRepository.findRecentlyUsed(limit)).thenReturn(Collections.emptyList());

            // When
            List<MealTemplate> result = mealTemplateService.getRecentTemplates(limit);

            // Then
            assertThat(result).isEmpty();
            verify(mealTemplateRepository).findRecentlyUsed(limit);
        }

        @Test
        @DisplayName("Should respect limit parameter")
        void shouldRespectLimit_When_GettingRecentTemplates() {
            // Given
            int limit = 3;
            List<MealTemplate> templates = Arrays.asList(
                    createTemplate("1", "Recent 1"),
                    createTemplate("2", "Recent 2"),
                    createTemplate("3", "Recent 3")
            );

            when(mealTemplateRepository.findRecentlyUsed(limit)).thenReturn(templates);

            // When
            List<MealTemplate> result = mealTemplateService.getRecentTemplates(limit);

            // Then
            assertThat(result).hasSize(3);
            verify(mealTemplateRepository).findRecentlyUsed(limit);
        }
    }

    @Nested
    @DisplayName("refreshCache() tests")
    class RefreshCacheTests {

        @Test
        @DisplayName("Should execute without errors")
        void shouldExecuteSuccessfully_When_RefreshingCache() {
            // Given - no setup needed

            // When
            mealTemplateService.refreshCache();

            // Then - method should complete without exceptions
            verifyNoInteractions(mealTemplateRepository);
            verifyNoInteractions(categorizationService);
        }
    }

    // Helper method to create test meal templates
    private MealTemplate createTemplate(String id, String name) {
        return MealTemplate.builder()
                .id(id)
                .name(name)
                .instructions("Test instructions")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(300.0)
                        .protein(25.0)
                        .carbs(20.0)
                        .fat(15.0)
                        .build())
                .photos(new ArrayList<>())
                .ingredients(new ArrayList<>())
                .usageCount(1)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .lastUsed(Timestamp.now())
                .build();
    }
}
