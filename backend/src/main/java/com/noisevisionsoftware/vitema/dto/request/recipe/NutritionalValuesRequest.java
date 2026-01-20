package com.noisevisionsoftware.vitema.dto.request.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionalValuesRequest {
    private double calories;
    private double protein;
    private double fat;
    private double carbs;
}
