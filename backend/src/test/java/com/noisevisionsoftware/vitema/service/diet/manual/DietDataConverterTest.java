package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class DietDataConverterTest {

    @InjectMocks
    private DietDataConverter dietDataConverter;

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
                .categoryId("cat-123")
                .hasCustomUnit(false)
                .build();

        ParsedProduct parsedProduct2 = ParsedProduct.builder()
                .name("Rice")
                .quantity(100.0)
                .unit("g")
                .original("100g rice")
                .categoryId("cat-456")
                .hasCustomUnit(false)
                .build();

        parsedMeal = ParsedMeal.builder()
                .name("Grilled Chicken")
                .instructions("Grill the chicken")
                .ingredients(List.of(parsedProduct, parsedProduct2))
                .build();

        parsedDay = ParsedDay.builder()
                .meals(List.of(parsedMeal))
                .build();

        Map<String, String> mealTimes = new HashMap<>();
        mealTimes.put("BREAKFAST", "08:00");
        mealTimes.put("LUNCH", "13:00");

        manualDietRequest = ManualDietRequest.builder()
                .userId("user-123")
                .days(List.of(parsedDay))
                .mealsPerDay(3)
                .duration(7)
                .startDate("2024-01-01")
                .mealTimes(mealTimes)
                .build();
    }

    @Nested
    @DisplayName("convertToParsedDietData")
    class ConvertToParsedDietDataTests {

        @Test
        @DisplayName("Should convert ManualDietRequest to ParsedDietData successfully")
        void givenValidRequest_When_ConvertToParsedDietData_Then_ReturnParsedDietData() {
            // When
            ParsedDietData result = dietDataConverter.convertToParsedDietData(manualDietRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDays()).isEqualTo(manualDietRequest.getDays());
            assertThat(result.getMealsPerDay()).isEqualTo(manualDietRequest.getMealsPerDay());
            assertThat(result.getDuration()).isEqualTo(manualDietRequest.getDuration());
            assertThat(result.getMealTimes()).isEqualTo(manualDietRequest.getMealTimes());
            assertThat(result.getStartDate()).isNotNull();
            assertThat(result.getShoppingList()).isNotNull();
            assertThat(result.getCategorizedProducts()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void givenNullRequest_When_ConvertToParsedDietData_Then_ThrowException() {
            // When & Then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> dietDataConverter.convertToParsedDietData(null))
                    .withMessageContaining("ManualDietRequest nie może być null");
        }

        @Test
        @DisplayName("Should handle invalid date format gracefully")
        void givenInvalidDateFormat_When_ConvertToParsedDietData_Then_UseFallbackDate() {
            // Given
            ManualDietRequest requestWithInvalidDate = ManualDietRequest.builder()
                    .userId("user-123")
                    .days(List.of(parsedDay))
                    .mealsPerDay(3)
                    .duration(7)
                    .startDate("invalid-date")
                    .build();

            // When
            ParsedDietData result = dietDataConverter.convertToParsedDietData(requestWithInvalidDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStartDate()).isNotNull();
        }

        @Test
        @DisplayName("Should handle empty days list")
        void givenEmptyDaysList_When_ConvertToParsedDietData_Then_ReturnEmptyShoppingList() {
            // Given
            ManualDietRequest requestWithEmptyDays = ManualDietRequest.builder()
                    .userId("user-123")
                    .days(new ArrayList<>())
                    .mealsPerDay(3)
                    .duration(7)
                    .startDate("2024-01-01")
                    .build();

            // When
            ParsedDietData result = dietDataConverter.convertToParsedDietData(requestWithEmptyDays);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getShoppingList()).isEmpty();
            assertThat(result.getCategorizedProducts()).isEmpty();
        }

        @Test
        @DisplayName("Should handle days with null meals")
        void givenDaysWithNullMeals_When_ConvertToParsedDietData_Then_HandleGracefully() {
            // Given
            ParsedDay dayWithNullMeals = ParsedDay.builder()
                    .meals(null)
                    .build();
            ManualDietRequest request = ManualDietRequest.builder()
                    .userId("user-123")
                    .days(List.of(dayWithNullMeals))
                    .mealsPerDay(3)
                    .duration(7)
                    .startDate("2024-01-01")
                    .build();

            // When
            ParsedDietData result = dietDataConverter.convertToParsedDietData(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getShoppingList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateShoppingList")
    class GenerateShoppingListTests {

        @Test
        @DisplayName("Should generate shopping list from ingredients")
        void givenRequestWithIngredients_When_GenerateShoppingList_Then_CreateShoppingList() {
            // Given
            ParsedDietData parsedData = new ParsedDietData();

            // When
            dietDataConverter.generateShoppingList(parsedData, manualDietRequest);

            // Then
            assertThat(parsedData.getShoppingList()).isNotEmpty();
            assertThat(parsedData.getShoppingList()).contains("200g chicken breast", "100g rice");
            assertThat(parsedData.getCategorizedProducts()).isNotEmpty();
            assertThat(parsedData.getCategorizedProducts()).containsKey("cat-123");
            assertThat(parsedData.getCategorizedProducts()).containsKey("cat-456");
        }

        @Test
        @DisplayName("Should handle duplicate ingredients")
        void givenDuplicateIngredients_When_GenerateShoppingList_Then_AggregateQuantities() {
            // Given
            ParsedProduct duplicateProduct = ParsedProduct.builder()
                    .name("Chicken Breast")
                    .quantity(150.0)
                    .unit("g")
                    .original("200g chicken breast")
                    .categoryId("cat-123")
                    .build();

            ParsedMeal mealWithDuplicates = ParsedMeal.builder()
                    .name("Meal")
                    .ingredients(List.of(parsedProduct, duplicateProduct))
                    .build();

            ParsedDay dayWithDuplicates = ParsedDay.builder()
                    .meals(List.of(mealWithDuplicates))
                    .build();

            ManualDietRequest request = ManualDietRequest.builder()
                    .days(List.of(dayWithDuplicates))
                    .mealsPerDay(1)
                    .duration(1)
                    .startDate("2024-01-01")
                    .build();

            ParsedDietData parsedData = new ParsedDietData();

            // When
            dietDataConverter.generateShoppingList(parsedData, request);

            // Then
            assertThat(parsedData.getShoppingList()).hasSize(1);
            assertThat(parsedData.getShoppingList()).contains("200g chicken breast");
        }

        @Test
        @DisplayName("Should ignore ingredients with empty original text")
        void givenIngredientsWithEmptyOriginal_When_GenerateShoppingList_Then_IgnoreThem() {
            // Given
            ParsedProduct productWithEmptyOriginal = ParsedProduct.builder()
                    .name("Product")
                    .quantity(100.0)
                    .unit("g")
                    .original("")
                    .build();

            ParsedMeal meal = ParsedMeal.builder()
                    .name("Meal")
                    .ingredients(List.of(productWithEmptyOriginal))
                    .build();

            ParsedDay day = ParsedDay.builder()
                    .meals(List.of(meal))
                    .build();

            ManualDietRequest request = ManualDietRequest.builder()
                    .days(List.of(day))
                    .mealsPerDay(1)
                    .duration(1)
                    .startDate("2024-01-01")
                    .build();

            ParsedDietData parsedData = new ParsedDietData();

            // When
            dietDataConverter.generateShoppingList(parsedData, request);

            // Then
            assertThat(parsedData.getShoppingList()).isEmpty();
        }

        @Test
        @DisplayName("Should sort shopping list alphabetically")
        void givenMultipleIngredients_When_GenerateShoppingList_Then_SortAlphabetically() {
            // Given
            ParsedProduct productA = ParsedProduct.builder()
                    .name("Apple")
                    .quantity(1.0)
                    .unit("kg")
                    .original("1kg apple")
                    .build();

            ParsedProduct productZ = ParsedProduct.builder()
                    .name("Zucchini")
                    .quantity(1.0)
                    .unit("kg")
                    .original("1kg zucchini")
                    .build();

            ParsedProduct productM = ParsedProduct.builder()
                    .name("Mango")
                    .quantity(1.0)
                    .unit("kg")
                    .original("1kg mango")
                    .build();

            ParsedMeal meal = ParsedMeal.builder()
                    .name("Meal")
                    .ingredients(List.of(productZ, productA, productM))
                    .build();

            ParsedDay day = ParsedDay.builder()
                    .meals(List.of(meal))
                    .build();

            ManualDietRequest request = ManualDietRequest.builder()
                    .days(List.of(day))
                    .mealsPerDay(1)
                    .duration(1)
                    .startDate("2024-01-01")
                    .build();

            ParsedDietData parsedData = new ParsedDietData();

            // When
            dietDataConverter.generateShoppingList(parsedData, request);

            // Then
            assertThat(parsedData.getShoppingList()).hasSize(3);
            assertThat(parsedData.getShoppingList().get(0)).isEqualTo("1kg apple");
            assertThat(parsedData.getShoppingList().get(1)).isEqualTo("1kg mango");
            assertThat(parsedData.getShoppingList().get(2)).isEqualTo("1kg zucchini");
        }
    }

    @Nested
    @DisplayName("convertToManualDietRequest")
    class ConvertToManualDietRequestTests {

        @Test
        @DisplayName("Should convert ParsedDietData to ManualDietRequest successfully")
        void givenValidParsedDietData_When_ConvertToManualDietRequest_Then_ReturnManualDietRequest() {
            // Given
            ParsedDietData parsedData = dietDataConverter.convertToParsedDietData(manualDietRequest);
            String userId = "user-456";

            // When
            ManualDietRequest result = dietDataConverter.convertToManualDietRequest(parsedData, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getDays()).isEqualTo(parsedData.getDays());
            assertThat(result.getMealsPerDay()).isEqualTo(parsedData.getMealsPerDay());
            assertThat(result.getDuration()).isEqualTo(parsedData.getDuration());
            assertThat(result.getMealTimes()).isEqualTo(parsedData.getMealTimes());
            assertThat(result.getStartDate()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when ParsedDietData is null")
        void givenNullParsedDietData_When_ConvertToManualDietRequest_Then_ThrowException() {
            // When & Then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> dietDataConverter.convertToManualDietRequest(null, "user-123"))
                    .withMessageContaining("ParsedDietData nie może być null");
        }

        @Test
        @DisplayName("Should handle null timestamp in startDate")
        void givenNullStartDate_When_ConvertToManualDietRequest_Then_UseCurrentDate() {
            // Given
            ParsedDietData parsedData = new ParsedDietData();
            parsedData.setDays(new ArrayList<>());
            parsedData.setMealsPerDay(3);
            parsedData.setDuration(7);
            parsedData.setStartDate(null);

            // When
            ManualDietRequest result = dietDataConverter.convertToManualDietRequest(parsedData, "user-123");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStartDate()).isNotNull();
            assertThat(result.getStartDate()).isEqualTo(LocalDate.now().toString());
        }
    }

    @Nested
    @DisplayName("createDietSummary")
    class CreateDietSummaryTests {

        @Test
        @DisplayName("Should create diet summary with correct statistics")
        void givenValidParsedDietData_When_CreateDietSummary_Then_ReturnSummary() {
            // Given
            ParsedDietData parsedData = dietDataConverter.convertToParsedDietData(manualDietRequest);

            // When
            Map<String, Object> summary = dietDataConverter.createDietSummary(parsedData);

            // Then
            assertThat(summary).isNotNull();
            assertThat(summary.get("totalDays")).isEqualTo(1);
            assertThat(summary.get("totalMeals")).isEqualTo(1);
            assertThat(summary.get("totalIngredients")).isEqualTo(2);
            assertThat(summary.get("daysWithoutMeals")).isEqualTo(0);
            assertThat(summary.get("shoppingListSize")).isEqualTo(2);
            assertThat(summary.get("categoriesCount")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle empty ParsedDietData")
        void givenEmptyParsedDietData_When_CreateDietSummary_Then_ReturnEmptySummary() {
            // Given
            ParsedDietData parsedData = new ParsedDietData();
            parsedData.setDays(new ArrayList<>());
            parsedData.setShoppingList(new ArrayList<>());
            parsedData.setCategorizedProducts(new HashMap<>());

            // When
            Map<String, Object> summary = dietDataConverter.createDietSummary(parsedData);

            // Then
            assertThat(summary).isNotNull();
            assertThat(summary.get("totalDays")).isEqualTo(0);
            assertThat(summary.get("totalMeals")).isEqualTo(0);
            assertThat(summary.get("totalIngredients")).isEqualTo(0);
            assertThat(summary.get("daysWithoutMeals")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should count days without meals")
        void givenDaysWithoutMeals_When_CreateDietSummary_Then_CountCorrectly() {
            // Given
            ParsedDay dayWithMeals = ParsedDay.builder()
                    .meals(List.of(parsedMeal))
                    .build();

            ParsedDay dayWithoutMeals = ParsedDay.builder()
                    .meals(new ArrayList<>())
                    .build();

            ParsedDay dayWithNullMeals = ParsedDay.builder()
                    .meals(null)
                    .build();

            ParsedDietData parsedData = new ParsedDietData();
            parsedData.setDays(List.of(dayWithMeals, dayWithoutMeals, dayWithNullMeals));

            // When
            Map<String, Object> summary = dietDataConverter.createDietSummary(parsedData);

            // Then
            assertThat(summary.get("daysWithoutMeals")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should calculate average meals per day correctly")
        void givenMultipleDays_When_CreateDietSummary_Then_CalculateAverage() {
            // Given
            ParsedDay day1 = ParsedDay.builder()
                    .meals(List.of(parsedMeal, parsedMeal))
                    .build();

            ParsedDay day2 = ParsedDay.builder()
                    .meals(List.of(parsedMeal))
                    .build();

            ParsedDietData parsedData = new ParsedDietData();
            parsedData.setDays(List.of(day1, day2));

            // When
            Map<String, Object> summary = dietDataConverter.createDietSummary(parsedData);

            // Then
            assertThat(summary.get("totalMeals")).isEqualTo(3);
            assertThat(summary.get("averageMealsPerDay")).isEqualTo(1.5);
        }
    }

    @Nested
    @DisplayName("canConvert")
    class CanConvertTests {

        @Test
        @DisplayName("Should return true for valid request")
        void givenValidRequest_When_CanConvert_Then_ReturnTrue() {
            // When
            boolean result = dietDataConverter.canConvert(manualDietRequest);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null request")
        void givenNullRequest_When_CanConvert_Then_ReturnFalse() {
            // When
            boolean result = dietDataConverter.canConvert(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for request with empty days")
        void givenRequestWithEmptyDays_When_CanConvert_Then_ReturnFalse() {
            // Given
            ManualDietRequest request = ManualDietRequest.builder()
                    .days(new ArrayList<>())
                    .mealsPerDay(3)
                    .startDate("2024-01-01")
                    .build();

            // When
            boolean result = dietDataConverter.canConvert(request);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for request with null days")
        void givenRequestWithNullDays_When_CanConvert_Then_ReturnFalse() {
            // Given
            ManualDietRequest request = ManualDietRequest.builder()
                    .days(null)
                    .mealsPerDay(3)
                    .startDate("2024-01-01")
                    .build();

            // When
            boolean result = dietDataConverter.canConvert(request);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for request with empty startDate")
        void givenRequestWithEmptyStartDate_When_CanConvert_Then_ReturnFalse() {
            // Given
            ManualDietRequest request = ManualDietRequest.builder()
                    .days(List.of(parsedDay))
                    .mealsPerDay(3)
                    .startDate("")
                    .build();

            // When
            boolean result = dietDataConverter.canConvert(request);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for request with invalid mealsPerDay")
        void givenRequestWithInvalidMealsPerDay_When_CanConvert_Then_ReturnFalse() {
            // Given
            ManualDietRequest request = ManualDietRequest.builder()
                    .days(List.of(parsedDay))
                    .mealsPerDay(0)
                    .startDate("2024-01-01")
                    .build();

            // When
            boolean result = dietDataConverter.canConvert(request);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("createEmptyDietStructure")
    class CreateEmptyDietStructureTests {

        @Test
        @DisplayName("Should create empty diet structure with correct parameters")
        void givenValidParameters_When_CreateEmptyDietStructure_Then_ReturnStructure() {
            // Given
            int days = 7;
            int mealsPerDay = 3;
            String startDate = "2024-01-01";

            // When
            ParsedDietData result = dietDataConverter.createEmptyDietStructure(days, mealsPerDay, startDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDays()).hasSize(days);
            assertThat(result.getMealsPerDay()).isEqualTo(mealsPerDay);
            assertThat(result.getDuration()).isEqualTo(days);
            assertThat(result.getStartDate()).isNotNull();
            assertThat(result.getShoppingList()).isEmpty();
            assertThat(result.getCategorizedProducts()).isEmpty();

            // Verify structure
            for (ParsedDay day : result.getDays()) {
                assertThat(day.getMeals()).hasSize(mealsPerDay);
                for (ParsedMeal meal : day.getMeals()) {
                    assertThat(meal.getName()).isNotNull();
                    assertThat(meal.getIngredients()).isEmpty();
                }
            }
        }

        @Test
        @DisplayName("Should handle invalid date format")
        void givenInvalidDateFormat_When_CreateEmptyDietStructure_Then_UseFallbackDate() {
            // Given
            int days = 3;
            int mealsPerDay = 2;
            String invalidDate = "invalid-date";

            // When
            ParsedDietData result = dietDataConverter.createEmptyDietStructure(days, mealsPerDay, invalidDate);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStartDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("cloneDietWithModifications")
    class CloneDietWithModificationsTests {

        @Test
        @DisplayName("Should clone diet with all modifications")
        void givenModifications_When_CloneDietWithModifications_Then_ApplyModifications() {
            // Given
            ParsedDietData original = dietDataConverter.convertToParsedDietData(manualDietRequest);
            Integer newDuration = 14;
            Integer newMealsPerDay = 4;
            String newStartDate = "2024-02-01";

            // When
            ParsedDietData cloned = dietDataConverter.cloneDietWithModifications(
                    original, newDuration, newMealsPerDay, newStartDate);

            // Then
            assertThat(cloned).isNotNull();
            assertThat(cloned.getDuration()).isEqualTo(newDuration);
            assertThat(cloned.getMealsPerDay()).isEqualTo(newMealsPerDay);
            assertThat(cloned.getMealTimes()).isEqualTo(original.getMealTimes());
            assertThat(cloned.getDays()).hasSize(newDuration);
        }

        @Test
        @DisplayName("Should clone diet without modifications")
        void givenNullModifications_When_CloneDietWithModifications_Then_KeepOriginalValues() {
            // Given
            ParsedDietData original = dietDataConverter.convertToParsedDietData(manualDietRequest);

            // When
            ParsedDietData cloned = dietDataConverter.cloneDietWithModifications(
                    original, null, null, null);

            // Then
            assertThat(cloned).isNotNull();
            assertThat(cloned.getDuration()).isEqualTo(original.getDuration());
            assertThat(cloned.getMealsPerDay()).isEqualTo(original.getMealsPerDay());
            assertThat(cloned.getStartDate()).isEqualTo(original.getStartDate());
        }

        @Test
        @DisplayName("Should throw exception when original is null")
        void givenNullOriginal_When_CloneDietWithModifications_Then_ThrowException() {
            // When & Then
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> dietDataConverter.cloneDietWithModifications(
                            null, 7, 3, "2024-01-01"))
                    .withMessageContaining("Oryginalna dieta nie może być null");
        }

        @Test
        @DisplayName("Should clone days correctly when extending duration")
        void givenExtendedDuration_When_CloneDietWithModifications_Then_CreateNewDays() {
            // Given
            ParsedDietData original = dietDataConverter.convertToParsedDietData(manualDietRequest);
            int newDuration = 10;

            // When
            ParsedDietData cloned = dietDataConverter.cloneDietWithModifications(
                    original, newDuration, null, null);

            // Then
            assertThat(cloned.getDays()).hasSize(newDuration);
            // First day should be cloned
            assertThat(cloned.getDays().getFirst().getMeals()).isNotEmpty();
            // Additional days should be empty
            for (int i = original.getDays().size(); i < newDuration; i++) {
                assertThat(cloned.getDays().get(i).getMeals()).hasSize(original.getMealsPerDay());
            }
        }

        @Test
        @DisplayName("Should clone days correctly when reducing duration")
        void givenReducedDuration_When_CloneDietWithModifications_Then_TruncateDays() {
            // Given
            ParsedDietData original = dietDataConverter.convertToParsedDietData(manualDietRequest);
            int newDuration = 1;

            // When
            ParsedDietData cloned = dietDataConverter.cloneDietWithModifications(
                    original, newDuration, null, null);

            // Then
            assertThat(cloned.getDays()).hasSize(newDuration);
        }

        @Test
        @DisplayName("Should clone meals correctly when changing mealsPerDay")
        void givenChangedMealsPerDay_When_CloneDietWithModifications_Then_AdjustMeals() {
            // Given
            ParsedDietData original = dietDataConverter.convertToParsedDietData(manualDietRequest);
            Integer newMealsPerDay = 5;

            // When
            ParsedDietData cloned = dietDataConverter.cloneDietWithModifications(
                    original, null, newMealsPerDay, null);

            // Then
            assertThat(cloned.getMealsPerDay()).isEqualTo(newMealsPerDay);
            for (ParsedDay day : cloned.getDays()) {
                assertThat(day.getMeals()).hasSize(newMealsPerDay);
            }
        }

        @Test
        @DisplayName("Should perform deep copy of ingredients")
        void givenOriginalWithIngredients_When_CloneDietWithModifications_Then_CreateDeepCopy() {
            // Given
            ParsedDietData original = dietDataConverter.convertToParsedDietData(manualDietRequest);

            // When
            ParsedDietData cloned = dietDataConverter.cloneDietWithModifications(
                    original, null, null, null);

            // Then
            assertThat(cloned.getDays()).isNotSameAs(original.getDays());
            assertThat(cloned.getShoppingList()).isNotSameAs(original.getShoppingList());
            assertThat(cloned.getCategorizedProducts()).isNotSameAs(original.getCategorizedProducts());
        }
    }
}
