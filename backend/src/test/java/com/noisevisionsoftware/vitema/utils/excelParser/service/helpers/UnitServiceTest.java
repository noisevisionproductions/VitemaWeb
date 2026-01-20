package com.noisevisionsoftware.vitema.utils.excelParser.service.helpers;

import com.noisevisionsoftware.vitema.utils.excelParser.model.unit.ProductUnit;
import com.noisevisionsoftware.vitema.utils.excelParser.model.unit.UnitDetectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnitServiceTest {

    private UnitService unitService;

    @BeforeEach
    void setUp() {
        unitService = new UnitService();
    }

    @Test
    @DisplayName("Powinien znaleźć jednostkę po wartości")
    void getUnit_shouldFindUnitByValue() {
        // given, when
        Optional<ProductUnit> kilogram = unitService.getUnit("kg");
        Optional<ProductUnit> gram = unitService.getUnit("g");
        Optional<ProductUnit> liter = unitService.getUnit("l");
        Optional<ProductUnit> notExisting = unitService.getUnit("notExist");

        // then
        assertTrue(kilogram.isPresent());
        assertEquals("kilogram", kilogram.get().getLabel());
        assertEquals("weight", kilogram.get().getType());
        assertEquals("g", kilogram.get().getBaseUnit());
        assertEquals(1000.0, kilogram.get().getConversionFactor());

        assertTrue(gram.isPresent());
        assertEquals("gram", gram.get().getLabel());

        assertTrue(liter.isPresent());
        assertEquals("litr", liter.get().getLabel());

        assertFalse(notExisting.isPresent());
    }

    @Test
    @DisplayName("Powinien konwertować wartość do jednostki bazowej")
    void convertToBaseUnit_shouldConvertToBaseUnit() {
        // given, when
        Double grams = unitService.convertToBaseUnit(1.0, "kg");
        Double milliliters = unitService.convertToBaseUnit(1.0, "l");
        Double teaspoon = unitService.convertToBaseUnit(1.0, "łyżeczka");
        Double piece = unitService.convertToBaseUnit(1.0, "szt");

        // then
        assertEquals(1000.0, grams);
        assertEquals(1000.0, milliliters);
        assertEquals(5.0, teaspoon);
        assertNull(piece);
    }

    @Test
    @DisplayName("Powinien sprawdzać czy jednostka jest poprawna")
    void isValidUnit_shouldCheckIfUnitIsValid() {
        // given, when, then
        assertTrue(unitService.isValidUnit("kg"));
        assertTrue(unitService.isValidUnit("g"));
        assertTrue(unitService.isValidUnit("ml"));
        assertTrue(unitService.isValidUnit("szt"));
        assertTrue(unitService.isValidUnit("łyżka"));
        assertFalse(unitService.isValidUnit("nieznana"));
        assertFalse(unitService.isValidUnit(""));
        assertFalse(unitService.isValidUnit(null));
    }

    @Test
    @DisplayName("Powinien normalizować do jednostki bazowej")
    void normalizeToBaseUnit_shouldNormalizeToBaseUnit() {
        // given, when
        Optional<UnitService.NormalizedValue> kg = unitService.normalizeToBaseUnit(2.5, "kg");
        Optional<UnitService.NormalizedValue> g = unitService.normalizeToBaseUnit(500, "g");
        Optional<UnitService.NormalizedValue> l = unitService.normalizeToBaseUnit(0.5, "l");
        Optional<UnitService.NormalizedValue> spoon = unitService.normalizeToBaseUnit(2, "łyżka");
        Optional<UnitService.NormalizedValue> piece = unitService.normalizeToBaseUnit(3, "szt");

        // then
        assertTrue(kg.isPresent());
        assertEquals(2500.0, kg.get().value());
        assertEquals("g", kg.get().unit());

        assertTrue(g.isPresent());
        assertEquals(500.0, g.get().value());
        assertEquals("g", g.get().unit());

        assertTrue(l.isPresent());
        assertEquals(500.0, l.get().value());
        assertEquals("ml", l.get().unit());

        assertTrue(spoon.isPresent());
        assertEquals(30.0, spoon.get().value());
        assertEquals("ml", spoon.get().unit());

        assertFalse(piece.isPresent());
    }

    @ParameterizedTest
    @DisplayName("Powinien wykryć jednostkę w tekście - dokładne dopasowanie")
    @CsvSource({
            "gram, g, weight, true",
            "kilogram, kg, weight, true",
            "litr, l, volume, true",
            "łyżka, łyżka, kitchen, true",
            "łyżeczka, łyżeczka, kitchen, true",
            "sztuka, szt, piece, true"
    })
    void detectUnitInText_shouldDetectExactMatch(String input, String expectedUnit, String expectedType, boolean expectedMatch) {
        // given, when
        UnitDetectionResult result = unitService.detectUnitInText(input);

        // then
        assertEquals(expectedUnit, result.getUnit());
        assertEquals(expectedType, result.getType());
        assertEquals(expectedMatch, result.isMatch());
    }

    @ParameterizedTest
    @DisplayName("Powinien wykryć jednostkę w tekście - wzorce")
    @CsvSource({
            "100g mąki, g, weight, true",
            "2 kg ryżu, kg, weight, true",
            "200 ml mleka, ml, volume, true",
            "0.5 l wody, l, volume, true",
            "3 łyżki cukru, łyżka, kitchen, true",
            "2 łyżeczki soli, łyżeczka, kitchen, true",
            "5 szt jabłek, szt, piece, true"
    })
    void detectUnitInText_shouldDetectPatterns(String input, String expectedUnit, String expectedType, boolean expectedMatch) {
        // given, when
        UnitDetectionResult result = unitService.detectUnitInText(input);

        // then
        assertEquals(expectedUnit, result.getUnit());
        assertEquals(expectedType, result.getType());
        assertEquals(expectedMatch, result.isMatch());
    }

    @ParameterizedTest
    @DisplayName("Powinien wykryć jednostkę w tekście - częściowe dopasowanie")
    @CsvSource({
            "To jest 100 gramów, g, weight, true",
            "Potrzebuję 2 litry, l, volume, true",
            "Dodaj 3 łyżki cukru, łyżka, kitchen, true",
            "Wsyp 5 łyżeczek soli, łyżeczka, kitchen, true"
    })
    void detectUnitInText_shouldDetectPartialMatch(String input, String expectedUnit, String expectedType, boolean expectedMatch) {
        // given, when
        UnitDetectionResult result = unitService.detectUnitInText(input);

        // then
        assertEquals(expectedUnit, result.getUnit());
        assertEquals(expectedType, result.getType());
        assertEquals(expectedMatch, result.isMatch());
    }

    @ParameterizedTest
    @DisplayName("Powinien zwrócić domyślną jednostkę dla nieznanych wartości")
    @ValueSource(strings = {"nieznana jednostka", "zupełnie coś innego", "bla"})
    void detectUnitInText_shouldReturnDefaultForUnknown(String input) {
        // given, when
        UnitDetectionResult result = unitService.detectUnitInText(input);

        // then
        assertEquals("szt", result.getUnit());
        assertEquals("piece", result.getType());
        assertFalse(result.isMatch());
    }


    @Test
    @DisplayName("Powinien normalizować aliasy jednostek")
    void normalizeUnitAlias_shouldNormalizeUnitAliases() {
        // given, when, then
        assertEquals("g", unitService.normalizeUnitAlias("gram"));
        assertEquals("g", unitService.normalizeUnitAlias("gramów"));
        assertEquals("kg", unitService.normalizeUnitAlias("kilogram"));
        assertEquals("l", unitService.normalizeUnitAlias("litr"));
        assertEquals("ml", unitService.normalizeUnitAlias("mililitr"));
        assertEquals("szt", unitService.normalizeUnitAlias("sztuka"));
        assertEquals("łyżka", unitService.normalizeUnitAlias("łyżka"));
        assertEquals("łyżka", unitService.normalizeUnitAlias("łyżek"));
        assertEquals("łyżeczka", unitService.normalizeUnitAlias("łyżeczka"));
        assertEquals("nieznana", unitService.normalizeUnitAlias("nieznana"));
    }

    @Test
    @DisplayName("Powinien sprawdzać czy jednostki można łączyć")
    void canCombineQuantities_shouldCheckIfUnitsCanBeCombined() {
        // given, when, then
        assertTrue(unitService.canCombineQuantities("g", "g")); // dokładnie ta sama jednostka
        assertTrue(unitService.canCombineQuantities("g", "kg")); // ta sama jednostka bazowa
        assertTrue(unitService.canCombineQuantities("ml", "l")); // ta sama jednostka bazowa
        assertTrue(unitService.canCombineQuantities("łyżka", "łyżeczka")); // ta sama jednostka bazowa

        assertFalse(unitService.canCombineQuantities("g", "ml")); // różne jednostki bazowe
        assertFalse(unitService.canCombineQuantities("szt", "g")); // jednostka bez konwersji
        assertFalse(unitService.canCombineQuantities("nieznana", "g")); // nieznana jednostka
        assertFalse(unitService.canCombineQuantities("g", "nieznana")); // nieznana jednostka
    }
}