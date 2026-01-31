package com.noisevisionsoftware.vitema.service.diet;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.noisevisionsoftware.vitema.mapper.diet.FirestoreDietMapper;
import com.noisevisionsoftware.vitema.model.diet.Diet;
import com.noisevisionsoftware.vitema.model.diet.DietFileInfo;
import com.noisevisionsoftware.vitema.model.meal.MealType;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import com.noisevisionsoftware.vitema.model.recipe.Recipe;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import com.noisevisionsoftware.vitema.service.RecipeService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.*;
import com.noisevisionsoftware.vitema.utils.excelParser.service.ProductParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DietManagerService Tests")
class DietManagerServiceTest {

    @Mock
    private Firestore firestore;

    @Mock
    private ProductParsingService productParsingService;

    @Mock
    private FirestoreDietMapper firestoreMapper;

    @Mock
    private DietService dietService;

    @Mock
    private RecipeService recipeService;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private DietManagerService dietManagerService;

    private static final String TEST_USER_ID = "user123";
    private static final String TEST_DIET_ID = "diet123";
    private static final String TEST_RECIPE_ID = "recipe123";
    private static final String TEST_FILE_NAME = "test_diet.xlsx";
    private static final String TEST_FILE_URL = "https://storage.example.com/test_diet.xlsx";

    private DietFileInfo testFileInfo;
    private ParsedDietData testParsedDietData;

    @BeforeEach
    void setUp() {
        testFileInfo = new DietFileInfo(TEST_FILE_NAME, TEST_FILE_URL);
        testParsedDietData = createTestParsedDietData();
    }

    @Nested
    @DisplayName("saveDietWithShoppingList")
    class SaveDietWithShoppingListTests {

        @Test
        @DisplayName("Should save diet successfully and return diet ID when all data is valid")
        void givenValidData_When_SaveDietWithShoppingList_Then_ReturnDietId() throws Exception {
            // Given
            DocumentReference dietDocRef = setupMockDietDocRef();
            setupMockShoppingList();
            setupMockMapper();
            setupMockProductParsing();
            setupMockRecipeService();

            // When
            String result = dietManagerService.saveDietWithShoppingList(
                    testParsedDietData, TEST_USER_ID, testFileInfo);

            // Then
            assertThat(result).isEqualTo(TEST_DIET_ID);
            verify(dietDocRef).set(any());
            verify(dietDocRef).update(eq("days"), any());
            verify(recipeService).refreshRecipesCache();
            verify(dietService).refreshDietsCache();
        }

        @Test
        @DisplayName("Should save recipes for all meals in all days")
        void givenMultipleDaysWithMeals_When_SaveDietWithShoppingList_Then_SaveAllRecipes() throws Exception {
            // Given
            setupMockDietDocRef();
            setupMockShoppingList();
            setupMockMapper();
            setupMockProductParsing();
            setupMockRecipeService();

            int expectedRecipeCount = testParsedDietData.getDays().stream()
                    .mapToInt(day -> day.getMeals().size())
                    .sum();

            // When
            dietManagerService.saveDietWithShoppingList(testParsedDietData, TEST_USER_ID, testFileInfo);

            // Then
            verify(recipeService, times(expectedRecipeCount)).findOrCreateRecipe(any(Recipe.class));
            verify(recipeRepository, times(expectedRecipeCount)).saveReference(any(RecipeReference.class));
        }

        @Test
        @DisplayName("Should save shopping list with correct structure")
        void givenValidData_When_SaveDietWithShoppingList_Then_SaveShoppingListCorrectly() throws Exception {
            // Given
            setupMockDietDocRef();
            CollectionReference shoppingListsCollectionRef = setupMockShoppingList();
            setupMockMapper();
            setupMockProductParsing();
            setupMockRecipeService();

            // When
            dietManagerService.saveDietWithShoppingList(testParsedDietData, TEST_USER_ID, testFileInfo);

            // Then
            ArgumentCaptor<Map<String, Object>> shoppingListCaptor = ArgumentCaptor.forClass(Map.class);
            verify(shoppingListsCollectionRef).add(shoppingListCaptor.capture());

            Map<String, Object> shoppingList = shoppingListCaptor.getValue();
            assertThat(shoppingList)
                    .containsEntry("dietId", TEST_DIET_ID)
                    .containsEntry("userId", TEST_USER_ID)
                    .containsEntry("version", 3)
                    .containsKeys("items", "createdAt", "startDate", "endDate");
        }

