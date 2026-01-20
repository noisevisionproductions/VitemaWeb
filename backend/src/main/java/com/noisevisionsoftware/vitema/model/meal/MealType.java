package com.noisevisionsoftware.vitema.model.meal;

import lombok.Getter;

@Getter
public enum MealType {
    BREAKFAST("Śniadanie"),
    SECOND_BREAKFAST("Drugie śniadanie"),
    LUNCH("Obiad"),
    SNACK("Przekąska"),
    DINNER("Kolacja");

    private final String label;

    MealType(String label) {
        this.label = label;
    }

    public static String getMealTypeLabel(MealType mealType) {
        return mealType != null ? mealType.getLabel() : "";
    }
}