package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.service.external.OpenFoodFactsService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientManagementServiceTest {

    @Mock
    private OpenFoodFactsService openFoodFactsService;

    @Mock
    private ProductCategorizationService categorizationService;

    @InjectMocks
    private IngredientManagementService ingredientManagementService;

    private ParsedProduct validIngredient;

    @BeforeEach
    void setUp() {
        validIngredient = ParsedProduct.builder()
                .id("ingredient-123")
                .name("Mleko 3,2%")
                .original("Mleko 3,2%")
                .quantity(200.0)
                .unit("ml")
                .categoryId("nabiał")
                .hasCustomUnit(false)
                .build();
    }

    @Nested
    @DisplayName("searchIngredients")
    class SearchIngredientsTests {

        @Test
        @DisplayName("Should return external results when sufficient")
        void givenSufficientExternalResults_When_SearchIngredients_Then_ReturnExternalResults() {
            // Given
            String query = "mleko";
            int limit = 5;
            List<ParsedProduct> externalResults = new ArrayList<>(List.of(
                    createParsedProduct("Mleko 3,2%"),
                    createParsedProduct("Mleko pełne"),
                    createParsedProduct("Mleko odtłuszczone"),
                    createParsedProduct("Mleko skondensowane"),
                    createParsedProduct("Mleko kokosowe")
            ));

            when(openFoodFactsService.searchIngredients(query, limit)).thenReturn(externalResults);

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).hasSize(5);
            assertThat(result).containsAll(externalResults);
            verify(openFoodFactsService).searchIngredients(query, limit);
        }

        @Test
        @DisplayName("Should supplement with local results when external results insufficient")
        void givenInsufficientExternalResults_When_SearchIngredients_Then_SupplementWithLocal() {
            // Given
            String query = "mleko";
            int limit = 5;
            List<ParsedProduct> externalResults = new ArrayList<>(List.of(
                    createParsedProduct("Mleko 3,2%")
            ));

            when(openFoodFactsService.searchIngredients(query, limit)).thenReturn(externalResults);
            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("nabiał");

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
            assertThat(result.getFirst().getName()).isEqualTo("Mleko 3,2%");
            verify(openFoodFactsService).searchIngredients(query, limit);
        }

        @Test
        @DisplayName("Should return local results when external service throws exception")
        void givenExternalServiceException_When_SearchIngredients_Then_ReturnLocalResults() {
            // Given
            String query = "mleko";
            int limit = 5;

            when(openFoodFactsService.searchIngredients(query, limit))
                    .thenThrow(new RuntimeException("External service error"));
            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("nabiał");

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            verify(openFoodFactsService).searchIngredients(query, limit);
        }

        @Test
        @DisplayName("Should add categories to external results without category")
        void givenExternalResultsWithoutCategory_When_SearchIngredients_Then_AddCategories() {
            // Given
            String query = "mleko";
            int limit = 5;
            ParsedProduct product1 = createParsedProduct("Mleko 1");
            ParsedProduct product2 = createParsedProduct("Mleko 2");
            ParsedProduct product3 = createParsedProduct("Mleko 3");
            ParsedProduct product4 = createParsedProduct("Mleko 4");
            ParsedProduct product5 = createParsedProduct("Mleko 5");
            product1.setCategoryId(null);
            product2.setCategoryId(null);
            product3.setCategoryId(null);
            product4.setCategoryId(null);
            product5.setCategoryId(null);

            List<ParsedProduct> externalResults = new ArrayList<>(List.of(product1, product2, product3, product4, product5));

            when(openFoodFactsService.searchIngredients(query, limit)).thenReturn(externalResults);
            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("nabiał");

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).hasSize(5);
            assertThat(result.get(0).getCategoryId()).isEqualTo("nabiał");
            assertThat(result.get(1).getCategoryId()).isEqualTo("nabiał");
            assertThat(result.get(2).getCategoryId()).isEqualTo("nabiał");
            assertThat(result.get(3).getCategoryId()).isEqualTo("nabiał");
            assertThat(result.get(4).getCategoryId()).isEqualTo("nabiał");
            verify(categorizationService, times(5)).suggestCategory(any(ParsedProduct.class));
        }

        @Test
        @DisplayName("Should handle empty query")
        void givenEmptyQuery_When_SearchIngredients_Then_ReturnResults() {
            // Given
            String query = "";
            int limit = 5;

            when(openFoodFactsService.searchIngredients(query, limit)).thenReturn(new ArrayList<>());
            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("category");

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).isNotNull();
            verify(openFoodFactsService).searchIngredients(query, limit);
        }
    }

    @Nested
    @DisplayName("createIngredient")
    class CreateIngredientTests {

        @Test
        @DisplayName("Should generate ID when ingredient has no ID")
        void givenIngredientWithoutId_When_CreateIngredient_Then_GenerateId() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Nowy składnik")
                    .original("Nowy składnik")
                    .quantity(100.0)
                    .unit("g")
                    .build();
            ingredient.setId(null);

            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("category-123");

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId()).isNotEmpty();
            verify(categorizationService).suggestCategory(any(ParsedProduct.class));
        }

        @Test
        @DisplayName("Should keep existing ID when ingredient has ID")
        void givenIngredientWithId_When_CreateIngredient_Then_KeepExistingId() {
            // Given
            String existingId = "existing-id-123";
            ParsedProduct ingredient = ParsedProduct.builder()
                    .id(existingId)
                    .name("Składnik")
                    .original("Składnik")
                    .quantity(100.0)
                    .unit("g")
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result.getId()).isEqualTo(existingId);
        }

        @Test
        @DisplayName("Should add category when ingredient has no category")
        void givenIngredientWithoutCategory_When_CreateIngredient_Then_AddCategory() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Nowy składnik")
                    .original("Nowy składnik")
                    .quantity(100.0)
                    .unit("g")
                    .build();
            ingredient.setId(null);
            ingredient.setCategoryId(null);

            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("category-123");

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result.getCategoryId()).isEqualTo("category-123");
            verify(categorizationService).suggestCategory(any(ParsedProduct.class));
        }

        @Test
        @DisplayName("Should keep existing category when ingredient has category")
        void givenIngredientWithCategory_When_CreateIngredient_Then_KeepExistingCategory() {
            // Given
            String existingCategory = "existing-category";
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Składnik")
                    .original("Składnik")
                    .quantity(100.0)
                    .unit("g")
                    .categoryId(existingCategory)
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result.getCategoryId()).isEqualTo(existingCategory);
            verify(categorizationService, never()).suggestCategory(any(ParsedProduct.class));
        }

        @Test
        @DisplayName("Should handle categorization exception gracefully")
        void givenCategorizationException_When_CreateIngredient_Then_HandleGracefully() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Nowy składnik")
                    .original("Nowy składnik")
                    .quantity(100.0)
                    .unit("g")
                    .build();
            ingredient.setId(null);
            ingredient.setCategoryId(null);

            when(categorizationService.suggestCategory(any(ParsedProduct.class)))
                    .thenThrow(new RuntimeException("Categorization error"));

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getCategoryId()).isNull();
            verify(categorizationService).suggestCategory(any(ParsedProduct.class));
        }
    }

    @Nested
    @DisplayName("createBasicIngredient")
    class CreateBasicIngredientTests {

        @Test
        @DisplayName("Should create ingredient with provided parameters")
        void givenNameUnitQuantity_When_CreateBasicIngredient_Then_CreateIngredient() {
            // Given
            String name = "Mleko";
            String unit = "ml";
            Double quantity = 200.0;

            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("nabiał");

            // When
            ParsedProduct result = ingredientManagementService.createBasicIngredient(name, unit, quantity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getName()).isEqualTo(name);
            assertThat(result.getOriginal()).isEqualTo(name);
            assertThat(result.getUnit()).isEqualTo(unit);
            assertThat(result.getQuantity()).isEqualTo(quantity);
            assertThat(result.isHasCustomUnit()).isFalse();
            assertThat(result.getCategoryId()).isEqualTo("nabiał");
            verify(categorizationService).suggestCategory(any(ParsedProduct.class));
        }

        @Test
        @DisplayName("Should use default unit when unit is null")
        void givenNullUnit_When_CreateBasicIngredient_Then_UseDefaultUnit() {
            // Given
            String name = "Jajka";
            Double quantity = 2.0;

            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("nabiał");

            // When
            ParsedProduct result = ingredientManagementService.createBasicIngredient(name, null, quantity);

            // Then
            assertThat(result.getUnit()).isEqualTo("szt");
        }

        @Test
        @DisplayName("Should use default quantity when quantity is null")
        void givenNullQuantity_When_CreateBasicIngredient_Then_UseDefaultQuantity() {
            // Given
            String name = "Mleko";
            String unit = "ml";

            when(categorizationService.suggestCategory(any(ParsedProduct.class))).thenReturn("nabiał");

            // When
            ParsedProduct result = ingredientManagementService.createBasicIngredient(name, unit, null);

            // Then
            assertThat(result.getQuantity()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("validateIngredient")
    class ValidateIngredientTests {

        @Test
        @DisplayName("Should return true for valid ingredient")
        void givenValidIngredient_When_ValidateIngredient_Then_ReturnTrue() {
            // When
            boolean result = ingredientManagementService.validateIngredient(validIngredient);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null ingredient")
        void givenNullIngredient_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient without name")
        void givenIngredientWithoutName_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .quantity(100.0)
                    .unit("g")
                    .build();
            ingredient.setName(null);

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient with empty name")
        void givenIngredientWithEmptyName_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("   ")
                    .quantity(100.0)
                    .unit("g")
                    .build();

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient with null quantity")
        void givenIngredientWithNullQuantity_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Składnik")
                    .quantity(null)
                    .unit("g")
                    .build();

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient with zero quantity")
        void givenIngredientWithZeroQuantity_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Składnik")
                    .quantity(0.0)
                    .unit("g")
                    .build();

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient with negative quantity")
        void givenIngredientWithNegativeQuantity_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Składnik")
                    .quantity(-10.0)
                    .unit("g")
                    .build();

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient without unit")
        void givenIngredientWithoutUnit_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Składnik")
                    .quantity(100.0)
                    .unit(null)
                    .build();

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for ingredient with empty unit")
        void givenIngredientWithEmptyUnit_When_ValidateIngredient_Then_ReturnFalse() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Składnik")
                    .quantity(100.0)
                    .unit("   ")
                    .build();

            // When
            boolean result = ingredientManagementService.validateIngredient(ingredient);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("normalizeIngredient")
    class NormalizeIngredientTests {

        @Test
        @DisplayName("Should normalize ingredient name")
        void givenIngredientWithWhitespace_When_NormalizeIngredient_Then_TrimName() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("  Mleko  ")
                    .quantity(200.0)
                    .unit("ml")
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.normalizeIngredient(ingredient);

            // Then
            assertThat(result.getName()).isEqualTo("Mleko");
        }

        @Test
        @DisplayName("Should normalize unit")
        void givenIngredientWithUnitVariation_When_NormalizeIngredient_Then_NormalizeUnit() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Mleko")
                    .quantity(200.0)
                    .unit("sztuka")
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.normalizeIngredient(ingredient);

            // Then
            assertThat(result.getUnit()).isEqualTo("szt");
        }

        @Test
        @DisplayName("Should set default quantity when quantity is null")
        void givenIngredientWithNullQuantity_When_NormalizeIngredient_Then_SetDefaultQuantity() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Mleko")
                    .quantity(null)
                    .unit("ml")
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.normalizeIngredient(ingredient);

            // Then
            assertThat(result.getQuantity()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should set default quantity when quantity is zero")
        void givenIngredientWithZeroQuantity_When_NormalizeIngredient_Then_SetDefaultQuantity() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("Mleko")
                    .quantity(0.0)
                    .unit("ml")
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.normalizeIngredient(ingredient);

            // Then
            assertThat(result.getQuantity()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should return null for null ingredient")
        void givenNullIngredient_When_NormalizeIngredient_Then_ReturnNull() {
            // When
            ParsedProduct result = ingredientManagementService.normalizeIngredient(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should normalize various unit formats")
        void givenIngredientWithVariousUnits_When_NormalizeIngredient_Then_NormalizeCorrectly() {
            // Test various unit normalizations
            String[][] unitTests = {
                    {"gram", "g"},
                    {"kilogram", "kg"},
                    {"mililitr", "ml"},
                    {"litr", "l"},
                    {"łyżka", "łyżka"},
                    {"łyżeczka", "łyżeczka"},
                    {"szklanka", "szklanka"}
            };

            for (String[] test : unitTests) {
                ParsedProduct ingredient = ParsedProduct.builder()
                        .name("Składnik")
                        .quantity(1.0)
                        .unit(test[0])
                        .build();

                ParsedProduct result = ingredientManagementService.normalizeIngredient(ingredient);

                assertThat(result.getUnit()).as("Unit normalization for: " + test[0])
                        .isEqualTo(test[1]);
            }
        }
    }

    // Helper methods
    private ParsedProduct createParsedProduct(String name) {
        return ParsedProduct.builder()
                .id("id-" + name)
                .name(name)
                .original(name)
                .quantity(100.0)
                .unit("ml")
                .categoryId("category-123")
                .build();
    }
}
