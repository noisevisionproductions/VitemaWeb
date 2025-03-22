package com.noisevisionsoftware.nutrilog.model.recipe.jpa;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalValuesEntity {

    private double calories;
    private double protein;
    private double fat;
    private double carbs;
}