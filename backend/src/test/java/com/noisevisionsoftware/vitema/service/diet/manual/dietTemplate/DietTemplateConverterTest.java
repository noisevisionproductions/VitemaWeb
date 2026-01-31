package com.noisevisionsoftware.vitema.service.diet.manual.dietTemplate;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.model.diet.template.*;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DietTemplateConverterTest {

    @InjectMocks
    private DietTemplateConverter dietTemplateConverter;

    private ManualDietRequest manualDietRequest;
    private ParsedDay parsedDay;
    private ParsedMeal parsedMeal;
    private ParsedProduct parsedProduct;

    @BeforeEach
    void setUp() {
        parsedProduct = ParsedProduct.builder()
                .name("Chicken Breast")
                .quantity(200.0)
                .unit("g")
                .original("200g chicken breast")
                .hasCustomUnit(false)
                .categoryId("cat-123")
                .build();

        parsedMeal = ParsedMeal.builder()
                .name("Grilled Chicken")
                .mealType(MealType.DINNER)
                .time("18:00")
                .instructions("Grill the chicken for 20 minutes")
                .ingredients(List.of(parsedProduct))
                .nutritionalValues(NutritionalValues.builder()
                        .calories(300.0)
                        .protein(50.0)
                        .fat(10.0)
                        .carbs(5.0)
                        .build())
                .photos(List.of("photo1.jpg", "photo2.jpg"))
                .build();

        parsedDay = ParsedDay.builder()
                .meals(List.of(parsedMeal))
                .build();

        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("BREAKFAST", "08:00");
        mealTimes.put("LUNCH", "13:00");
        mealTimes.put("DINNER", "18:00");

        manualDietRequest = ManualDietRequest.builder()
                .days(List.of(parsedDay))
                .mealsPerDay(3)
                .duration(7)
                .mealTimes(mealTimes)
                .mealTypes(List.of("BREAKFAST", "LUNCH", "DINNER"))
                .build();
    }

    @Nested
    @DisplayName("convertFromManualDiet")
    class ConvertFromManualDietTests {

        @Test
        @DisplayName("Should convert ManualDietRequest to DietTemplate successfully")
        void givenValidManualDietRequest_When_ConvertFromManualDiet_Then_ReturnDietTemplate() {
            // Given
            String templateName = "Test Template";
            String description = "Test Description";
            DietTemplateCategory category = DietTemplateCategory.WEIGHT_LOSS;
            String createdBy = "user-123";

            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    manualDietRequest, templateName, description, category, createdBy);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(templateName);
            assertThat(result.getDescription()).isEqualTo(description);
            assertThat(result.getCategory()).isEqualTo(category);
            assertThat(result.getCreatedBy()).isEqualTo(createdBy);
            assertThat(result.getDuration()).isEqualTo(7);
            assertThat(result.getMealsPerDay()).isEqualTo(3);
            assertThat(result.getMealTimes()).isEqualTo(manualDietRequest.getMealTimes());
            assertThat(result.getMealTypes()).isEqualTo(manualDietRequest.getMealTypes());
        }

        @Test
        @DisplayName("Should convert days correctly with proper day numbers and names")
        void givenManualDietRequestWithDays_When_ConvertFromManualDiet_Then_ConvertDaysCorrectly() {
            // Given
            ParsedDay day1 = ParsedDay.builder()
                    .meals(List.of(parsedMeal))
                    .build();
            ParsedDay day2 = ParsedDay.builder()
                    .meals(List.of(parsedMeal))
                    .build();
            ParsedDay day3 = ParsedDay.builder()
                    .meals(List.of(parsedMeal))
                    .build();

            ManualDietRequest requestWithMultipleDays = ManualDietRequest.builder()
                    .days(List.of(day1, day2, day3))
                    .mealsPerDay(3)
                    .duration(3)
                    .mealTimes(new HashMap<>())
                    .mealTypes(new ArrayList<>())
                    .build();

            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    requestWithMultipleDays, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            assertThat(result.getDays()).isNotNull();
            assertThat(result.getDays()).hasSize(3);
            assertThat(result.getDays().get(0).getDayNumber()).isEqualTo(1);
            assertThat(result.getDays().get(0).getDayName()).isEqualTo("Dzień 1");
            assertThat(result.getDays().get(1).getDayNumber()).isEqualTo(2);
            assertThat(result.getDays().get(1).getDayName()).isEqualTo("Dzień 2");
            assertThat(result.getDays().get(2).getDayNumber()).isEqualTo(3);
            assertThat(result.getDays().get(2).getDayName()).isEqualTo("Dzień 3");
        }

        @Test
        @DisplayName("Should convert meals correctly with all properties")
        void givenManualDietRequestWithMeals_When_ConvertFromManualDiet_Then_ConvertMealsCorrectly() {
            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    manualDietRequest, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            assertThat(result.getDays()).isNotNull();
            assertThat(result.getDays()).hasSize(1);
            List<DietTemplateMealData> meals = result.getDays().get(0).getMeals();
            assertThat(meals).isNotNull();
            assertThat(meals).hasSize(1);

            DietTemplateMealData meal = meals.get(0);
            assertThat(meal.getName()).isEqualTo("Grilled Chicken");
            assertThat(meal.getMealType()).isEqualTo(MealType.DINNER);
            assertThat(meal.getTime()).isEqualTo("18:00");
            assertThat(meal.getInstructions()).isEqualTo("Grill the chicken for 20 minutes");
            assertThat(meal.getNutritionalValues()).isNotNull();
            assertThat(meal.getNutritionalValues().getCalories()).isEqualTo(300.0);
            assertThat(meal.getPhotos()).isNotNull();
            assertThat(meal.getPhotos()).hasSize(2);
            assertThat(meal.getPhotos()).containsExactly("photo1.jpg", "photo2.jpg");
        }

        @Test
        @DisplayName("Should convert ingredients correctly with all properties")
        void givenManualDietRequestWithIngredients_When_ConvertFromManualDiet_Then_ConvertIngredientsCorrectly() {
            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    manualDietRequest, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            List<DietTemplateIngredient> ingredients = result.getDays().get(0).getMeals().get(0).getIngredients();
            assertThat(ingredients).isNotNull();
            assertThat(ingredients).hasSize(1);

            DietTemplateIngredient ingredient = ingredients.get(0);
            assertThat(ingredient.getName()).isEqualTo("Chicken Breast");
            assertThat(ingredient.getQuantity()).isEqualTo(200.0);
            assertThat(ingredient.getUnit()).isEqualTo("g");
            assertThat(ingredient.getOriginal()).isEqualTo("200g chicken breast");
            assertThat(ingredient.isHasCustomUnit()).isFalse();
            assertThat(ingredient.getCategoryId()).isEqualTo("cat-123");
        }

        @Test
        @DisplayName("Should handle null ingredients list")
        void givenMealWithNullIngredients_When_ConvertFromManualDiet_Then_ReturnEmptyIngredientsList() {
            // Given
            ParsedMeal mealWithoutIngredients = ParsedMeal.builder()
                    .name("Simple Meal")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .ingredients(null)
                    .build();

            ParsedDay dayWithoutIngredients = ParsedDay.builder()
                    .meals(List.of(mealWithoutIngredients))
                    .build();

            ManualDietRequest requestWithoutIngredients = ManualDietRequest.builder()
                    .days(List.of(dayWithoutIngredients))
                    .mealsPerDay(1)
                    .duration(1)
                    .mealTimes(new HashMap<>())
                    .mealTypes(new ArrayList<>())
                    .build();

            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    requestWithoutIngredients, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            List<DietTemplateIngredient> ingredients = result.getDays().get(0).getMeals().get(0).getIngredients();
            assertThat(ingredients).isNotNull();
            assertThat(ingredients).isEmpty();
        }

        @Test
        @DisplayName("Should calculate average nutrition with default values")
        void givenManualDietRequest_When_ConvertFromManualDiet_Then_SetDefaultNutritionValues() {
            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    manualDietRequest, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            DietTemplateNutrition nutrition = result.getTargetNutrition();
            assertThat(nutrition).isNotNull();
            assertThat(nutrition.getTargetCalories()).isEqualTo(2000.0);
            assertThat(nutrition.getTargetProtein()).isEqualTo(150.0);
            assertThat(nutrition.getTargetFat()).isEqualTo(80.0);
            assertThat(nutrition.getTargetCarbs()).isEqualTo(250.0);
            assertThat(nutrition.getCalculationMethod()).isEqualTo("ESTIMATED");
        }

        @Test
        @DisplayName("Should handle empty days list")
        void givenManualDietRequestWithEmptyDays_When_ConvertFromManualDiet_Then_ReturnTemplateWithEmptyDays() {
            // Given
            ManualDietRequest requestWithEmptyDays = ManualDietRequest.builder()
                    .days(new ArrayList<>())
                    .mealsPerDay(0)
                    .duration(0)
                    .mealTimes(new HashMap<>())
                    .mealTypes(new ArrayList<>())
                    .build();

            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    requestWithEmptyDays, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDays()).isNotNull();
            assertThat(result.getDays()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple meals per day")
        void givenManualDietRequestWithMultipleMeals_When_ConvertFromManualDiet_Then_ConvertAllMeals() {
            // Given
            ParsedMeal breakfast = ParsedMeal.builder()
                    .name("Breakfast")
                    .mealType(MealType.BREAKFAST)
                    .time("08:00")
                    .ingredients(new ArrayList<>())
                    .build();

            ParsedMeal lunch = ParsedMeal.builder()
                    .name("Lunch")
                    .mealType(MealType.LUNCH)
                    .time("13:00")
                    .ingredients(new ArrayList<>())
                    .build();

            ParsedMeal dinner = ParsedMeal.builder()
                    .name("Dinner")
                    .mealType(MealType.DINNER)
                    .time("18:00")
                    .ingredients(new ArrayList<>())
                    .build();

            ParsedDay dayWithMultipleMeals = ParsedDay.builder()
                    .meals(List.of(breakfast, lunch, dinner))
                    .build();

            ManualDietRequest requestWithMultipleMeals = ManualDietRequest.builder()
                    .days(List.of(dayWithMultipleMeals))
                    .mealsPerDay(3)
                    .duration(1)
                    .mealTimes(new HashMap<>())
                    .mealTypes(new ArrayList<>())
                    .build();

            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    requestWithMultipleMeals, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            assertThat(result.getDays()).hasSize(1);
            List<DietTemplateMealData> meals = result.getDays().get(0).getMeals();
            assertThat(meals).hasSize(3);
            assertThat(meals.get(0).getName()).isEqualTo("Breakfast");
            assertThat(meals.get(1).getName()).isEqualTo("Lunch");
            assertThat(meals.get(2).getName()).isEqualTo("Dinner");
        }

        @Test
        @DisplayName("Should handle multiple ingredients per meal")
        void givenMealWithMultipleIngredients_When_ConvertFromManualDiet_Then_ConvertAllIngredients() {
            // Given
            ParsedProduct product1 = ParsedProduct.builder()
                    .name("Chicken")
                    .quantity(200.0)
                    .unit("g")
                    .original("200g chicken")
                    .hasCustomUnit(false)
                    .categoryId("cat-1")
                    .build();

            ParsedProduct product2 = ParsedProduct.builder()
                    .name("Rice")
                    .quantity(150.0)
                    .unit("g")
                    .original("150g rice")
                    .hasCustomUnit(false)
                    .categoryId("cat-2")
                    .build();

            ParsedProduct product3 = ParsedProduct.builder()
                    .name("Broccoli")
                    .quantity(100.0)
                    .unit("g")
                    .original("100g broccoli")
                    .hasCustomUnit(false)
                    .categoryId("cat-3")
                    .build();

            ParsedMeal mealWithMultipleIngredients = ParsedMeal.builder()
                    .name("Complete Meal")
                    .mealType(MealType.DINNER)
                    .time("18:00")
                    .ingredients(List.of(product1, product2, product3))
                    .build();

            ParsedDay dayWithMultipleIngredients = ParsedDay.builder()
                    .meals(List.of(mealWithMultipleIngredients))
                    .build();

            ManualDietRequest requestWithMultipleIngredients = ManualDietRequest.builder()
                    .days(List.of(dayWithMultipleIngredients))
                    .mealsPerDay(1)
                    .duration(1)
                    .mealTimes(new HashMap<>())
                    .mealTypes(new ArrayList<>())
                    .build();

            // When
            DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                    requestWithMultipleIngredients, "Template", "Desc", DietTemplateCategory.CUSTOM, "user-123");

            // Then
            List<DietTemplateIngredient> ingredients = result.getDays().get(0).getMeals().get(0).getIngredients();
            assertThat(ingredients).hasSize(3);
            assertThat(ingredients.get(0).getName()).isEqualTo("Chicken");
            assertThat(ingredients.get(1).getName()).isEqualTo("Rice");
            assertThat(ingredients.get(2).getName()).isEqualTo("Broccoli");
        }

        @Test
        @DisplayName("Should handle all DietTemplateCategory values")
        void givenDifferentCategories_When_ConvertFromManualDiet_Then_AcceptAllCategories() {
            // Given & When & Then
            for (DietTemplateCategory category : DietTemplateCategory.values()) {
                DietTemplate result = dietTemplateConverter.convertFromManualDiet(
                        manualDietRequest, "Template", "Desc", category, "user-123");
                assertThat(result.getCategory()).isEqualTo(category);
            }
        }
    }
}
