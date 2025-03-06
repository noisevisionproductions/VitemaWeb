package com.noisevisionsoftware.nutrilog.utils.excelParser.service.helpers;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.unit.ProductUnit;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.unit.UnitDetectionResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UnitService {

    private static final List<ProductUnit> UNITS = Arrays.asList(
            // Jednostki wagi
            new ProductUnit("g", "gram", "weight", "g", 1.0),
            new ProductUnit("kg", "kilogram", "weight", "g", 1000.0),
            new ProductUnit("dag", "dekagram", "weight", "g", 10.0),

            // Jednostki objętości
            new ProductUnit("ml", "mililitr", "volume", "ml", 1.0),
            new ProductUnit("l", "litr", "volume", "ml", 1000.0),

            // Jednostki sztukowe
            new ProductUnit("szt", "sztuka", "piece"),
            new ProductUnit("opak", "opakowanie", "piece"),

            // Jednostki kuchenne
            new ProductUnit("łyżka", "łyżka", "kitchen", "ml", 15.0),
            new ProductUnit("łyżeczka", "łyżeczka", "kitchen", "ml", 5.0),
            new ProductUnit("szklanka", "szklanka", "kitchen", "ml", 250.0),
            new ProductUnit("garść", "garść", "kitchen", "g", 30.0)
    );

    private static final List<Map<String, Object>> UNIT_PATTERNS = Arrays.asList(
            Map.of(
                    "pattern", Pattern.compile("\\b(\\d+(?:[.,]\\d+)?)\\s*(kg|g|dag|dkg)\\b", Pattern.CASE_INSENSITIVE),
                    "type", "weight"
            ),
            Map.of(
                    "pattern", Pattern.compile("\\b(\\d+(?:[.,]\\d+)?)\\s*(ml|l|litr(?:y|ów)?)\\b", Pattern.CASE_INSENSITIVE),
                    "type", "volume"
            ),
            Map.of(
                    "pattern", Pattern.compile("\\b(\\d+(?:[.,]\\d+)?)\\s*(szt|sztuk[ia]?|opak(?:owanie)?)\\b", Pattern.CASE_INSENSITIVE),
                    "type", "piece"
            ),
            Map.of(
                    "pattern", Pattern.compile("\\b(\\d+(?:[.,]\\d+)?)\\s*(łyżk[ai]|łyżeczk[ai]|szklank[ai]|garść|garści)\\b", Pattern.CASE_INSENSITIVE),
                    "type", "kitchen"
            )
    );

    private static final Map<String, String> UNIT_ALIASES = new HashMap<>() {{
        // Waga
        put("gram", "g");
        put("gramów", "g");
        put("kilogram", "kg");
        put("kilogramów", "kg");
        put("dekagram", "dag");
        put("dekagramów", "dag");
        put("deko", "dag");
        put("dkg", "dag");

        // Objętość
        put("mililitr", "ml");
        put("mililitrów", "ml");
        put("litr", "l");
        put("litrów", "l");

        // Opakowania
        put("sztuka", "szt");
        put("sztuk", "szt");
        put("opakowanie", "opak");
        put("opakowań", "opak");
        put("opakowania", "opak");
        put("op.", "opak");

        // Miary kuchenne
        put("łyżka", "łyżka");
        put("łyżek", "łyżka");
        put("łyżki", "łyżka");
        put("łyżeczka", "łyżeczka");
        put("łyżeczek", "łyżeczka");
        put("łyżeczki", "łyżeczka");
        put("szklanka", "szklanka");
        put("szklanek", "szklanka");
        put("szklanki", "szklanka");
        put("garść", "garść");
        put("garści", "garść");
    }};

    public Optional<ProductUnit> getUnit(String value) {
        return UNITS.stream()
                .filter(unit -> unit.getValue().equals(value))
                .findFirst();
    }

    public Double convertToBaseUnit(double value, String fromUnit) {
        return getUnit(fromUnit)
                .filter(unit -> unit.getConversionFactor() != null && unit.getBaseUnit() != null)
                .map(unit -> value * unit.getConversionFactor())
                .orElse(null);
    }

    public boolean isValidUnit(String unit) {
        return UNITS.stream().anyMatch(u -> u.getValue().equals(unit));
    }

    public record NormalizedValue(double value, String unit) {
    }

    public Optional<NormalizedValue> normalizeToBaseUnit(double value, String unit) {
        return getUnit(unit)
                .filter(u -> u.getBaseUnit() != null && u.getConversionFactor() != null)
                .map(u -> new NormalizedValue(
                        value * u.getConversionFactor(),
                        u.getBaseUnit()
                ));
    }

    // W metodzie detectUnitInText w klasie UnitService
    public UnitDetectionResult detectUnitInText(String text) {
        if (text == null || text.isEmpty()) {
            return new UnitDetectionResult("", "piece", false);
        }

        // Próbujemy znaleźć dokładne dopasowanie najpierw
        String lowerText = text.toLowerCase();
        if (UNIT_ALIASES.containsKey(lowerText)) {
            String normalizedUnit = UNIT_ALIASES.get(lowerText);
            Optional<ProductUnit> unit = getUnit(normalizedUnit);
            if (unit.isPresent()) {
                return new UnitDetectionResult(normalizedUnit, unit.get().getType(), true);
            }
        }

        // Jeśli nie znajdziemy dokładnego dopasowania, szukamy jednostki w tekście
        for (Map<String, Object> patternInfo : UNIT_PATTERNS) {
            Pattern pattern = (Pattern) patternInfo.get("pattern");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String detectedUnit = matcher.group(2).toLowerCase();
                String normalizedUnit = normalizeUnitAlias(detectedUnit);

                Optional<ProductUnit> validUnit = getUnit(normalizedUnit);
                if (validUnit.isPresent()) {
                    return new UnitDetectionResult(
                            normalizedUnit,
                            validUnit.get().getType(),
                            true
                    );
                }
            }
        }

        // Jako ostateczność, sprawdzamy czy tekst zawiera jakąkolwiek znaną jednostkę
        for (String alias : UNIT_ALIASES.keySet()) {
            if (lowerText.contains(alias)) {
                String normalizedUnit = UNIT_ALIASES.get(alias);
                Optional<ProductUnit> unit = getUnit(normalizedUnit);
                if (unit.isPresent()) {
                    return new UnitDetectionResult(normalizedUnit, unit.get().getType(), true);
                }
            }
        }

        return new UnitDetectionResult("szt", "piece", false);
    }

    public String normalizeUnitAlias(String unit) {
        return UNIT_ALIASES.getOrDefault(unit.toLowerCase(), unit.toLowerCase());
    }

    public boolean canCombineQuantities(String unit1, String unit2) {
        Optional<ProductUnit> unitInfo1 = getUnit(unit1);
        Optional<ProductUnit> unitInfo2 = getUnit(unit2);

        if (unitInfo1.isEmpty() || unitInfo2.isEmpty()) {
            return false;
        }

        if (unit1.equals(unit2)) {
            return true;
        }

        String baseUnit1 = unitInfo1.get().getBaseUnit();
        String baseUnit2 = unitInfo2.get().getBaseUnit();

        if (baseUnit1 == null || baseUnit2 == null) {
            return false;
        }

        return baseUnit1.equals(baseUnit2);
    }
}
