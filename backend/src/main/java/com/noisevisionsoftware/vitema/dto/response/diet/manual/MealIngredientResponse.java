package com.noisevisionsoftware.vitema.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealIngredientResponse {

    private String id;
    private String name;
    private String quantity;
    private String unit;
    private String original;
    private String categoryId;
    private boolean hasCustomUnit;
}
