package com.noisevisionsoftware.vitema.model.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NutritionalValues {
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
}