        @Test
        @DisplayName("Should throw RuntimeException when Firestore collection access fails")
        void givenFirestoreFailure_When_SaveDietWithShoppingList_Then_ThrowRuntimeException() {
            // Given
            when(firestore.collection("diets")).thenThrow(new RuntimeException("Firestore connection error"));

            // When & Then
            assertThatThrownBy(() -> dietManagerService.saveDietWithShoppingList(
                    testParsedDietData, TEST_USER_ID, testFileInfo))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to save diet")
                    .hasCauseInstanceOf(RuntimeException.class);

            verify(recipeService, never()).refreshRecipesCache();
            verify(dietService, never()).refreshDietsCache();
        }

        @Test
        @DisplayName("Should throw RuntimeException when diet save fails")
        void givenDietSaveFailure_When_SaveDietWithShoppingList_Then_ThrowRuntimeException() throws Exception {
            // Given
            DocumentReference dietDocRef = mock(DocumentReference.class);
            CollectionReference dietsCollectionRef = mock(CollectionReference.class);
            when(firestore.collection("diets")).thenReturn(dietsCollectionRef);
            when(dietsCollectionRef.document()).thenReturn(dietDocRef);

            @SuppressWarnings("unchecked")
            ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
            when(writeResultFuture.get()).thenThrow(new RuntimeException("Save failed"));
            when(dietDocRef.set(any())).thenReturn(writeResultFuture);
            when(firestoreMapper.toFirestoreMap(any(Diet.class))).thenReturn(new HashMap<>());

            // When & Then
            assertThatThrownBy(() -> dietManagerService.saveDietWithShoppingList(
                    testParsedDietData, TEST_USER_ID, testFileInfo))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to save diet");

            verify(recipeService, never()).refreshRecipesCache();
            verify(dietService, never()).refreshDietsCache();
        }

