package com.noisevisionsoftware.nutrilog.model.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionalValues {
    private double calories;
    private double protein;
    private double fat;
    private double carbs;
}

