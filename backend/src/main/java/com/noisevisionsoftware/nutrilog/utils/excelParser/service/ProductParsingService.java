package com.noisevisionsoftware.nutrilog.utils.excelParser.service;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsingResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.QuantityInfo;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.UnitProcessingResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.unit.UnitDetectionResult;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.helpers.QuantityParser;
import com.noisevisionsoftware.nutrilog.utils.excelParser.service.helpers.UnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductParsingService {

    private final UnitService unitService;
    private final QuantityParser quantityParser;

    // Wzorce dla różnych formatów ilości
    private static final List<Pattern> QUANTITY_PATTERNS = Arrays.asList(
            // Podstawowe wzorce, które już masz
            Pattern.compile("^(pół|półtorej|ćwierć|jedna|jeden|dwa|trzy|cztery|pięć|\\d+(?:[.,]\\d+)?(?:\\s*-\\s*\\d+(?:[.,]\\d+)?)?|\\d+/\\d+|\\d+\\s+\\d+/\\d+)\\s*([^\\d\\s]+)?\\s*(.+)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^(.+?)\\s+(pół|półtorej|ćwierć|jedna|jeden|dwa|trzy|cztery|pięć|\\d+(?:[.,]\\d+)?(?:\\s*-\\s*\\d+(?:[.,]\\d+)?)?|\\d+/\\d+|\\d+\\s+\\d+/\\d+)\\s*([^\\d\\s]+)?$", Pattern.CASE_INSENSITIVE),

            // Dodatkowe wzorce dla większej elastyczności
            Pattern.compile("^(\\d+)\\s*([^\\d\\s]+)\\s+(.+)$", Pattern.CASE_INSENSITIVE),  // "600 g marchwi"
            Pattern.compile("^(.+?)\\s+(\\d+)\\s*([^\\d\\s]+)$", Pattern.CASE_INSENSITIVE)  // "marchew 600 g"
    );

    public ParsingResult parseProduct(String input) {
        try {
            String cleanInput = cleanInputString(input);

            Pattern directPattern1 = Pattern.compile("^(\\d+)\\s*([a-ząćęłńóśźż]+)\\s+(.+)$", Pattern.CASE_INSENSITIVE);
            Matcher directMatcher1 = directPattern1.matcher(cleanInput);
            if (directMatcher1.matches()) {
                double quantity = Double.parseDouble(directMatcher1.group(1));
                String unit = directMatcher1.group(2).toLowerCase();
                String name = directMatcher1.group(3);

                return new ParsingResult(new ParsedProduct(
                        cleanProductName(name),
                        quantity,
                        unit,
                        input,
                        false
                ));
            }

            Pattern directPattern2 = Pattern.compile("^(.+?)\\s+(\\d+)\\s*([a-ząćęłńóśźż]+)$", Pattern.CASE_INSENSITIVE);
            Matcher directMatcher2 = directPattern2.matcher(cleanInput);
            if (directMatcher2.matches()) {
                String name = directMatcher2.group(1);
                double quantity = Double.parseDouble(directMatcher2.group(2));
                String unit = directMatcher2.group(3).toLowerCase();

                return new ParsingResult(new ParsedProduct(
                        cleanProductName(name),
                        quantity,
                        unit,
                        input,
                        false
                ));
            }

            QuantityInfo quantityInfo = extractQuantityAndUnit(cleanInput);

            if (!quantityInfo.isSuccess() || quantityInfo.getQuantity() == null || quantityInfo.getRemainingText() == null) {

                Pattern numberPattern = Pattern.compile("(\\d+(?:[.,]\\d+)?)");
                Matcher numberMatcher = numberPattern.matcher(cleanInput);
                if (numberMatcher.find()) {
                    double quantity = Double.parseDouble(numberMatcher.group(1).replace(',', '.'));
                    return new ParsingResult(new ParsedProduct(
                            cleanProductName(cleanInput.replace(numberMatcher.group(1), "").trim()),
                            quantity,
                            "szt",
                            input,
                            false
                    ));
                }

                return new ParsingResult(new ParsedProduct(
                        cleanProductName(cleanInput),
                        1.0,
                        "szt",
                        input,
                        false
                ));
            }

            double quantity = quantityInfo.getQuantity();
            String potentialUnit = quantityInfo.getPotentialUnit();
            String remainingText = quantityInfo.getRemainingText();

            if ((potentialUnit == null || potentialUnit.isEmpty()) && remainingText.isEmpty()) {
                String[] nameParts = cleanInput.split(" ");
                StringBuilder nameBuilder = new StringBuilder();
                for (String part : nameParts) {
                    if (!isNumeric(part)) {
                        nameBuilder.append(part).append(" ");
                    }
                }

                return new ParsingResult(new ParsedProduct(
                        cleanProductName(nameBuilder.toString().trim()),
                        quantity,
                        "szt",
                        input,
                        false
                ));
            }

            UnitProcessingResult processedUnit = processUnitAndName(
                    potentialUnit,
                    remainingText
            );

            // Jeśli nie można znormalizować, zwróć oryginalne wartości
            return new ParsingResult(new ParsedProduct(
                    cleanProductName(processedUnit.getName()),
                    quantity,
                    processedUnit.getUnit(),
                    input,
                    !processedUnit.isFoundKnownUnit() && unitService.isValidUnit(processedUnit.getUnit())
            ));
        } catch (Exception e) {
            return new ParsingResult(new ParsedProduct(
                    cleanInputString(input),
                    1.0,
                    "szt",
                    input,
                    false
            ));
        }
    }

    private String cleanInputString(String input) {
        return input
                .trim()
                .replaceAll("^[•\\-]\\s*", "")  // Usuń znaki wypunktowania
                .replaceAll("\\s+", " ");       // Normalizuj spacje
    }

    private QuantityInfo extractQuantityAndUnit(String input) {
        for (Pattern pattern : QUANTITY_PATTERNS) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String quantityStr;
                String potentialUnit;
                String remainingText;

                if (pattern == QUANTITY_PATTERNS.getFirst()) {
                    // Format "2-3 kg mąki"
                    quantityStr = matcher.group(1);
                    potentialUnit = matcher.group(2) != null ? matcher.group(2).toLowerCase() : null;
                    remainingText = matcher.group(3);
                } else {
                    // Format "mąka 2-3 kg"
                    quantityStr = matcher.group(2);
                    potentialUnit = matcher.group(3) != null ? matcher.group(3).toLowerCase() : null;
                    remainingText = matcher.group(1);
                }

                Double quantity = quantityParser.parseQuantity(quantityStr);
                if (quantity == null) continue;

                if (remainingText == null && potentialUnit != null) {
                    remainingText = potentialUnit;
                    potentialUnit = null;
                }

                if (remainingText != null) {
                    return new QuantityInfo(quantity, potentialUnit, remainingText.trim());
                }
            }
        }

        return new QuantityInfo(false);
    }

    private UnitProcessingResult processUnitAndName(String potentialUnit, String remainingText) {
        String unit = "";
        String name = remainingText;
        boolean foundKnownUnit = false;

        // Najpierw sprawdź potencjalną jednostkę
        if (potentialUnit != null && !potentialUnit.isEmpty()) {
            UnitDetectionResult unitInfo = unitService.detectUnitInText(potentialUnit);
            if (unitInfo.isMatch()) {
                unit = unitInfo.getUnit();
                foundKnownUnit = true;
            }
        }

        // Jeśli nie znaleziono jednostki w potencjalnej jednostce,
        // sprawdź w pozostałym tekście
        if (!foundKnownUnit && remainingText != null && !remainingText.isEmpty()) {
            UnitDetectionResult unitInfo = unitService.detectUnitInText(remainingText);
            if (unitInfo.isMatch()) {
                unit = unitInfo.getUnit();
                // Usuwanie jednostki z nazwy produktu
                name = remainingText.replaceAll("(?i)\\b" + Pattern.quote(unit) + "\\b", "").trim();
            }
        }

        // Ustaw jednostkę domyślną, tylko jeśli naprawdę nie znaleziono żadnej innej
        if (unit.isEmpty()) {
            unit = "szt";
        }

        return new UnitProcessingResult(unit, name, true);
    }

    private String cleanProductName(String name) {
        return name
                .replaceAll("\\s*\\([^)]+\\)", "")  // Usuń nawiasy z zawartością
                .replaceAll("\\s+", " ")           // Normalizuj spacje
                .trim()
                .toLowerCase();                    // Konwertuj na małe litery dla spójności
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}