        @Test
        @DisplayName("Should create diet with correct metadata")
        void givenValidData_When_SaveDietWithShoppingList_Then_CreateDietWithCorrectMetadata() throws Exception {
            // Given
            DocumentReference dietDocRef = setupMockDietDocRef();
            setupMockShoppingList();
            setupMockProductParsing();
            setupMockRecipeService();

            ArgumentCaptor<Diet> dietCaptor = ArgumentCaptor.forClass(Diet.class);
            when(firestoreMapper.toFirestoreMap(dietCaptor.capture())).thenReturn(new HashMap<>());

            // When
            dietManagerService.saveDietWithShoppingList(testParsedDietData, TEST_USER_ID, testFileInfo);

            // Then
            Diet capturedDiet = dietCaptor.getValue();
            assertThat(capturedDiet).isNotNull();
            assertThat(capturedDiet.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(capturedDiet.getMetadata().getTotalDays()).isEqualTo(testParsedDietData.getDays().size());
            assertThat(capturedDiet.getMetadata().getFileName()).isEqualTo(TEST_FILE_NAME);
            assertThat(capturedDiet.getMetadata().getFileUrl()).isEqualTo(TEST_FILE_URL);
        }

        private DocumentReference setupMockDietDocRef() throws Exception {
            DocumentReference dietDocRef = mock(DocumentReference.class);
            CollectionReference dietsCollectionRef = mock(CollectionReference.class);
            when(firestore.collection("diets")).thenReturn(dietsCollectionRef);
            when(dietsCollectionRef.document()).thenReturn(dietDocRef);
            when(dietDocRef.getId()).thenReturn(TEST_DIET_ID);

            @SuppressWarnings("unchecked")
            ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
            WriteResult writeResult = mock(WriteResult.class);
            when(writeResultFuture.get()).thenReturn(writeResult);
            when(dietDocRef.set(any())).thenReturn(writeResultFuture);
            when(dietDocRef.update(eq("days"), any())).thenReturn(writeResultFuture);

            return dietDocRef;
        }

        private CollectionReference setupMockShoppingList() throws Exception {
            CollectionReference shoppingListsCollectionRef = mock(CollectionReference.class);
            DocumentReference shoppingListDocRef = mock(DocumentReference.class);

            @SuppressWarnings("unchecked")
            ApiFuture<DocumentReference> documentReferenceFuture = mock(ApiFuture.class);
            when(firestore.collection("shopping_lists")).thenReturn(shoppingListsCollectionRef);
            when(shoppingListsCollectionRef.add(any())).thenReturn(documentReferenceFuture);
            when(documentReferenceFuture.get()).thenReturn(shoppingListDocRef);

            return shoppingListsCollectionRef;
        }

        private void setupMockMapper() {
            when(firestoreMapper.toFirestoreMap(any(Diet.class))).thenReturn(new HashMap<>());
        }

        private void setupMockProductParsing() {
            ParsingResult parsingResult = mock(ParsingResult.class);
            ParsedProduct parsedProduct = new ParsedProduct("jabłka", 1.0, "kg", "1kg jabłek", false);
            when(parsingResult.getProduct()).thenReturn(parsedProduct);
            when(productParsingService.parseProduct(anyString())).thenReturn(parsingResult);
        }

        private void setupMockRecipeService() {
            Recipe testRecipe = Recipe.builder()
                    .id(TEST_RECIPE_ID)
                    .name("Test Recipe")
                    .build();
            when(recipeService.findOrCreateRecipe(any(Recipe.class))).thenReturn(testRecipe);
            doNothing().when(recipeRepository).saveReference(any(RecipeReference.class));
        }
    }

    @Nested
    @DisplayName("saveRecipes")
    class SaveRecipesTests {

        @Test
        @DisplayName("Should save all recipes from parsed data and return recipe IDs map")
        void givenValidParsedData_When_SaveRecipes_Then_SaveAllRecipesAndReturnIds() {
            // Given
            Recipe savedRecipe = Recipe.builder()
                    .id(TEST_RECIPE_ID)
                    .name("Test Recipe")
                    .build();
            when(recipeService.findOrCreateRecipe(any(Recipe.class))).thenReturn(savedRecipe);
            doNothing().when(recipeRepository).saveReference(any(RecipeReference.class));

            // When
            Map<String, String> result = dietManagerService.saveRecipes(
                    testParsedDietData, TEST_USER_ID, TEST_DIET_ID);

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).containsKey("0_BREAKFAST");
            assertThat(result).containsKey("1_LUNCH");
            assertThat(result.values()).allMatch(id -> id.equals(TEST_RECIPE_ID));
        }

        @Test
        @DisplayName("Should create recipes with correct properties")
        void givenParsedMeal_When_SaveRecipes_Then_CreateRecipeWithCorrectProperties() {
            // Given
            Recipe savedRecipe = Recipe.builder()
                    .id(TEST_RECIPE_ID)
                    .name("Test Recipe")
                    .build();

            ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
            when(recipeService.findOrCreateRecipe(recipeCaptor.capture())).thenReturn(savedRecipe);
            doNothing().when(recipeRepository).saveReference(any(RecipeReference.class));

            // When
            dietManagerService.saveRecipes(testParsedDietData, TEST_USER_ID, TEST_DIET_ID);

            // Then
            List<Recipe> capturedRecipes = recipeCaptor.getAllValues();
            assertThat(capturedRecipes).hasSizeGreaterThan(0);

            Recipe firstRecipe = capturedRecipes.get(0);
            assertThat(firstRecipe.getName()).isEqualTo("Owsianka z owocami");
            assertThat(firstRecipe.getInstructions()).isNotNull();
            assertThat(firstRecipe.getNutritionalValues()).isNotNull();
            assertThat(firstRecipe.getCreatedAt()).isNotNull();
            assertThat(firstRecipe.getParentRecipeId()).isNull();
        }

