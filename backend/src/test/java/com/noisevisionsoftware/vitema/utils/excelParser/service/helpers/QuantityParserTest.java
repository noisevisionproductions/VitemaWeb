package com.noisevisionsoftware.vitema.utils.excelParser.service.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class QuantityParserTest {

    private QuantityParser parser;

    @BeforeEach
    void setUp() {
        parser = new QuantityParser();
    }

    @ParameterizedTest
    @DisplayName("Powinien zwrócić null dla pustych lub null wartości")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void parseQuantity_shouldReturnNullForEmptyOrNullInput(String input) {
        assertNull(parser.parseQuantity(input));
    }

    @ParameterizedTest
    @DisplayName("Powinien poprawnie parsować przedrostki tekstowe")
    @CsvSource({
            "pół, 0.5",
            "półtorej, 1.5",
            "ćwierć, 0.25",
            "półtora, 1.5",
            "jedna, 1.0",
            "jeden, 1.0",
            "dwa, 2.0",
            "trzy, 3.0",
            "cztery, 4.0",
            "pięć, 5.0"
    })
    void parseQuantity_shouldParseTextPrefixes(String input, double expected) {
        assertEquals(expected, parser.parseQuantity(input));
    }

    @ParameterizedTest
    @DisplayName("Powinien poprawnie parsować liczby dziesiętne")
    @CsvSource({
            "5, 5.0",
            "3.5, 3.5",
            "2.5, 2.5",
            "0.25, 0.25",
            "100, 100.0"
    })
    void parseQuantity_shouldParseDecimalNumbers(String input, double expected) {
        assertEquals(expected, parser.parseQuantity(input));
    }

    @ParameterizedTest
    @DisplayName("Powinien poprawnie parsować zakresy")
    @CsvSource({
            "2-3, 2.5",
            "1-2, 1.5",
            "0.5-1.5, 1.0",
            "0.5-1.5, 1.0",
            "10-20, 15.0"
    })
    void parseQuantity_shouldParseRanges(String input, double expected) {
        assertEquals(expected, parser.parseQuantity(input));
    }

    @ParameterizedTest
    @DisplayName("Powinien poprawnie parsować ułamki")
    @CsvSource({
            "1/2, 0.5",
            "1/4, 0.25",
            "3/4, 0.75",
            "1/3, 0.3333333333333333",
            "2/3, 0.6666666666666666"
    })
    void parseQuantity_shouldParseFractions(String input, double expected) {
        assertEquals(expected, parser.parseQuantity(input));
    }

    @ParameterizedTest
    @DisplayName("Powinien poprawnie parsować liczby mieszane z ułamkami")
    @CsvSource({
            "1 1/2, 1.5",
            "2 1/4, 2.25",
            "3 3/4, 3.75"
    })
    void parseQuantity_shouldParseMixedNumbers(String input, double expected) {
        assertEquals(expected, parser.parseQuantity(input));
    }

    @Test
    @DisplayName("Powinien zwrócić null dla niepoprawnego ułamka z zerem w mianowniku")
    void parseQuantity_shouldReturnNullForInvalidFractionWithZeroDenominator() {
        assertNull(parser.parseQuantity("1/0"));
    }

    @Test
    @DisplayName("Powinien zwrócić null dla niepoprawnego formatu liczby")
    void parseQuantity_shouldReturnNullForInvalidNumberFormat() {
        assertNull(parser.parseQuantity("abc"));
    }

    @Test
    @DisplayName("Powinien ignorować spacje przed i po wartości")
    void parseQuantity_shouldIgnoreLeadingAndTrailingSpaces() {
        assertEquals(5.0, parser.parseQuantity(" 5 "));
    }
}