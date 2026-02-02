package com.noisevisionsoftware.vitema.service.diet.manual;

import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.service.category.ProductCategorizationService;
import com.noisevisionsoftware.vitema.service.product.ProductService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientManagementServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductCategorizationService categorizationService;

    @InjectMocks
    private IngredientManagementService ingredientManagementService;

    @BeforeEach
    void setUp() {
        // Setup for new implementation
    }

    @Nested
    @DisplayName("searchIngredients")
    class SearchIngredientsTests {

        @Test
        @DisplayName("Should return products from ProductService")
        void givenQuery_When_SearchIngredients_Then_ReturnProducts() {
            // Given
            String query = "chicken";
            int limit = 5;
            List<IngredientDTO> mockResults = List.of(
                    createIngredientDTO("Chicken Breast", "g"),
                    createIngredientDTO("Chicken Thigh", "g")
            );

            when(productService.searchProducts(query, null)).thenReturn(mockResults);

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("Chicken Breast");
            assertThat(result.get(1).getName()).isEqualTo("Chicken Thigh");
            verify(productService).searchProducts(query, null);
        }

        @Test
        @DisplayName("Should handle empty query")
        void givenEmptyQuery_When_SearchIngredients_Then_ReturnEmptyList() {
            // Given
            String query = "";
            int limit = 5;

            when(productService.searchProducts(query, null)).thenReturn(List.of());

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(productService).searchProducts(query, null);
        }

        @Test
        @DisplayName("Should handle service exception")
        void givenServiceException_When_SearchIngredients_Then_ReturnEmptyList() {
            // Given
            String query = "chicken";
            int limit = 5;

            when(productService.searchProducts(anyString(), any())).thenThrow(new RuntimeException("Service error"));

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should apply limit to results")
        void givenMoreResultsThanLimit_When_SearchIngredients_Then_ApplyLimit() {
            // Given
            String query = "chicken";
            int limit = 2;
            List<IngredientDTO> mockResults = List.of(
                    createIngredientDTO("Chicken Breast", "g"),
                    createIngredientDTO("Chicken Thigh", "g"),
                    createIngredientDTO("Chicken Wing", "g")
            );

            when(productService.searchProducts(query, null)).thenReturn(mockResults);

            // When
            List<ParsedProduct> result = ingredientManagementService.searchIngredients(query, limit);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("createIngredient - Deprecated")
    class CreateIngredientTests {

        @Test
        @DisplayName("Should generate ID when ingredient has no ID")
        void givenIngredientWithoutId_When_CreateIngredient_Then_GenerateId() {
            // Given
            ParsedProduct ingredient = ParsedProduct.builder()
                    .name("New ingredient")
                    .original("New ingredient")
                    .quantity(100.0)
                    .unit("g")
                    .build();
            ingredient.setId(null);

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId()).isNotEmpty();
        }

        @Test
        @DisplayName("Should keep existing ID when ingredient has ID")
        void givenIngredientWithId_When_CreateIngredient_Then_KeepExistingId() {
            // Given
            String existingId = "existing-id-123";
            ParsedProduct ingredient = ParsedProduct.builder()
                    .id(existingId)
                    .name("Ingredient")
                    .original("Ingredient")
                    .quantity(100.0)
                    .unit("g")
                    .build();

            // When
            ParsedProduct result = ingredientManagementService.createIngredient(ingredient);

            // Then
            assertThat(result.getId()).isEqualTo(existingId);
        }
    }

    @Nested
    @DisplayName("validateIngredient")
    class ValidateIngredientTests {

        @Test
        @DisplayName("Should return true for valid ingredient data")
        void givenValidData_When_ValidateIngredient_Then_ReturnTrue() {
            // When
            boolean result = ingredientManagementService.validateIngredient("Milk", 200.0, "ml");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null name")
        void givenNullName_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient(null, 100.0, "g");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty name")
        void givenEmptyName_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient("   ", 100.0, "g");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null quantity")
        void givenNullQuantity_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient("Milk", null, "ml");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for zero quantity")
        void givenZeroQuantity_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient("Milk", 0.0, "ml");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for negative quantity")
        void givenNegativeQuantity_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient("Milk", -10.0, "ml");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for null unit")
        void givenNullUnit_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient("Milk", 100.0, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty unit")
        void givenEmptyUnit_When_ValidateIngredient_Then_ReturnFalse() {
            // When
            boolean result = ingredientManagementService.validateIngredient("Milk", 100.0, "   ");

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("normalizeUnit")
    class NormalizeUnitTests {

        @Test
        @DisplayName("Should normalize unit variations to standard format")
        void givenUnitVariations_When_NormalizeUnit_Then_ReturnStandardUnit() {
            // Test various unit normalizations
            String[][] unitTests = {
                    {"gram", "g"},
                    {"kilogram", "kg"},
                    {"mililitr", "ml"},
                    {"litr", "l"},
                    {"łyżka", "łyżka"},
                    {"łyżeczka", "łyżeczka"},
                    {"szklanka", "szklanka"},
                    {"sztuka", "szt"}
            };

            for (String[] test : unitTests) {
                String result = ingredientManagementService.normalizeUnit(test[0]);
                assertThat(result).as("Unit normalization for: " + test[0])
                        .isEqualTo(test[1]);
            }
        }

        @Test
        @DisplayName("Should return default unit for null input")
        void givenNullUnit_When_NormalizeUnit_Then_ReturnDefaultUnit() {
            // When
            String result = ingredientManagementService.normalizeUnit(null);

            // Then
            assertThat(result).isEqualTo("szt");
        }
    }

    // Helper methods
    private IngredientDTO createIngredientDTO(String name, String unit) {
        return IngredientDTO.builder()
                .id("id-" + name.replace(" ", "-").toLowerCase())
                .name(name)
                .defaultUnit(unit)
                .categoryId("category-123")
                .type("GLOBAL")
                .build();
    }
}
