package com.noisevisionsoftware.vitema.service;

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
import com.noisevisionsoftware.vitema.model.recipe.RecipeReference;
import com.noisevisionsoftware.vitema.repository.recipe.RecipeRepository;
import com.noisevisionsoftware.vitema.service.diet.DietManagerService;
import com.noisevisionsoftware.vitema.service.diet.DietService;
import com.noisevisionsoftware.vitema.utils.excelParser.model.*;
import com.noisevisionsoftware.vitema.utils.excelParser.service.ProductParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private String testUserId;
    private String testDietId;
    private DietFileInfo testFileInfo;
    private ParsedDietData testParsedDietData;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
        testDietId = "diet123";
        testFileInfo = new DietFileInfo("test_diet.xlsx", "https://storage.example.com/test_diet.xlsx");

        testParsedDietData = createTestParsedDietData();
    }

    @Test
    void saveDietWithShoppingList_ShouldSaveDietAndReturnId() throws Exception {
        // given
        DocumentReference dietDocRef = mock(DocumentReference.class);
        CollectionReference dietsCollectionRef = mock(CollectionReference.class);
        when(firestore.collection("diets")).thenReturn(dietsCollectionRef);
        when(dietsCollectionRef.document()).thenReturn(dietDocRef);
        when(dietDocRef.getId()).thenReturn(testDietId);
        doNothing().when(recipeRepository).saveReference(any(RecipeReference.class));

        // Mockowanie ApiFuture i WriteResult
        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> writeResultFuture = mock(ApiFuture.class);
        WriteResult writeResult = mock(WriteResult.class);
        when(writeResultFuture.get()).thenReturn(writeResult);
        when(dietDocRef.set(any())).thenReturn(writeResultFuture);
        when(dietDocRef.update(eq("days"), any())).thenReturn(writeResultFuture);

        // Mockowanie shopping_lists
        CollectionReference shoppingListsCollectionRef = mock(CollectionReference.class);
        DocumentReference shoppingListDocRef = mock(DocumentReference.class);

        @SuppressWarnings("unchecked")
        ApiFuture<DocumentReference> documentReferenceFuture = mock(ApiFuture.class);
        when(firestore.collection("shopping_lists")).thenReturn(shoppingListsCollectionRef);
        when(shoppingListsCollectionRef.add(any())).thenReturn(documentReferenceFuture);
        when(documentReferenceFuture.get()).thenReturn(shoppingListDocRef);

        // Mockowanie FirestoreDietMapper
        Map<String, Object> dietMap = new HashMap<>();
        when(firestoreMapper.toFirestoreMap(any(Diet.class))).thenReturn(dietMap);

        // Mockowanie ProductParsingService
        ParsingResult parsingResult = mock(ParsingResult.class);
        when(productParsingService.parseProduct(anyString())).thenReturn(parsingResult);

        ParsedProduct parsedProduct = new ParsedProduct("jabłka", 1.0, "kg", "1kg jabłek", false);
        when(parsingResult.getProduct()).thenReturn(parsedProduct);

        Recipe testRecipe = Recipe.builder()
                .id("recipe123")
                .name("Test Recipe")
                .build();
        when(recipeService.findOrCreateRecipe(any(Recipe.class))).thenReturn(testRecipe);

        // when
        String result = dietManagerService.saveDietWithShoppingList(testParsedDietData, testUserId, testFileInfo);

        // then
        assertEquals(testDietId, result);

        // Weryfikacja zapisania diety
        verify(dietDocRef).set(any());
        verify(dietDocRef).update(eq("days"), any());

        // Weryfikacja zapisania przepisów
        verify(recipeService, times(testParsedDietData.getDays().stream()
                .mapToInt(day -> day.getMeals().size())
                .sum())).findOrCreateRecipe(any(Recipe.class));

        // Weryfikacja zapisania listy zakupów
        verify(shoppingListsCollectionRef).add(argThat(map ->
                map != null &&
                        testDietId.equals(map.get("dietId")) &&
                        testUserId.equals(map.get("userId")) &&
                        map.containsKey("items") &&
                        map.containsKey("createdAt") &&
                        map.containsKey("version")
        ));

        // Weryfikacja odświeżenia cache
        verify(recipeService).refreshRecipesCache();
        verify(dietService).refreshDietsCache();
    }

    @Test
    void saveDietWithShoppingList_WhenExceptionOccurs_ShouldThrowRuntimeException() {
        // given
        when(firestore.collection("diets")).thenThrow(new RuntimeException("Test exception"));

        // when & then
        assertThrows(RuntimeException.class, () ->
                dietManagerService.saveDietWithShoppingList(testParsedDietData, testUserId, testFileInfo)
        );

        // Weryfikacja, że cache nie został odświeżony
        verify(recipeService, never()).refreshRecipesCache();
        verify(dietService, never()).refreshDietsCache();
    }

    @Test
    void parseProductStrings_ShouldParseProductsCorrectly() throws Exception {
        // given
        List<String> productStrings = Arrays.asList("1kg jabłek", "2 sztuki bananów");
        String categoryId = "owoce";

        // Mock produktów
        ParsingResult result1 = mock(ParsingResult.class);
        ParsedProduct product1 = new ParsedProduct("jabłka", 1.0, "kg", "1kg jabłek", false);
        when(result1.getProduct()).thenReturn(product1);

        ParsingResult result2 = mock(ParsingResult.class);
        ParsedProduct product2 = new ParsedProduct("banany", 2.0, "szt", "2 sztuki bananów", false);
        when(result2.getProduct()).thenReturn(product2);

        when(productParsingService.parseProduct("1kg jabłek")).thenReturn(result1);
        when(productParsingService.parseProduct("2 sztuki bananów")).thenReturn(result2);

        // Dostęp do metody prywatnej przez refleksję
        java.lang.reflect.Method method = DietManagerService.class.getDeclaredMethod(
                "parseProductStrings", List.class, String.class);
        method.setAccessible(true);

        // when
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(
                dietManagerService, productStrings, categoryId);

        // then
        assertEquals(2, result.size());
        assertEquals("jabłka", result.getFirst().get("name"));
        assertEquals(1.0, result.getFirst().get("quantity"));
        assertEquals("kg", result.get(0).get("unit"));
        assertEquals("owoce", result.get(0).get("categoryId"));

        assertEquals("banany", result.get(1).get("name"));
        assertEquals(2.0, result.get(1).get("quantity"));
        assertEquals("szt", result.get(1).get("unit"));
        assertEquals("owoce", result.get(1).get("categoryId"));
    }

    @Test
    void parseProductStrings_WhenProductParsingFails_ShouldCreateDefaultProduct() throws Exception {
        // given
        List<String> productStrings = Collections.singletonList("niewłaściwy format produktu");
        String categoryId = "inne";

        ParsingResult result = mock(ParsingResult.class);
        when(result.getProduct()).thenReturn(null); // Parsowanie nie powiodło się
        when(productParsingService.parseProduct(anyString())).thenReturn(result);

        // Dostęp do metody prywatnej przez refleksję
        java.lang.reflect.Method method = DietManagerService.class.getDeclaredMethod(
                "parseProductStrings", List.class, String.class);
        method.setAccessible(true);

        // when
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result2 = (List<Map<String, Object>>) method.invoke(
                dietManagerService, productStrings, categoryId);

        // then
        assertEquals(1, result2.size());
        assertEquals("niewłaściwy format produktu", result2.getFirst().get("name"));
        assertEquals(1.0, result2.getFirst().get("quantity"));
        assertEquals("szt", result2.getFirst().get("unit"));
        assertEquals("inne", result2.getFirst().get("categoryId"));
    }

    private ParsedDietData createTestParsedDietData() {
        ParsedDietData data = new ParsedDietData();

        // Tworzenie dni
        List<ParsedDay> days = new ArrayList<>();

        // Dzień 1
        ParsedDay day1 = new ParsedDay();
        day1.setDate(Timestamp.ofTimeSecondsAndNanos(1646092800, 0)); // 2022-03-01

        List<ParsedMeal> meals1 = new ArrayList<>();

        // Posiłek 1 (śniadanie)
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

        // Dzień 2
        ParsedDay day2 = new ParsedDay();
        day2.setDate(Timestamp.ofTimeSecondsAndNanos(1646179200, 0)); // 2022-03-02

        List<ParsedMeal> meals2 = new ArrayList<>();

        // Posiłek 2 (obiad)
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

        // Dodanie produktów do listy zakupów
        Map<String, List<String>> categorizedProducts = new HashMap<>();
        categorizedProducts.put("owoce", Arrays.asList("1kg jabłek", "2 banany"));
        categorizedProducts.put("nabiał", Arrays.asList("1l mleka", "250g twarogu"));
        data.setCategorizedProducts(categorizedProducts);

        return data;
    }
}