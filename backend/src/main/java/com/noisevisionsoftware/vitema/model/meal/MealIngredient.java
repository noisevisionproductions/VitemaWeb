package com.noisevisionsoftware.vitema.model.meal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealIngredient {

    private String id;
    private String name;
    private Double quantity;
    private String unit;
    private String original;
    private String categoryId;
    private boolean hasCustomUnit;
}
