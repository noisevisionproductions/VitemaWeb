package com.noisevisionsoftware.vitema.model.changelog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangelogEntryType {
    FEATURE("feature"),
    FIX("fix"),
    IMPROVEMENT("improvement");

    private final String value;

    ChangelogEntryType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ChangelogEntryType fromValue(String value) {
        for (ChangelogEntryType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Nieznany typ wpisu changelog: " + value);
    }
}
