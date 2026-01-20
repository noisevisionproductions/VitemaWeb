package com.noisevisionsoftware.vitema.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern UNIT_PATTERN = Pattern.compile(
            "\\s*\\d+[.,]?\\d*\\s*(?:kilogram(?:y|ów)?|dekagram(?:y|ów)?|gram(?:y|ów)?|litr(?:y|ów)?|sztuk(?:i|a)?|opakow(?:anie|ania|ań)?|szt|kg|g|ml|l|mm|cm|m)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public static String removeUnits(String productName) {
        if (productName == null || productName.isEmpty()) {
            return "";
        }

        String result = productName.trim();
        Matcher matcher = UNIT_PATTERN.matcher(result);
        result = matcher.replaceAll("");

        result = result.trim().replaceAll("\\s+", " ");

        return result;
    }
}