        @Test
        @DisplayName("Should create recipe references with correct properties")
        void givenSavedRecipe_When_SaveRecipes_Then_CreateRecipeReferences() {
            // Given
            Recipe savedRecipe = Recipe.builder()
                    .id(TEST_RECIPE_ID)
                    .name("Test Recipe")
                    .build();
            when(recipeService.findOrCreateRecipe(any(Recipe.class))).thenReturn(savedRecipe);

            ArgumentCaptor<RecipeReference> referenceCaptor = ArgumentCaptor.forClass(RecipeReference.class);
            doNothing().when(recipeRepository).saveReference(referenceCaptor.capture());

            // When
            dietManagerService.saveRecipes(testParsedDietData, TEST_USER_ID, TEST_DIET_ID);

            // Then
            List<RecipeReference> references = referenceCaptor.getAllValues();
            assertThat(references).hasSizeGreaterThan(0);

            RecipeReference firstRef = references.get(0);
            assertThat(firstRef.getRecipeId()).isEqualTo(TEST_RECIPE_ID);
            assertThat(firstRef.getDietId()).isEqualTo(TEST_DIET_ID);
            assertThat(firstRef.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(firstRef.getMealType()).isNotNull();
            assertThat(firstRef.getAddedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle meals with null photos by creating empty list")
        void givenMealWithNullPhotos_When_SaveRecipes_Then_CreateRecipeWithEmptyPhotosList() {
            // Given
            Recipe savedRecipe = Recipe.builder()
                    .id(TEST_RECIPE_ID)
                    .name("Test Recipe")
                    .build();

            ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
            when(recipeService.findOrCreateRecipe(recipeCaptor.capture())).thenReturn(savedRecipe);
            doNothing().when(recipeRepository).saveReference(any(RecipeReference.class));

            // When
            dietManagerService.saveRecipes(testParsedDietData, TEST_USER_ID, TEST_DIET_ID);

            // Then
            List<Recipe> capturedRecipes = recipeCaptor.getAllValues();
            assertThat(capturedRecipes).allMatch(recipe -> recipe.getPhotos() != null);
        }
    }

    @Nested
    @DisplayName("createDaysWithMeals")
    class CreateDaysWithMealsTests {

        @Test
        @DisplayName("Should create days with meals from parsed data and recipe IDs")
        void givenParsedDataAndRecipeIds_When_CreateDaysWithMeals_Then_CreateDaysStructure() {
            // Given
            Map<String, String> savedRecipeIds = new HashMap<>();
            savedRecipeIds.put("0_BREAKFAST", TEST_RECIPE_ID);
            savedRecipeIds.put("1_LUNCH", "recipe456");

            // When
            List<Map<String, Object>> result = dietManagerService.createDaysWithMeals(
                    testParsedDietData, savedRecipeIds);

            // Then
            assertThat(result).hasSize(testParsedDietData.getDays().size());
            assertThat(result.get(0)).containsKeys("date", "meals");
        }

        @Test
        @DisplayName("Should create meals with correct structure")
        void givenParsedDataAndRecipeIds_When_CreateDaysWithMeals_Then_CreateMealsWithCorrectStructure() {
            // Given
            Map<String, String> savedRecipeIds = new HashMap<>();
            savedRecipeIds.put("0_BREAKFAST", TEST_RECIPE_ID);
            savedRecipeIds.put("1_LUNCH", "recipe456");

            // When
            List<Map<String, Object>> result = dietManagerService.createDaysWithMeals(
                    testParsedDietData, savedRecipeIds);

            // Then
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> meals = (List<Map<String, Object>>) result.get(0).get("meals");
            assertThat(meals).isNotEmpty();

            Map<String, Object> firstMeal = meals.get(0);
            assertThat(firstMeal).containsKeys("recipeId", "mealType", "time");
            assertThat(firstMeal.get("recipeId")).isEqualTo(TEST_RECIPE_ID);
            assertThat(firstMeal.get("mealType")).isEqualTo("BREAKFAST");
            assertThat(firstMeal.get("time")).isEqualTo("08:00");
        }

        @Test
        @DisplayName("Should create correct number of days")
        void givenMultipleDays_When_CreateDaysWithMeals_Then_CreateCorrectNumberOfDays() {
            // Given
            Map<String, String> savedRecipeIds = new HashMap<>();
            savedRecipeIds.put("0_BREAKFAST", TEST_RECIPE_ID);
            savedRecipeIds.put("1_LUNCH", "recipe456");

            // When
            List<Map<String, Object>> result = dietManagerService.createDaysWithMeals(
                    testParsedDietData, savedRecipeIds);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("saveShoppingList")
    class SaveShoppingListTests {

        @Test
        @DisplayName("Should save shopping list with all required fields")
        void givenValidParsedData_When_SaveShoppingList_Then_SaveWithAllRequiredFields() throws Exception {
            // Given
            CollectionReference shoppingListsCollectionRef = setupMockShoppingListCollection();
            setupMockProductParsing();

            // When
            dietManagerService.saveShoppingList(testParsedDietData, TEST_USER_ID, TEST_DIET_ID);

            // Then
            ArgumentCaptor<Map<String, Object>> shoppingListCaptor = ArgumentCaptor.forClass(Map.class);
            verify(shoppingListsCollectionRef).add(shoppingListCaptor.capture());

            Map<String, Object> shoppingList = shoppingListCaptor.getValue();
            assertThat(shoppingList)
                    .containsEntry("dietId", TEST_DIET_ID)
                    .containsEntry("userId", TEST_USER_ID)
                    .containsEntry("version", 3)
                    .containsKeys("items", "createdAt", "startDate", "endDate");
        }

        @Test
        @DisplayName("Should handle null categorized products gracefully")
        void givenNullCategorizedProducts_When_SaveShoppingList_Then_SaveWithEmptyItems() throws Exception {
            // Given
            ParsedDietData dataWithoutProducts = createTestParsedDietData();
            dataWithoutProducts.setCategorizedProducts(null);

            CollectionReference shoppingListsCollectionRef = setupMockShoppingListCollection();

            // When
            dietManagerService.saveShoppingList(dataWithoutProducts, TEST_USER_ID, TEST_DIET_ID);

            // Then
            ArgumentCaptor<Map<String, Object>> shoppingListCaptor = ArgumentCaptor.forClass(Map.class);
            verify(shoppingListsCollectionRef).add(shoppingListCaptor.capture());

            Map<String, Object> shoppingList = shoppingListCaptor.getValue();
            @SuppressWarnings("unchecked")
            Map<String, List<Map<String, Object>>> items = 
                    (Map<String, List<Map<String, Object>>>) shoppingList.get("items");
            assertThat(items).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when Firestore save fails")
        void givenFirestoreFailure_When_SaveShoppingList_Then_ThrowException() throws Exception {
            // Given
            CollectionReference shoppingListsCollectionRef = mock(CollectionReference.class);
            when(firestore.collection("shopping_lists")).thenReturn(shoppingListsCollectionRef);

            @SuppressWarnings("unchecked")
            ApiFuture<DocumentReference> documentReferenceFuture = mock(ApiFuture.class);
            when(shoppingListsCollectionRef.add(any())).thenReturn(documentReferenceFuture);
            when(documentReferenceFuture.get()).thenThrow(new RuntimeException("Firestore error"));

            setupMockProductParsing();

            // When & Then
            assertThatThrownBy(() -> dietManagerService.saveShoppingList(
                    testParsedDietData, TEST_USER_ID, TEST_DIET_ID))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should set start and end dates from diet days")
        void givenDietWithMultipleDays_When_SaveShoppingList_Then_SetCorrectDates() throws Exception {
            // Given
            CollectionReference shoppingListsCollectionRef = setupMockShoppingListCollection();
            setupMockProductParsing();

            // When
            dietManagerService.saveShoppingList(testParsedDietData, TEST_USER_ID, TEST_DIET_ID);

            // Then
            ArgumentCaptor<Map<String, Object>> shoppingListCaptor = ArgumentCaptor.forClass(Map.class);
            verify(shoppingListsCollectionRef).add(shoppingListCaptor.capture());

            Map<String, Object> shoppingList = shoppingListCaptor.getValue();
            assertThat(shoppingList.get("startDate"))
                    .isEqualTo(testParsedDietData.getDays().get(0).getDate());
            assertThat(shoppingList.get("endDate"))
                    .isEqualTo(testParsedDietData.getDays().get(testParsedDietData.getDays().size() - 1).getDate());
        }

        private CollectionReference setupMockShoppingListCollection() throws Exception {
            CollectionReference shoppingListsCollectionRef = mock(CollectionReference.class);
            DocumentReference shoppingListDocRef = mock(DocumentReference.class);

            @SuppressWarnings("unchecked")
            ApiFuture<DocumentReference> documentReferenceFuture = mock(ApiFuture.class);
            when(firestore.collection("shopping_lists")).thenReturn(shoppingListsCollectionRef);
            when(shoppingListsCollectionRef.add(any())).thenReturn(documentReferenceFuture);
            when(documentReferenceFuture.get()).thenReturn(shoppingListDocRef);

            return shoppingListsCollectionRef;
        }

        private void setupMockProductParsing() {
            ParsingResult parsingResult = mock(ParsingResult.class);
            ParsedProduct parsedProduct = new ParsedProduct("jabłka", 1.0, "kg", "1kg jabłek", false);
            when(parsingResult.getProduct()).thenReturn(parsedProduct);
            when(productParsingService.parseProduct(anyString())).thenReturn(parsingResult);
        }
    }

    @Nested
    @DisplayName("parseProductStrings")
    class ParseProductStringsTests {

        @Test
        @DisplayName("Should parse valid product strings correctly")
        void givenValidProductStrings_When_ParseProductStrings_Then_ParseCorrectly() {
            // Given
            List<String> productStrings = Arrays.asList("1kg jabłek", "2 sztuki bananów");
            String categoryId = "owoce";

            ParsingResult result1 = mock(ParsingResult.class);
            ParsedProduct product1 = new ParsedProduct("jabłka", 1.0, "kg", "1kg jabłek", false);
            when(result1.getProduct()).thenReturn(product1);

            ParsingResult result2 = mock(ParsingResult.class);
            ParsedProduct product2 = new ParsedProduct("banany", 2.0, "szt", "2 sztuki bananów", false);
            when(result2.getProduct()).thenReturn(product2);

            when(productParsingService.parseProduct("1kg jabłek")).thenReturn(result1);
            when(productParsingService.parseProduct("2 sztuki bananów")).thenReturn(result2);

            // When
            List<Map<String, Object>> result = dietManagerService.parseProductStrings(
                    productStrings, categoryId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0))
                    .containsEntry("name", "jabłka")
                    .containsEntry("quantity", 1.0)
                    .containsEntry("unit", "kg")
                    .containsEntry("categoryId", "owoce")
                    .containsEntry("original", "1kg jabłek")
                    .containsEntry("hasCustomUnit", false);
        }

        @Test
        @DisplayName("Should create default product when parsing fails")
        void givenUnparsableProductString_When_ParseProductStrings_Then_CreateDefaultProduct() {
            // Given
            List<String> productStrings = Collections.singletonList("invalid product format");
            String categoryId = "inne";

            ParsingResult result = mock(ParsingResult.class);
            when(result.getProduct()).thenReturn(null);
            when(productParsingService.parseProduct(anyString())).thenReturn(result);

            // When
            List<Map<String, Object>> parsedProducts = dietManagerService.parseProductStrings(
                    productStrings, categoryId);

            // Then
            assertThat(parsedProducts).hasSize(1);
            assertThat(parsedProducts.get(0))
                    .containsEntry("name", "invalid product format")
                    .containsEntry("quantity", 1.0)
                    .containsEntry("unit", "szt")
                    .containsEntry("categoryId", "inne")
                    .containsEntry("hasCustomUnit", false);
        }

        @Test
        @DisplayName("Should include product ID when present")
        void givenProductWithId_When_ParseProductStrings_Then_IncludeId() {
            // Given
            List<String> productStrings = Collections.singletonList("1kg jabłek");
            String categoryId = "owoce";

            ParsingResult result = mock(ParsingResult.class);
            ParsedProduct product = new ParsedProduct("jabłka", 1.0, "kg", "1kg jabłek", false);
            product.setId("product123");
            when(result.getProduct()).thenReturn(product);
            when(productParsingService.parseProduct(anyString())).thenReturn(result);

            // When
            List<Map<String, Object>> parsedProducts = dietManagerService.parseProductStrings(
                    productStrings, categoryId);

            // Then
            assertThat(parsedProducts.get(0)).containsEntry("id", "product123");
        }

        @Test
        @DisplayName("Should handle empty product strings list")
        void givenEmptyProductList_When_ParseProductStrings_Then_ReturnEmptyList() {
            // Given
            List<String> productStrings = Collections.emptyList();
            String categoryId = "owoce";

            // When
            List<Map<String, Object>> result = dietManagerService.parseProductStrings(
                    productStrings, categoryId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should preserve hasCustomUnit flag")
        void givenProductWithCustomUnit_When_ParseProductStrings_Then_PreserveCustomUnitFlag() {
            // Given
            List<String> productStrings = Collections.singletonList("1 pęczek pietruszki");
            String categoryId = "warzywa";

            ParsingResult result = mock(ParsingResult.class);
            ParsedProduct product = new ParsedProduct("pietruszka", 1.0, "pęczek", "1 pęczek pietruszki", true);
            when(result.getProduct()).thenReturn(product);
            when(productParsingService.parseProduct(anyString())).thenReturn(result);

            // When
            List<Map<String, Object>> parsedProducts = dietManagerService.parseProductStrings(
                    productStrings, categoryId);

            // Then
            assertThat(parsedProducts.get(0)).containsEntry("hasCustomUnit", true);
        }
    }

    @Nested
    @DisplayName("convertToRecipeIngredients")
    class ConvertToRecipeIngredientsTests {

        @Test
        @DisplayName("Should convert parsed products to recipe ingredients")
        void givenParsedProducts_When_ConvertToRecipeIngredients_Then_ConvertSuccessfully() {
            // Given
            ParsedProduct product1 = ParsedProduct.builder()
                    .name("mąka")
                    .quantity(500.0)
                    .unit("g")
                    .original("500g mąki")
                    .hasCustomUnit(false)
                    .categoryId("grains")
                    .build();

            ParsedProduct product2 = ParsedProduct.builder()
                    .name("mleko")
                    .quantity(250.0)
                    .unit("ml")
                    .original("250ml mleka")
                    .hasCustomUnit(false)
                    .categoryId("dairy")
                    .build();

            List<ParsedProduct> parsedProducts = Arrays.asList(product1, product2);

            // When
            List<RecipeIngredient> result = dietManagerService.convertToRecipeIngredients(parsedProducts);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0))
                    .satisfies(ingredient -> {
                        assertThat(ingredient.getId()).isNotNull();
                        assertThat(ingredient.getName()).isEqualTo("mąka");
                        assertThat(ingredient.getQuantity()).isEqualTo(500.0);
                        assertThat(ingredient.getUnit()).isEqualTo("g");
                        assertThat(ingredient.getOriginal()).isEqualTo("500g mąki");
                        assertThat(ingredient.getCategoryId()).isEqualTo("grains");
                        assertThat(ingredient.isHasCustomUnit()).isFalse();
                    });
        }

        @Test
        @DisplayName("Should return empty list when parsed products is null")
        void givenNullParsedProducts_When_ConvertToRecipeIngredients_Then_ReturnEmptyList() {
            // When
            List<RecipeIngredient> result = dietManagerService.convertToRecipeIngredients(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when parsed products is empty")
        void givenEmptyParsedProducts_When_ConvertToRecipeIngredients_Then_ReturnEmptyList() {
            // When
            List<RecipeIngredient> result = dietManagerService.convertToRecipeIngredients(Collections.emptyList());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate unique IDs for each ingredient")
        void givenMultipleParsedProducts_When_ConvertToRecipeIngredients_Then_GenerateUniqueIds() {
            // Given
            ParsedProduct product1 = ParsedProduct.builder()
                    .name("mąka")
                    .quantity(500.0)
                    .unit("g")
                    .original("500g mąki")
                    .hasCustomUnit(false)
                    .build();

            ParsedProduct product2 = ParsedProduct.builder()
                    .name("cukier")
                    .quantity(200.0)
                    .unit("g")
                    .original("200g cukru")
                    .hasCustomUnit(false)
                    .build();

            List<ParsedProduct> parsedProducts = Arrays.asList(product1, product2);

            // When
            List<RecipeIngredient> result = dietManagerService.convertToRecipeIngredients(parsedProducts);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isNotEqualTo(result.get(1).getId());
            assertThat(result).allMatch(ingredient -> ingredient.getId() != null);
        }
    }

    // Helper methods

    private ParsedDietData createTestParsedDietData() {
        ParsedDietData data = new ParsedDietData();

        // Create days
        List<ParsedDay> days = new ArrayList<>();

        // Day 1
        ParsedDay day1 = new ParsedDay();
        day1.setDate(Timestamp.ofTimeSecondsAndNanos(1646092800, 0)); // 2022-03-01

        List<ParsedMeal> meals1 = new ArrayList<>();

        // Meal 1 (breakfast)
        ParsedMeal breakfast = new ParsedMeal();
        breakfast.setName("Owsianka z owocami");
        breakfast.setMealType(MealType.BREAKFAST);
        breakfast.setTime("08:00");
        breakfast.setInstructions("Wymieszaj płatki owsiane z mlekiem i dodaj owoce");

        NutritionalValues breakfastNutrition = NutritionalValues.builder()
                .calories(350.0)
                .protein(10.0)
                .fat(5.0)
                .carbs(60.0)
                .build();
        breakfast.setNutritionalValues(breakfastNutrition);

        meals1.add(breakfast);
        day1.setMeals(meals1);
        days.add(day1);

        // Day 2
        ParsedDay day2 = new ParsedDay();
        day2.setDate(Timestamp.ofTimeSecondsAndNanos(1646179200, 0)); // 2022-03-02

        List<ParsedMeal> meals2 = new ArrayList<>();

        // Meal 2 (lunch)
        ParsedMeal lunch = new ParsedMeal();
        lunch.setName("Sałatka z kurczakiem");
        lunch.setMealType(MealType.LUNCH);
        lunch.setTime("13:00");
        lunch.setInstructions("Pokrój kurczaka i warzywa, wymieszaj z sosem");

        NutritionalValues lunchNutrition = NutritionalValues.builder()
                .calories(450.0)
                .protein(30.0)
                .fat(15.0)
                .carbs(30.0)
                .build();
        lunch.setNutritionalValues(lunchNutrition);

        meals2.add(lunch);
        day2.setMeals(meals2);
        days.add(day2);

        data.setDays(days);

        // Add products to shopping list
        Map<String, List<String>> categorizedProducts = new HashMap<>();
        categorizedProducts.put("owoce", Arrays.asList("1kg jabłek", "2 banany"));
        categorizedProducts.put("nabiał", Arrays.asList("1l mleka", "250g twarogu"));
        data.setCategorizedProducts(categorizedProducts);

        return data;
    }
}