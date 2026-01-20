package com.noisevisionsoftware.vitema.model.diet.template;

import lombok.Getter;

@Getter
public enum DietTemplateCategory {
    WEIGHT_LOSS("Odchudzanie"),

    WEIGHT_GAIN("Nabieranie masy"),

    MAINTENANCE("Utrzymanie wagi"),

    SPORT("Sportowa"),

    MEDICAL("Medyczna"),

    VEGETARIAN("Wegetariańska"),

    VEGAN("Wegańska"),

    CUSTOM("Niestandardowa");

    private final String label;

    DietTemplateCategory(String label) {
        this.label = label;
    }

}
