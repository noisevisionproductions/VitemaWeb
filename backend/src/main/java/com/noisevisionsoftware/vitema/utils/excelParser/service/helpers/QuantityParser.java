package com.noisevisionsoftware.vitema.utils.excelParser.service.helpers;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuantityParser {
    private static final Map<String, Double> PREFIX_MULTIPLIERS = Map.of(
            "pół", 0.5,
            "półtorej", 1.5,
            "ćwierć", 0.25,
            "półtora", 1.5,
            "jedna", 1.0,
            "jeden", 1.0,
            "dwa", 2.0,
            "trzy", 3.0,
            "cztery", 4.0,
            "pięć", 5.0
    );

    public Double parseQuantity(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String cleanInput = input.trim().toLowerCase();

        // Sprawdzanie przedrostków
        Double prefixMultiplier = PREFIX_MULTIPLIERS.get(cleanInput);
        if (prefixMultiplier != null) {
            return prefixMultiplier;
        }

        // Obsługa zakresów (np. "2-3")
        Matcher rangeMatcher = Pattern.compile("^(\\d+(?:[.,]\\d+)?)\\s*-\\s*(\\d+(?:[.,]\\d+)?)$").matcher(cleanInput);
        if (rangeMatcher.matches()) {
            try {
                double min = Double.parseDouble(rangeMatcher.group(1).replace(',', '.'));
                double max = Double.parseDouble(rangeMatcher.group(2).replace(',', '.'));
                return (min + max) / 2;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Obsługa ułamków (np. "1/2")
        Matcher fractionMatcher = Pattern.compile("^(\\d+)/(\\d+)$").matcher(cleanInput);
        if (fractionMatcher.matches()) {
            try {
                int numerator = Integer.parseInt(fractionMatcher.group(1));
                int denominator = Integer.parseInt(fractionMatcher.group(2));
                if (denominator == 0) {
                    return null;
                }
                return (double) numerator / denominator;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Obsługa liczb mieszanych (np. "1 1/2")
        Matcher mixedMatcher = Pattern.compile("^(\\d+)\\s+(\\d+)/(\\d+)$").matcher(cleanInput);
        if (mixedMatcher.matches()) {
            try {
                int wholeNumber = Integer.parseInt(mixedMatcher.group(1));
                int numerator = Integer.parseInt(mixedMatcher.group(2));
                int denominator = Integer.parseInt(mixedMatcher.group(3));
                if (denominator == 0) {
                    return null;
                }
                return wholeNumber + ((double) numerator / denominator);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Podstawowa obsługa liczby (np. "5")
        try {
            return Double.parseDouble(cleanInput.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}