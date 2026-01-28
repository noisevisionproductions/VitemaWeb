package com.noisevisionsoftware.vitema.model.newsletter;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubscriberRole {
    DIETITIAN,
    COMPANY,
    FREELANCER,
    STUDIO;

    @JsonValue
    public String getValue() {
        return this.name();
    }
}