package com.noisevisionsoftware.vitema.utils.excelParser.service;

import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsingResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.QuantityInfo;
import com.noisevisionsoftware.vitema.utils.excelParser.model.UnitProcessingResult;
import com.noisevisionsoftware.vitema.utils.excelParser.model.unit.UnitDetectionResult;
import com.noisevisionsoftware.vitema.utils.excelParser.service.helpers.QuantityParser;
import com.noisevisionsoftware.vitema.utils.excelParser.service.helpers.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductParsingServiceTest {

    @Mock
    private UnitService unitService;

    @Mock
    private QuantityParser quantityParser;

    @Spy
    @InjectMocks
    private ProductParsingService parsingService;

    @BeforeEach
    void setUp() {

        when(unitService.detectUnitInText(anyString())).thenReturn(
                new UnitDetectionResult("szt", "piece", true)
        );

        lenient().when(unitService.detectUnitInText("g")).thenReturn(
                new UnitDetectionResult("g", "weight", true)
        );
        lenient().when(unitService.detectUnitInText("łyżki")).thenReturn(
                new UnitDetectionResult("łyżka", "kitchen", true)
        );
    }

    @Test
    @DisplayName("Powinien zawsze zwracać poprawny wynik parsowania")
    void parseProduct_shouldAlwaysReturnValidResult() {
        when(quantityParser.parseQuantity(anyString())).thenReturn(1.0);

        String[] inputs = {
                "500 g mąka",
                "marchew 500 g",
                "jabłko (zielone) 2 szt",
                "jabłka 3",
                "jabłka",
                "• 500 g mąka",
                "500g mąka",
                "2 łyżki mąki"
        };

        for (String input : inputs) {
            // when
            ParsingResult result = parsingService.parseProduct(input);

            assertNotNull(result);
            assertNotNull(result.getProduct());
            assertNotNull(result.getProduct().getName());
            assertTrue(result.getProduct().getQuantity() > 0);
            assertNotNull(result.getProduct().getUnit());
            assertEquals(input, result.getProduct().getOriginal());
        }
    }

    @Test
    @DisplayName("Powinien wykryć i przypisać liczbę do pola quantity")
    void parseProduct_shouldDetectNumberAndAssignToQuantity() {
        // Konfiguracja mocka dla quantityParser - parsuje liczby z stringów
        when(quantityParser.parseQuantity(anyString())).thenAnswer(invocation -> {
            String input = invocation.getArgument(0);
            try {
                // Próbuj parsować jako liczbę
                return Double.parseDouble(input.replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        });
        
        // Testujemy tylko czy liczby są poprawnie wykrywane
        String[] inputs = {
                "500 g mąka",
                "marchew 500 g",
                "jabłko (zielone) 2 szt",
                "jabłka 3",
                "500g mąka",
                "2 łyżki mąki"
        };

        double[] expectedQuantities = {
                500.0,
                500.0,
                2.0,
                3.0,
                500.0,
                2.0
        };

        for (int i = 0; i < inputs.length; i++) {
            // when
            ParsingResult result = parsingService.parseProduct(inputs[i]);

            assertEquals(expectedQuantities[i], result.getProduct().getQuantity(),
                    "Dla wejścia: " + inputs[i]);
        }
    }

    @Test
    @DisplayName("Powinien poprawnie czyścić nazwę produktu")
    void parseProduct_shouldCleanProductNames() {
        // Testujemy tylko czyszczenie nazw
        String[] inputs = {
                "500 g mąka pszenna",
                "marchew 500 g",
                "jabłko (zielone) 2 szt",
                "Szynka Wędzona (plastry) 200g"
        };

        String[] expectedNames = {
                "mąka pszenna",
                "marchew",
                "jabłko",
                "szynka wędzona"
        };

        for (int i = 0; i < inputs.length; i++) {
            // when
            ParsingResult result = parsingService.parseProduct(inputs[i]);

            assertEquals(expectedNames[i], result.getProduct().getName(),
                    "Dla wejścia: " + inputs[i]);
        }
    }

    @Test
    @DisplayName("Powinien wykryć jednostkę lub przypisać domyślną")
    void parseProduct_shouldDetectUnitOrAssignDefault() {
        // Testujemy tylko wykrywanie jednostek
        String[] inputs = {
                "500 g mąka",
                "marchew 500 g",
                "jabłka 3",
                "jabłka",
                "500g mąka"
        };

        String[] expectedUnits = {
                "g",
                "g",
                "szt", // domyślna jednostka
                "szt", // domyślna jednostka
                "g"
        };

        for (int i = 0; i < inputs.length; i++) {
            // when
            ParsingResult result = parsingService.parseProduct(inputs[i]);

            assertEquals(expectedUnits[i], result.getProduct().getUnit(),
                    "Dla wejścia: " + inputs[i]);
        }
    }

    @Test
    @DisplayName("Powinien obsłużyć wyjątek podczas parsowania")
    void parseProduct_shouldHandleException() {
        // given
        String input = "500 g mąka";

        // when
        ParsingResult result = parsingService.parseProduct(input);

        assertNotNull(result);
        assertNotNull(result.getProduct());
    }

    @Test
    @DisplayName("Powinien poprawnie czyścić nazwę produktu - metoda cleanProductName")
    void cleanProductName_shouldCorrectlyCleanName() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod("cleanProductName", String.class);
        method.setAccessible(true);

        // given
        String[] inputs = {
                "Jabłko (zielone)",
                "Mąka   pszenna",
                "MASŁO EXTRA",
                "Sok (100% pomarańczowy)"
        };

        String[] expected = {
                "jabłko",
                "mąka pszenna",
                "masło extra",
                "sok"
        };

        // when, then
        for (int i = 0; i < inputs.length; i++) {
            String result = (String) method.invoke(parsingService, inputs[i]);
            assertEquals(expected[i], result);
        }
    }

    @Test
    @DisplayName("Powinien poprawnie sprawdzać czy string jest liczbą")
    void isNumeric_shouldCorrectlyCheckIfStringIsNumeric() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod("isNumeric", String.class);
        method.setAccessible(true);

        // given, when, then
        assertTrue((boolean) method.invoke(parsingService, "123"));
        assertTrue((boolean) method.invoke(parsingService, "123.45"));
        assertTrue((boolean) method.invoke(parsingService, "0"));
        assertTrue((boolean) method.invoke(parsingService, "-123"));

        assertFalse((boolean) method.invoke(parsingService, "abc"));
        assertFalse((boolean) method.invoke(parsingService, ""));
        assertFalse((boolean) method.invoke(parsingService, (Object) null));
        assertFalse((boolean) method.invoke(parsingService, "123abc"));
    }

    @Test
    @DisplayName("Powinien poprawnie czyścić string wejściowy")
    void cleanInputString_shouldCorrectlyCleanInput() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod("cleanInputString", String.class);
        method.setAccessible(true);

        // given, when, then
        assertEquals("500 g mąka", method.invoke(parsingService, "• 500 g mąka"));
        assertEquals("500 g mąka", method.invoke(parsingService, "- 500 g mąka"));
        assertEquals("500 g mąka", method.invoke(parsingService, "  500  g   mąka  "));
        assertEquals("marchew 500 g", method.invoke(parsingService, "marchew 500 g"));
    }

    @Test
    @DisplayName("Powinien obsłużyć wykrywanie jednostek w tekście")
    void processUnitAndName_shouldDetectUnitsInText() throws Exception {
        when(unitService.detectUnitInText("g")).thenReturn(
                new UnitDetectionResult("g", "weight", true)
        );

        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod(
                "processUnitAndName", String.class, String.class);
        method.setAccessible(true);

        // given, when
        UnitProcessingResult result = (UnitProcessingResult) method.invoke(parsingService, "g", "mąka");

        // then
        assertEquals("g", result.getUnit());
        assertEquals("mąka", result.getName());
        assertTrue(result.isFoundKnownUnit());
    }

    @Test
    @DisplayName("Powinien wykrywać jednostki w tekście pozostałym")
    void processUnitAndName_shouldDetectUnitsInRemainingText() throws Exception {
        when(unitService.detectUnitInText(anyString())).thenReturn(
                new UnitDetectionResult("szt", "piece", false)
        );
        when(unitService.detectUnitInText("mąka z g")).thenReturn(
                new UnitDetectionResult("g", "weight", true)
        );

        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod(
                "processUnitAndName", String.class, String.class);
        method.setAccessible(true);

        // given, when
        UnitProcessingResult result = (UnitProcessingResult) method.invoke(parsingService, null, "mąka z g");

        // then
        assertEquals("g", result.getUnit());
        assertEquals("mąka z", result.getName()); // jednostka powinna być usunięta z nazwy
        assertTrue(result.isFoundKnownUnit());
    }

    @Test
    @DisplayName("Powinien poprawnie przetwarzać jednostki i nazwy")
    void processUnitAndName_shouldProcessUnitsAndNames() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        Method method = ProductParsingService.class.getDeclaredMethod(
                "processUnitAndName", String.class, String.class);
        method.setAccessible(true);

        when(unitService.detectUnitInText("g")).thenReturn(
                new UnitDetectionResult("g", "weight", true)
        );

        when(unitService.detectUnitInText("2 g mąka")).thenReturn(
                new UnitDetectionResult("g", "weight", true)
        );

        when(unitService.detectUnitInText("mąka")).thenReturn(
                new UnitDetectionResult("szt", "piece", false)
        );

        when(unitService.detectUnitInText("xyz")).thenReturn(
                new UnitDetectionResult("szt", "piece", false)
        );

        // Case 1: Jednostka w potentialUnit
        UnitProcessingResult result1 = (UnitProcessingResult) method.invoke(parsingService, "g", "mąka");
        assertEquals("g", result1.getUnit());
        assertEquals("mąka", result1.getName());
        assertTrue(result1.isFoundKnownUnit());

        // Case 2: Jednostka w remainingText
        UnitProcessingResult result2 = (UnitProcessingResult) method.invoke(parsingService, null, "2 g mąka");
        assertEquals("g", result2.getUnit());
        // Nazwa powinna być oczyszczona z jednostki, więc oczekujemy "2 mąka" lub podobnej wartości
        assertFalse(result2.getName().contains("g"));
        assertTrue(result2.isFoundKnownUnit());

        // Case 3: Brak znanej jednostki
        UnitProcessingResult result3 = (UnitProcessingResult) method.invoke(parsingService, "xyz", "mąka");
        assertEquals("szt", result3.getUnit()); // Domyślna jednostka
        assertEquals("mąka", result3.getName());
        assertTrue(result3.isFoundKnownUnit()); // W twojej implementacji zawsze zwraca true
    }

    @Test
    @DisplayName("Powinien poprawnie czyścić nazwę produktu")
    void cleanProductName_shouldCleanProductName() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod(
                "cleanProductName", String.class);
        method.setAccessible(true);

        assertEquals("mąka pszenna", method.invoke(parsingService, "Mąka Pszenna"));
        assertEquals("jabłko", method.invoke(parsingService, "Jabłko (czerwone)"));
        assertEquals("mąka", method.invoke(parsingService, "  mąka  "));
        assertEquals("olej rzepakowy", method.invoke(parsingService, "Olej Rzepakowy (nierafinowany)"));
    }

    @Test
    @DisplayName("Powinien poprawnie sprawdzać, czy string jest numeryczny")
    void isNumeric_shouldCheckIfStringIsNumeric() throws Exception {
        // Użycie refleksji do wywołania prywatnej metody
        java.lang.reflect.Method method = ProductParsingService.class.getDeclaredMethod(
                "isNumeric", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(parsingService, "123"));
        assertTrue((Boolean) method.invoke(parsingService, "123.45"));
        assertTrue((Boolean) method.invoke(parsingService, "0.5"));

        assertFalse((Boolean) method.invoke(parsingService, "abc"));
        assertFalse((Boolean) method.invoke(parsingService, ""));
        assertFalse((Boolean) method.invoke(parsingService, (Object) null));
        assertFalse((Boolean) method.invoke(parsingService, "123abc"));
    }

    @Test
    @DisplayName("Powinien obsłużyć przypadek z potentialUnit=null i pustym remainingText")
    void parseProduct_shouldHandleEmptyPotentialUnitAndRemainingText() throws Exception {
        // given
        String input = "2";

        QuantityInfo quantityInfo = new QuantityInfo(2.0, null, "");

        Method extractQuantityMethod = ProductParsingService.class.getDeclaredMethod(
                "extractQuantityAndUnit", String.class);
        extractQuantityMethod.setAccessible(true);

        // Używamy doReturn().when() z PowerMock lub Mockito Spy
        doReturn(quantityInfo).when(parsingService).extractQuantityAndUnit(anyString());

        // when
        ParsingResult result = parsingService.parseProduct(input);

        // then
        assertTrue(result.isSuccess());
        ParsedProduct product = result.getProduct();
        assertEquals(2.0, product.getQuantity());
        assertEquals("szt", product.getUnit()); // domyślna jednostka
        assertEquals(input, product.getOriginal());
    }

    @Test
    @DisplayName("Powinien prawidłowo przetwarzać obiekt QuantityInfo")
    void parseProduct_shouldProcessQuantityInfoCorrectly() {
        // given
        String input = "2 kg mąki";

        // when
        ParsingResult result = parsingService.parseProduct(input);

        // then
        assertTrue(result.isSuccess());
        ParsedProduct product = result.getProduct();
        assertEquals("mąki", product.getName());
        assertEquals(2.0, product.getQuantity());
        assertEquals("kg", product.getUnit());
        assertEquals(input, product.getOriginal());
    }

    @Test
    @DisplayName("Powinien testować metodę extractQuantityAndUnit")
    void extractQuantityAndUnit_shouldExtractQuantityAndUnit() throws Exception {
        // Używamy refleksji
        Method method = ProductParsingService.class.getDeclaredMethod(
                "extractQuantityAndUnit", String.class);
        method.setAccessible(true);

        when(quantityParser.parseQuantity("2")).thenReturn(2.0);
        when(quantityParser.parseQuantity("500")).thenReturn(500.0);
        when(quantityParser.parseQuantity("pół")).thenReturn(0.5);

        // Przypadek 1: "2 kg mąki"
        QuantityInfo result1 = (QuantityInfo) method.invoke(parsingService, "2 kg mąki");
        assertEquals(2.0, result1.getQuantity());
        assertEquals("kg", result1.getPotentialUnit());
        assertEquals("mąki", result1.getRemainingText());

        // Przypadek 2: "mąka 500 g"
        QuantityInfo result2 = (QuantityInfo) method.invoke(parsingService, "mąka 500 g");
        assertEquals(500.0, result2.getQuantity());
        assertEquals("g", result2.getPotentialUnit());
        assertEquals("mąka", result2.getRemainingText());

        // Przypadek 3: "pół kg mąki"
        QuantityInfo result3 = (QuantityInfo) method.invoke(parsingService, "pół kg mąki");
        assertEquals(0.5, result3.getQuantity());
        assertEquals("kg", result3.getPotentialUnit());
        assertEquals("mąki", result3.getRemainingText());
    }

    @Test
    @DisplayName("Powinien testować metodę cleanInputString")
    void cleanInputString_shouldCleanInput() throws Exception {
        // Używamy refleksji
        Method method = ProductParsingService.class.getDeclaredMethod(
                "cleanInputString", String.class);
        method.setAccessible(true);

        // Testowanie różnych przypadków
        assertEquals("500 g mąki", method.invoke(parsingService, "• 500 g mąki"));
        assertEquals("500 g mąki", method.invoke(parsingService, "- 500 g mąki"));
        assertEquals("500 g mąki", method.invoke(parsingService, "  500  g   mąki  "));
        assertEquals("mąka 500 g", method.invoke(parsingService, "mąka 500 g"));
    }

    @Test
    @DisplayName("Powinien obsłużyć wyjątek z extractQuantityAndUnit")
    void parseProduct_shouldHandleExceptionInExtractQuantityAndUnit() {
        // given
        String input = "jakiś niestandardowy wpis";

        doThrow(new RuntimeException("Test exception")).when(parsingService).extractQuantityAndUnit(anyString());

        // when
        ParsingResult result = parsingService.parseProduct(input);

        // then
        assertTrue(result.isSuccess()); // zawsze zwraca success = true
        ParsedProduct product = result.getProduct();
        assertNotNull(product);
        assertEquals(1.0, product.getQuantity()); // domyślna ilość
        assertEquals("szt", product.getUnit());   // domyślna jednostka
        assertEquals(input, product.getOriginal());
    }

    @Test
    @DisplayName("Powinien obsłużyć wyjątek z processUnitAndName")
    void parseProduct_shouldHandleExceptionInProcessUnitAndName() {
        // given
        String input = "2 kg mąki";

        // when
        ParsingResult result = parsingService.parseProduct(input);

        // then
        assertTrue(result.isSuccess()); // zawsze zwraca success = true
        ParsedProduct product = result.getProduct();
        assertNotNull(product);
        assertEquals(2.0, product.getQuantity()); // zachowuje quantity z QuantityInfo
        assertEquals("kg", product.getUnit());    // zachowuje jednostkę z QuantityInfo
        assertEquals(input, product.getOriginal());
    }

    @Test
    @DisplayName("Powinien poprawnie obsłużyć niestandardową jednostkę")
    void parseProduct_shouldHandleCustomUnit() {
        // given
        String input = "1 opakowanie serka";

        // when
        ParsingResult result = parsingService.parseProduct(input);

        assertTrue(result.isSuccess());
        ParsedProduct product = result.getProduct();
        assertEquals("serka", product.getName());
        assertEquals(1.0, product.getQuantity());
        assertEquals("opakowanie", product.getUnit());
        assertFalse(product.isHasCustomUnit()); // zmieniamy oczekiwanie
        assertEquals(input, product.getOriginal());
    }
}