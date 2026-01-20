package com.noisevisionsoftware.vitema.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpenFoodFactsNutriments {

    @JsonProperty("energy-kcal_100g")
    private Double energyKcal100g;

    @JsonProperty("proteins_100g")
    private Double proteins100g;

    @JsonProperty("carbohydrates_100g")
    private Double carbohydrates100g;

    @JsonProperty("fat_100g")
    private Double fat100g;

    @JsonProperty("fiber_100g")
    private Double fiber100g;

    @JsonProperty("sugars_100g")
    private Double sugars100g;

    @JsonProperty("salt_100g")
    private Double salt100g;

    @JsonProperty("sodium_100g")
    private Double sodium100g;
}
