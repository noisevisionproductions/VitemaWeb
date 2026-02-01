package com.noisevisionsoftware.vitema.mapper.diet;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.DietTemplateRequest;
import com.noisevisionsoftware.vitema.model.diet.template.*;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DietTemplateMapperTest {

    private DietTemplateMapper mapper;
    private static final String SAMPLE_ID = "template-123";
    private static final String SAMPLE_NAME = "Test Template";
    private static final String SAMPLE_DESCRIPTION = "Test description";
    private static final String SAMPLE_CREATED_BY = "user-456";

    @BeforeEach
    void setUp() {
        mapper = new DietTemplateMapper();
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponseTests {

        @Test
        @DisplayName("Should return null when template is null")
        void givenNullTemplate_When_ToResponse_Then_ReturnNull() {
            assertThat(mapper.toResponse(null)).isNull();
        }

        @Test
        @DisplayName("Should convert template to response with basic fields")
        void givenTemplateWithBasicFields_When_ToResponse_Then_ConvertAllFields() {
            // Given
            Timestamp now = Timestamp.now();
            DietTemplate template = DietTemplate.builder()
                    .id(SAMPLE_ID)
                    .name(SAMPLE_NAME)
                    .description(SAMPLE_DESCRIPTION)
                    .category(DietTemplateCategory.WEIGHT_LOSS)
                    .createdBy(SAMPLE_CREATED_BY)
                    .createdAt(now)
                    .updatedAt(now)
                    .version(1)
                    .duration(7)
                    .mealsPerDay(5)
                    .mealTimes(Map.of("BREAKFAST", "08:00"))
                    .mealTypes(List.of("BREAKFAST", "LUNCH", "DINNER"))
                    .days(Collections.emptyList())
                    .usageCount(3)
                    .notes("Template notes")
                    .build();

            // When
            var result = mapper.toResponse(template);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(SAMPLE_ID);
            assertThat(result.getName()).isEqualTo(SAMPLE_NAME);
            assertThat(result.getDescription()).isEqualTo(SAMPLE_DESCRIPTION);
            assertThat(result.getCategory()).isEqualTo("WEIGHT_LOSS");
            assertThat(result.getCategoryLabel()).isEqualTo("Odchudzanie");
            assertThat(result.getCreatedBy()).isEqualTo(SAMPLE_CREATED_BY);
            assertThat(result.getVersion()).isEqualTo(1);
            assertThat(result.getDuration()).isEqualTo(7);
            assertThat(result.getMealsPerDay()).isEqualTo(5);
            assertThat(result.getMealTimes()).containsEntry("BREAKFAST", "08:00");
            assertThat(result.getMealTypes()).containsExactly("BREAKFAST", "LUNCH", "DINNER");
            assertThat(result.getUsageCount()).isEqualTo(3);
            assertThat(result.getNotes()).isEqualTo("Template notes");
            assertThat(result.getTotalMeals()).isZero();
            assertThat(result.getTotalIngredients()).isZero();
            assertThat(result.isHasPhotos()).isFalse();
        }

        @Test
        @DisplayName("Should include target nutrition in response")
        void givenTemplateWithTargetNutrition_When_ToResponse_Then_IncludeNutrition() {
            // Given
            DietTemplateNutrition nutrition = DietTemplateNutrition.builder()
                    .targetCalories(2000.0)
                    .targetProtein(150.0)
                    .targetFat(65.0)
                    .targetCarbs(250.0)
                    .calculationMethod("TDEE")
                    .build();
            DietTemplate template = createMinimalTemplate();
            template.setTargetNutrition(nutrition);

            // When
            var result = mapper.toResponse(template);

            // Then
            assertThat(result.getTargetNutrition()).isNotNull();
            assertThat(result.getTargetNutrition().getTargetCalories()).isEqualTo(2000.0);
            assertThat(result.getTargetNutrition().getTargetProtein()).isEqualTo(150.0);
            assertThat(result.getTargetNutrition().getTargetFat()).isEqualTo(65.0);
            assertThat(result.getTargetNutrition().getTargetCarbs()).isEqualTo(250.0);
            assertThat(result.getTargetNutrition().getCalculationMethod()).isEqualTo("TDEE");
        }

        @Test
        @DisplayName("Should calculate totalMeals and totalIngredients correctly")
        void givenTemplateWithDaysAndMeals_When_ToResponse_Then_CalculateTotals() {
            // Given
            DietTemplateIngredient ingredient1 = DietTemplateIngredient.builder()
                    .name("Egg")
                    .quantity(1.0)
                    .unit("szt")
                    .build();
            DietTemplateIngredient ingredient2 = DietTemplateIngredient.builder()
                    .name("Bread")
                    .quantity(2.0)
                    .unit("szt")
                    .build();
            DietTemplateMealData meal1 = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .ingredients(List.of(ingredient1, ingredient2))
                    .build();
            DietTemplateMealData meal2 = DietTemplateMealData.builder()
                    .name("Lunch")
                    .mealType(MealType.LUNCH)
                    .ingredients(List.of(ingredient1))
                    .build();
            DietTemplateDayData day = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .meals(List.of(meal1, meal2))
                    .build();
            DietTemplate template = createMinimalTemplate();
            template.setDays(List.of(day));

            // When
            var result = mapper.toResponse(template);

            // Then
            assertThat(result.getTotalMeals()).isEqualTo(2);
            assertThat(result.getTotalIngredients()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should set hasPhotos true when meal has photos")
        void givenTemplateWithMealPhotos_When_ToResponse_Then_HasPhotosTrue() {
            // Given
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .photos(List.of("photo1.jpg"))
                    .ingredients(Collections.emptyList())
                    .build();
            DietTemplateDayData day = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .meals(List.of(meal))
                    .build();
            DietTemplate template = createMinimalTemplate();
            template.setDays(List.of(day));

            // When
            var result = mapper.toResponse(template);

            // Then
            assertThat(result.isHasPhotos()).isTrue();
        }

        @Test
        @DisplayName("Should convert days with meals to response")
        void givenTemplateWithDays_When_ToResponse_Then_IncludeDays() {
            // Given
            DietTemplateIngredient ingredient = DietTemplateIngredient.builder()
                    .name("Oatmeal")
                    .quantity(100.0)
                    .unit("g")
                    .original("100g oatmeal")
                    .categoryId("cat-1")
                    .hasCustomUnit(false)
                    .build();
            NutritionalValues nutrition = NutritionalValues.builder()
                    .calories(150.0)
                    .protein(5.0)
                    .fat(3.0)
                    .carbs(27.0)
                    .build();
            DietTemplateMealData meal = DietTemplateMealData.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .instructions("Cook oatmeal")
                    .ingredients(List.of(ingredient))
                    .nutritionalValues(nutrition)
                    .mealTemplateId("meal-1")
                    .build();
            DietTemplateDayData day = DietTemplateDayData.builder()
                    .dayNumber(1)
                    .dayName("Day 1")
                    .notes("First day")
                    .meals(List.of(meal))
                    .build();
            DietTemplate template = createMinimalTemplate();
            template.setDays(List.of(day));

            // When
            var result = mapper.toResponse(template);

            // Then
            assertThat(result.getDays()).hasSize(1);
            assertThat(result.getDays().getFirst().getDayNumber()).isEqualTo(1);
            assertThat(result.getDays().getFirst().getDayName()).isEqualTo("Day 1");
            assertThat(result.getDays().getFirst().getNotes()).isEqualTo("First day");
            assertThat(result.getDays().getFirst().getMeals()).hasSize(1);
            assertThat(result.getDays().getFirst().getMeals().getFirst().getName()).isEqualTo("Breakfast");
            assertThat(result.getDays().getFirst().getMeals().getFirst().getMealType()).isEqualTo("BREAKFAST");
            assertThat(result.getDays().getFirst().getMeals().getFirst().getIngredients().getFirst().getName()).isEqualTo("Oatmeal");
        }
    }

    @Nested
    @DisplayName("fromRequest")
    class FromRequestTests {

        @Test
        @DisplayName("Should create DietTemplate from request")
        void givenValidRequest_When_FromRequest_Then_CreateTemplate() {
            // Given
            DietTemplateRequest request = DietTemplateRequest.builder()
                    .name(SAMPLE_NAME)
                    .description(SAMPLE_DESCRIPTION)
                    .category("WEIGHT_LOSS")
                    .duration(14)
                    .mealsPerDay(5)
                    .mealTimes(Map.of("BREAKFAST", "07:30"))
                    .mealTypes(List.of("BREAKFAST", "LUNCH", "DINNER"))
                    .notes("Request notes")
                    .build();

            // When
            DietTemplate result = mapper.fromRequest(request, SAMPLE_CREATED_BY);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(SAMPLE_NAME);
            assertThat(result.getDescription()).isEqualTo(SAMPLE_DESCRIPTION);
            assertThat(result.getCategory()).isEqualTo(DietTemplateCategory.WEIGHT_LOSS);
            assertThat(result.getCreatedBy()).isEqualTo(SAMPLE_CREATED_BY);
            assertThat(result.getDuration()).isEqualTo(14);
            assertThat(result.getMealsPerDay()).isEqualTo(5);
            assertThat(result.getMealTimes()).containsEntry("BREAKFAST", "07:30");
            assertThat(result.getMealTypes()).containsExactly("BREAKFAST", "LUNCH", "DINNER");
            assertThat(result.getNotes()).isEqualTo("Request notes");
        }

        @Test
        @DisplayName("Should handle lowercase category in request")
        void givenRequestWithLowercaseCategory_When_FromRequest_Then_ConvertToEnum() {
            // Given
            DietTemplateRequest request = DietTemplateRequest.builder()
                    .name(SAMPLE_NAME)
                    .category("maintenance")
                    .duration(7)
                    .mealsPerDay(4)
                    .build();

            // When
            DietTemplate result = mapper.fromRequest(request, SAMPLE_CREATED_BY);

            // Then
            assertThat(result.getCategory()).isEqualTo(DietTemplateCategory.MAINTENANCE);
        }
    }

    @Nested
    @DisplayName("updateFromRequest")
    class UpdateFromRequestTests {

        @Test
        @DisplayName("Should update existing template from request")
        void givenExistingTemplateAndRequest_When_UpdateFromRequest_Then_UpdateFields() {
            // Given
            DietTemplate existing = DietTemplate.builder()
                    .id(SAMPLE_ID)
                    .name("Old Name")
                    .description("Old description")
                    .category(DietTemplateCategory.CUSTOM)
                    .createdBy(SAMPLE_CREATED_BY)
                    .duration(7)
                    .mealsPerDay(3)
                    .notes("Old notes")
                    .build();

            DietTemplateRequest request = DietTemplateRequest.builder()
                    .name("New Name")
                    .description("New description")
                    .category("VEGETARIAN")
                    .duration(21)
                    .mealsPerDay(6)
                    .mealTimes(Map.of("LUNCH", "13:00"))
                    .mealTypes(List.of("BREAKFAST", "LUNCH"))
                    .notes("New notes")
                    .build();

            // When
            DietTemplate result = mapper.updateFromRequest(existing, request);

            // Then
            assertThat(result).isSameAs(existing);
            assertThat(existing.getName()).isEqualTo("New Name");
            assertThat(existing.getDescription()).isEqualTo("New description");
            assertThat(existing.getCategory()).isEqualTo(DietTemplateCategory.VEGETARIAN);
            assertThat(existing.getDuration()).isEqualTo(21);
            assertThat(existing.getMealsPerDay()).isEqualTo(6);
            assertThat(existing.getMealTimes()).containsEntry("LUNCH", "13:00");
            assertThat(existing.getMealTypes()).containsExactly("BREAKFAST", "LUNCH");
            assertThat(existing.getNotes()).isEqualTo("New notes");
        }
    }

    private DietTemplate createMinimalTemplate() {
        Timestamp now = Timestamp.now();
        return DietTemplate.builder()
                .id(SAMPLE_ID)
                .name(SAMPLE_NAME)
                .category(DietTemplateCategory.CUSTOM)
                .createdBy(SAMPLE_CREATED_BY)
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .duration(7)
                .mealsPerDay(3)
                .mealTimes(Collections.emptyMap())
                .mealTypes(Collections.emptyList())
                .days(Collections.emptyList())
                .build();
    }
}
