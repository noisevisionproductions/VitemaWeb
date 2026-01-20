package com.noisevisionsoftware.vitema.dto.response.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NutritionalValuesResponse {
    private double calories;
    private double protein;
    private double fat;
    private double carbs;
}