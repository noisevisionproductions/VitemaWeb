package com.noisevisionsoftware.vitema.service.diet.manual;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.product.IngredientDTO;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.ManualDietRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.PreviewMealSaveRequest;
import com.noisevisionsoftware.vitema.dto.request.diet.manual.SaveMealTemplateRequest;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.MealSavePreviewResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.MealSuggestionResponse;
import com.noisevisionsoftware.vitema.dto.response.diet.manual.MealTemplateResponse;
import com.noisevisionsoftware.vitema.model.diet.DietFileInfo;
import com.noisevisionsoftware.vitema.model.meal.MealTemplate;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.service.diet.DietManagerService;
import com.noisevisionsoftware.vitema.service.product.ProductService;
import com.noisevisionsoftware.vitema.utils.MealTemplateConverter;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManualDietServiceTest {

    @Mock
    private DietManagerService dietManagerService;

    @Mock
    private RecipeService recipeService;

    @Mock
    private MealTemplateService mealTemplateService;

    @Mock
    private MealSuggestionService mealSuggestionService;

    @Mock
    private IngredientManagementService ingredientManagementService;

    @Mock
    private DietValidationService dietValidationService;

    @Mock
    private DietDataConverter dietDataConverter;

    @Mock
    private ProductService productService;

    @Mock
    private MealTemplateConverter mealTemplateConverter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ManualDietService manualDietService;

    private ManualDietRequest validManualDietRequest;
    private SaveMealTemplateRequest saveMealTemplateRequest;
    private PreviewMealSaveRequest previewMealSaveRequest;
    private MealTemplate mealTemplate;
    private MealTemplateResponse mealTemplateResponse;
    private Recipe recipe;
    private String userId;
    private String templateId;
    private String recipeId;
    private String mealId;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        templateId = "template-123";
        recipeId = "123";
        mealId = "meal-123";

        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userId);

        // Setup valid ManualDietRequest
        ParsedProduct product = ParsedProduct.builder()
                .name("Chicken Breast")
                .quantity(200.0)
                .unit("g")
                .build();

        com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal meal =
                com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedMeal.builder()
                        .name("Breakfast")
                        .ingredients(List.of(product))
                        .build();

        com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay day =
                com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDay.builder()
                        .meals(List.of(meal))
                        .build();

        validManualDietRequest = ManualDietRequest.builder()
                .userId(userId)
                .days(List.of(day))
                .mealsPerDay(1)
                .duration(1)
                .startDate(LocalDate.now().toString())
                .mealTimes(Map.of("BREAKFAST", "08:00"))
                .build();

        // Setup SaveMealTemplateRequest
        saveMealTemplateRequest = SaveMealTemplateRequest.builder()
                .name("Test Meal")
                .instructions("Test instructions")
                .shouldSave(true)
                .build();

        // Setup PreviewMealSaveRequest
        previewMealSaveRequest = PreviewMealSaveRequest.builder()
                .name("Test Meal")
                .instructions("Test instructions")
                .build();

        // Setup MealTemplate
        mealTemplate = MealTemplate.builder()
                .id(templateId)
                .name("Test Meal")
                .instructions("Test instructions")
                .createdBy(userId)
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .usageCount(0)
                .build();

        // Setup MealTemplateResponse
        mealTemplateResponse = MealTemplateResponse.builder()
                .id(templateId)
                .name("Test Meal")
                .instructions("Test instructions")
                .build();

        // Setup Recipe
        recipe = Recipe.builder()
                .id(recipeId)
                .name("Test Recipe")
                .instructions("Test instructions")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("saveManualDiet")
    class SaveManualDietTests {

        @Test
        @DisplayName("Should save manual diet successfully")
        void givenValidRequest_When_SaveManualDiet_Then_ReturnDietId() {
            // Given
            String expectedDietId = "diet-123";
            ParsedDietData parsedData = new ParsedDietData();

            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("isValid", true);
            validationResult.put("errors", new ArrayList<>());

            when(dietValidationService.validateManualDiet(any(ManualDietRequest.class)))
                    .thenReturn(validationResult);
            when(dietDataConverter.convertToParsedDietData(any(ManualDietRequest.class)))
                    .thenReturn(parsedData);
            when(dietManagerService.saveDietWithShoppingList(any(ParsedDietData.class), anyString(), any(DietFileInfo.class)))
                    .thenReturn(expectedDietId);

            // When
            String result = manualDietService.saveManualDiet(validManualDietRequest);

            // Then
            assertThat(result).isEqualTo(expectedDietId);
            verify(dietValidationService).validateManualDiet(validManualDietRequest);
            verify(dietDataConverter).convertToParsedDietData(validManualDietRequest);
            verify(dietManagerService).saveDietWithShoppingList(eq(parsedData), eq(userId), any(DietFileInfo.class));
        }

        @Test
        @DisplayName("Should throw exception when validation fails")
        void givenInvalidRequest_When_SaveManualDiet_Then_ThrowException() {
            // Given
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("isValid", false);
            validationResult.put("errors", List.of("Error 1", "Error 2"));

            when(dietValidationService.validateManualDiet(any(ManualDietRequest.class)))
                    .thenReturn(validationResult);

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.saveManualDiet(validManualDietRequest))
                    .withMessageContaining("Nie udało się zapisać diety")
                    .withMessageContaining("Błędy walidacji");

            verify(dietValidationService).validateManualDiet(validManualDietRequest);
        }

        @Test
        @DisplayName("Should handle exception during save")
        void givenExceptionDuringSave_When_SaveManualDiet_Then_ThrowRuntimeException() {
            // Given
            Map<String, Object> validationResult = new HashMap<>();
            validationResult.put("isValid", true);
            validationResult.put("errors", new ArrayList<>());

            when(dietValidationService.validateManualDiet(any(ManualDietRequest.class)))
                    .thenReturn(validationResult);
            when(dietDataConverter.convertToParsedDietData(any(ManualDietRequest.class)))
                    .thenReturn(new ParsedDietData());
            when(dietManagerService.saveDietWithShoppingList(any(), anyString(), any()))
                    .thenThrow(new RuntimeException("Save error"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.saveManualDiet(validManualDietRequest))
                    .withMessageContaining("Nie udało się zapisać diety");
        }
    }

    @Nested
    @DisplayName("updateMealTemplate")
    class UpdateMealTemplateTests {

        @Test
        @DisplayName("Should update meal template successfully")
        void givenValidTemplateId_When_UpdateMealTemplate_Then_ReturnUpdatedTemplate() {
            // Given
            when(mealTemplateService.getById(templateId)).thenReturn(mealTemplate);
            when(mealTemplateConverter.convertNutritionalValuesFromRequest(any())).thenReturn(null);
            when(mealTemplateConverter.convertIngredientsFromRequest(any())).thenReturn(new ArrayList<>());
            when(mealTemplateService.save(any(MealTemplate.class))).thenReturn(mealTemplate);
            when(mealTemplateConverter.convertTemplateToResponse(any(MealTemplate.class)))
                    .thenReturn(mealTemplateResponse);

            // When
            MealTemplateResponse result = manualDietService.updateMealTemplate(templateId, saveMealTemplateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
            verify(mealTemplateService).getById(templateId);
            verify(mealTemplateService).save(any(MealTemplate.class));
        }

        @Test
        @DisplayName("Should update recipe when templateId starts with recipe-")
        void givenRecipeTemplateId_When_UpdateMealTemplate_Then_UpdateRecipe() {
            // Given
            String recipeTemplateId = "recipe-" + recipeId;
            when(recipeService.updateRecipe(anyString(), any(Recipe.class))).thenReturn(recipe);
            when(mealTemplateConverter.convertNutritionalValuesFromRequest(any())).thenReturn(null);
            when(mealTemplateConverter.convertRecipeToTemplate(any(Recipe.class)))
                    .thenReturn(mealTemplateResponse);

            // When
            MealTemplateResponse result = manualDietService.updateMealTemplate(recipeTemplateId, saveMealTemplateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Meal");
            verify(recipeService).updateRecipe(anyString(), any(Recipe.class));
            verify(mealTemplateConverter).convertRecipeToTemplate(any(Recipe.class));
            verify(mealTemplateService, never()).getById(anyString());
        }

        @Test
        @DisplayName("Should handle exception during update")
        void givenExceptionDuringUpdate_When_UpdateMealTemplate_Then_ThrowRuntimeException() {
            // Given
            when(mealTemplateService.getById(templateId))
                    .thenThrow(new RuntimeException("Template not found"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.updateMealTemplate(templateId, saveMealTemplateRequest))
                    .withMessageContaining("Nie udało się zaktualizować szablonu posiłku");
        }
    }

    @Nested
    @DisplayName("searchMealSuggestions")
    class SearchMealSuggestionsTests {

        @Test
        @DisplayName("Should return meal suggestions")
        void givenQuery_When_SearchMealSuggestions_Then_ReturnSuggestions() {
            // Given
            String query = "breakfast";
            int limit = 5;
            List<MealSuggestionResponse> expectedSuggestions = List.of(
                    MealSuggestionResponse.builder().name("Breakfast 1").build(),
                    MealSuggestionResponse.builder().name("Breakfast 2").build()
            );

            when(mealSuggestionService.searchMealSuggestions(query, limit, userId))
                    .thenReturn(expectedSuggestions);

            // When
            List<MealSuggestionResponse> result = manualDietService.searchMealSuggestions(query, limit);

            // Then
            assertThat(result).isEqualTo(expectedSuggestions);
            verify(mealSuggestionService).searchMealSuggestions(query, limit, userId);
        }
    }

    @Nested
    @DisplayName("getMealTemplate")
    class GetMealTemplateTests {

        @Test
        @DisplayName("Should return meal template by id")
        void givenTemplateId_When_GetMealTemplate_Then_ReturnTemplate() {
            // Given
            when(mealTemplateService.getById(templateId)).thenReturn(mealTemplate);
            when(mealTemplateConverter.convertTemplateToResponse(mealTemplate))
                    .thenReturn(mealTemplateResponse);

            // When
            MealTemplateResponse result = manualDietService.getMealTemplate(templateId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
            verify(mealTemplateService).getById(templateId);
            verify(mealTemplateConverter).convertTemplateToResponse(mealTemplate);
        }

        @Test
        @DisplayName("Should return recipe when id starts with recipe-")
        void givenRecipeId_When_GetMealTemplate_Then_ReturnRecipe() {
            // Given
            String recipeTemplateId = "recipe-" + recipeId;
            when(recipeService.getRecipeById(anyString())).thenReturn(recipe);
            when(mealTemplateConverter.convertRecipeToTemplate(any(Recipe.class)))
                    .thenReturn(mealTemplateResponse);

            // When
            MealTemplateResponse result = manualDietService.getMealTemplate(recipeTemplateId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Meal");
            verify(recipeService).getRecipeById(recipeId);
            verify(mealTemplateConverter).convertRecipeToTemplate(any(Recipe.class));
            verify(mealTemplateService, never()).getById(anyString());
        }

        @Test
        @DisplayName("Should throw exception when template not found")
        void givenNonExistentId_When_GetMealTemplate_Then_ThrowException() {
            // Given
            when(mealTemplateService.getById(templateId))
                    .thenThrow(new RuntimeException("Template not found"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.getMealTemplate(templateId))
                    .withMessageContaining("Nie znaleziono szablonu posiłku");
        }
    }

    @Nested
    @DisplayName("previewMealSave")
    class PreviewMealSaveTests {

        @Test
        @DisplayName("Should return USE_EXISTING when exact meal found")
        void givenExactMealExists_When_PreviewMealSave_Then_ReturnUseExisting() {
            // Given
            when(mealSuggestionService.existsExactMeal(anyString(), eq(userId))).thenReturn(true);
            when(mealSuggestionService.findSimilarMeals(anyString(), anyInt(), eq(userId)))
                    .thenReturn(new ArrayList<>());
            when(mealSuggestionService.findHighlySimilarMeals(anyString(), eq(userId)))
                    .thenReturn(new ArrayList<>());

            // When
            MealSavePreviewResponse result = manualDietService.previewMealSave(previewMealSaveRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecommendedAction()).isEqualTo("USE_EXISTING");
            assertThat(result.isWillCreateNew()).isFalse();
            verify(mealSuggestionService).existsExactMeal(anyString(), eq(userId));
        }

        @Test
        @DisplayName("Should return UPDATE_EXISTING when highly similar meals found")
        void givenHighlySimilarMeals_When_PreviewMealSave_Then_ReturnUpdateExisting() {
            // Given
            List<MealSuggestionResponse> similarMeals = List.of(
                    MealSuggestionResponse.builder().name("Similar Meal").build()
            );

            when(mealSuggestionService.existsExactMeal(anyString(), eq(userId))).thenReturn(false);
            when(mealSuggestionService.findSimilarMeals(anyString(), anyInt(), eq(userId)))
                    .thenReturn(new ArrayList<>());
            when(mealSuggestionService.findHighlySimilarMeals(anyString(), eq(userId)))
                    .thenReturn(similarMeals);

            // When
            MealSavePreviewResponse result = manualDietService.previewMealSave(previewMealSaveRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecommendedAction()).isEqualTo("UPDATE_EXISTING");
            assertThat(result.isFoundSimilar()).isTrue();
            assertThat(result.getSimilarMeals()).isEqualTo(similarMeals);
        }

        @Test
        @DisplayName("Should return CREATE_NEW when no similar meals found")
        void givenNoSimilarMeals_When_PreviewMealSave_Then_ReturnCreateNew() {
            // Given
            when(mealSuggestionService.existsExactMeal(anyString(), eq(userId))).thenReturn(false);
            when(mealSuggestionService.findSimilarMeals(anyString(), anyInt(), eq(userId)))
                    .thenReturn(new ArrayList<>());
            when(mealSuggestionService.findHighlySimilarMeals(anyString(), eq(userId)))
                    .thenReturn(new ArrayList<>());

            // When
            MealSavePreviewResponse result = manualDietService.previewMealSave(previewMealSaveRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRecommendedAction()).isEqualTo("CREATE_NEW");
            assertThat(result.isWillCreateNew()).isTrue();
            assertThat(result.isFoundSimilar()).isFalse();
        }

        @Test
        @DisplayName("Should handle exception during preview")
        void givenExceptionDuringPreview_When_PreviewMealSave_Then_ThrowRuntimeException() {
            // Given
            when(mealSuggestionService.existsExactMeal(anyString(), eq(userId)))
                    .thenThrow(new RuntimeException("Service error"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.previewMealSave(previewMealSaveRequest))
                    .withMessageContaining("Błąd podczas sprawdzania podobnych posiłków");
        }
    }

    @Nested
    @DisplayName("saveMealTemplate")
    class SaveMealTemplateTests {

        @Test
        @DisplayName("Should save meal template when shouldSave is true")
        void givenShouldSaveTrue_When_SaveMealTemplate_Then_SaveTemplate() {
            // Given
            saveMealTemplateRequest.setShouldSave(true);
            when(mealTemplateConverter.convertRequestToTemplate(any(SaveMealTemplateRequest.class)))
                    .thenReturn(mealTemplate);
            when(mealTemplateService.save(any(MealTemplate.class))).thenReturn(mealTemplate);
            when(mealTemplateConverter.convertTemplateToResponse(any(MealTemplate.class)))
                    .thenReturn(mealTemplateResponse);

            // When
            MealTemplateResponse result = manualDietService.saveMealTemplate(saveMealTemplateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
            verify(mealTemplateService).save(any(MealTemplate.class));
            verify(mealTemplateService).incrementUsageCount(templateId);
        }

        @Test
        @DisplayName("Should not save when shouldSave is false")
        void givenShouldSaveFalse_When_SaveMealTemplate_Then_ReturnBasicResponse() {
            // Given
            saveMealTemplateRequest.setShouldSave(false);

            // When
            MealTemplateResponse result = manualDietService.saveMealTemplate(saveMealTemplateRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(saveMealTemplateRequest.getName());
            assertThat(result.getInstructions()).isEqualTo(saveMealTemplateRequest.getInstructions());
            verify(mealTemplateService, never()).save(any(MealTemplate.class));
            verify(mealTemplateService, never()).incrementUsageCount(anyString());
        }

        @Test
        @DisplayName("Should handle exception during save")
        void givenExceptionDuringSave_When_SaveMealTemplate_Then_ThrowRuntimeException() {
            // Given
            saveMealTemplateRequest.setShouldSave(true);
            when(mealTemplateConverter.convertRequestToTemplate(any(SaveMealTemplateRequest.class)))
                    .thenThrow(new RuntimeException("Conversion error"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.saveMealTemplate(saveMealTemplateRequest))
                    .withMessageContaining("Nie udało się zapisać szablonu posiłku");
        }
    }

    @Nested
    @DisplayName("searchIngredients")
    class SearchIngredientsTests {

        @Test
        @DisplayName("Should delegate to ingredientManagementService with userId")
        void givenQuery_When_SearchIngredients_Then_DelegateToService() {
            // Given
            String query = "mleko";
            int limit = 5;
            List<IngredientDTO> expectedResults = List.of(
                    IngredientDTO.builder()
                            .id("ingredient-1")
                            .name("Mleko")
                            .defaultUnit("ml")
                            .build()
            );

            when(ingredientManagementService.searchIngredientsNew(query, userId, limit))
                    .thenReturn(expectedResults);

            // When
            List<IngredientDTO> result = manualDietService.searchIngredients(query, limit);

            // Then
            assertThat(result).isEqualTo(expectedResults);
            verify(ingredientManagementService).searchIngredientsNew(query, userId, limit);
        }
    }

    @Nested
    @DisplayName("createIngredient")
    class CreateIngredientTests {

        @Test
        @DisplayName("Should create product and return IngredientDTO")
        void givenIngredient_When_CreateIngredient_Then_CreateProductAndReturnDTO() {
            // Given
            com.noisevisionsoftware.vitema.model.recipe.NutritionalValues nutritionalValues =
                    com.noisevisionsoftware.vitema.model.recipe.NutritionalValues.builder()
                            .calories(100.0)
                            .protein(20.0)
                            .fat(5.0)
                            .carbs(10.0)
                            .build();

            IngredientDTO ingredientDTO = IngredientDTO.builder()
                    .name("New Ingredient")
                    .defaultUnit("g")
                    .categoryId("category-1")
                    .nutritionalValues(nutritionalValues)
                    .build();

            com.noisevisionsoftware.vitema.model.product.Product savedProduct =
                    com.noisevisionsoftware.vitema.model.product.Product.builder()
                            .id("product-123")
                            .name("New Ingredient")
                            .defaultUnit("g")
                            .categoryId("category-1")
                            .type(com.noisevisionsoftware.vitema.model.product.ProductType.CUSTOM)
                            .build();

            when(productService.createProduct(any(com.noisevisionsoftware.vitema.model.product.Product.class), eq(userId)))
                    .thenReturn(savedProduct);

            // When
            IngredientDTO result = manualDietService.createIngredient(ingredientDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("product-123");
            assertThat(result.getName()).isEqualTo("New Ingredient");
            assertThat(result.getDefaultUnit()).isEqualTo("g");
            assertThat(result.getCategoryId()).isEqualTo("category-1");
            assertThat(result.getType()).isEqualTo("CUSTOM");
            verify(productService).createProduct(any(com.noisevisionsoftware.vitema.model.product.Product.class), eq(userId));
        }
    }

    @Nested
    @DisplayName("validateManualDiet")
    class ValidateManualDietTests {

        @Test
        @DisplayName("Should delegate to dietValidationService")
        void givenRequest_When_ValidateManualDiet_Then_DelegateToService() {
            // Given
            Map<String, Object> expectedResult = new HashMap<>();
            expectedResult.put("isValid", true);
            expectedResult.put("errors", new ArrayList<>());

            when(dietValidationService.validateManualDiet(validManualDietRequest))
                    .thenReturn(expectedResult);

            // When
            Map<String, Object> result = manualDietService.validateManualDiet(validManualDietRequest);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            verify(dietValidationService).validateManualDiet(validManualDietRequest);
        }
    }

    @Nested
    @DisplayName("uploadMealImage")
    class UploadMealImageTests {

        @Test
        @DisplayName("Should upload image when mealId is provided")
        void givenMealId_When_UploadMealImage_Then_UploadToRecipe() throws Exception {
            // Given
            MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
            String expectedUrl = "https://storage.example.com/image.jpg";

            when(recipeService.uploadImage(mealId, image)).thenReturn(expectedUrl);

            // When
            String result = manualDietService.uploadMealImage(image, mealId);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(recipeService).uploadImage(mealId, image);
            verify(recipeService, never()).uploadBase64Image(anyString());
        }

        @Test
        @DisplayName("Should upload base64 image when mealId is null")
        void givenNullMealId_When_UploadMealImage_Then_UploadBase64() throws Exception {
            // Given
            MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
            String expectedUrl = "https://storage.example.com/image.jpg";

            when(recipeService.uploadBase64Image(anyString())).thenReturn(expectedUrl);

            // When
            String result = manualDietService.uploadMealImage(image, null);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(recipeService).uploadBase64Image(anyString());
            verify(recipeService, never()).uploadImage(anyString(), any());
        }

        @Test
        @DisplayName("Should upload base64 image when mealId is empty")
        void givenEmptyMealId_When_UploadMealImage_Then_UploadBase64() throws Exception {
            // Given
            MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
            String expectedUrl = "https://storage.example.com/image.jpg";

            when(recipeService.uploadBase64Image(anyString())).thenReturn(expectedUrl);

            // When
            String result = manualDietService.uploadMealImage(image, "");

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(recipeService).uploadBase64Image(anyString());
            verify(recipeService, never()).uploadImage(anyString(), any());
        }

        @Test
        @DisplayName("Should handle exception during upload")
        void givenExceptionDuringUpload_When_UploadMealImage_Then_ThrowRuntimeException() throws Exception {
            // Given
            MultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
            when(recipeService.uploadImage(anyString(), any())).thenThrow(new RuntimeException("Upload error"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.uploadMealImage(image, mealId))
                    .withMessageContaining("Nie udało się przesłać zdjęcia");
        }
    }

    @Nested
    @DisplayName("uploadBase64MealImage")
    class UploadBase64MealImageTests {

        @Test
        @DisplayName("Should upload base64 image successfully")
        void givenBase64Image_When_UploadBase64MealImage_Then_ReturnUrl() throws BadRequestException {
            // Given
            String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
            String expectedUrl = "https://storage.example.com/image.jpg";

            when(recipeService.uploadBase64Image(base64Image)).thenReturn(expectedUrl);

            // When
            String result = manualDietService.uploadBase64MealImage(base64Image);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(recipeService).uploadBase64Image(base64Image);
        }

        @Test
        @DisplayName("Should handle exception during upload")
        void givenExceptionDuringUpload_When_UploadBase64MealImage_Then_ThrowRuntimeException() throws BadRequestException {
            // Given
            String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRg==";
            when(recipeService.uploadBase64Image(base64Image))
                    .thenThrow(new RuntimeException("Upload error"));

            // When & Then
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> manualDietService.uploadBase64MealImage(base64Image))
                    .withMessageContaining("Nie udało się przesłać zdjęcia");
        }
    }

    @Nested
    @DisplayName("getCurrentUserId")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("Should return userId when authentication is present")
        void givenAuthentication_When_GetCurrentUserId_Then_ReturnUserId() {
            // Given - already set up in setUp()

            // When - tested indirectly through other methods
            // This is tested through searchMealSuggestions which uses getCurrentUserId
            List<MealSuggestionResponse> suggestions = new ArrayList<>();
            when(mealSuggestionService.searchMealSuggestions(anyString(), anyInt(), eq(userId)))
                    .thenReturn(suggestions);

            manualDietService.searchMealSuggestions("test", 5);

            // Then
            verify(mealSuggestionService).searchMealSuggestions(anyString(), anyInt(), eq(userId));
        }

        @Test
        @DisplayName("Should handle null authentication gracefully")
        void givenNullAuthentication_When_GetCurrentUserId_Then_ReturnNull() {
            // Given
            when(securityContext.getAuthentication()).thenReturn(null);
            List<MealSuggestionResponse> suggestions = new ArrayList<>();
            when(mealSuggestionService.searchMealSuggestions(anyString(), anyInt(), isNull()))
                    .thenReturn(suggestions);

            // When
            manualDietService.searchMealSuggestions("test", 5);

            // Then
            verify(mealSuggestionService).searchMealSuggestions(anyString(), anyInt(), isNull());
        }
    }
}
