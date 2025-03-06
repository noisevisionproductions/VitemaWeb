package com.noisevisionsoftware.nutrilog.utils;

import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern UNIT_PATTERN = Pattern.compile(
            "\\s*\\d*[.,]?\\d+\\s*(" +
                    "kg|g|ml|l|mm|cm|m|szt|op|litr|litry|litrow|gram|gramy|gramow|sztuk|sztuki|" +
                    "opak|sztuka|opakowanie|opakowania|opakowan)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public static String removeUnits(String productName) {
        if (productName == null) {
            return "";
        }
        return UNIT_PATTERN.matcher(productName.trim())
                .replaceAll("")
                .trim()
                .replaceAll("\\s+", " ");
    }

    public static boolean containsUnits(String productName) {
        if (productName == null) {
            return false;
        }
        return UNIT_PATTERN.matcher(productName).find();
    }
}