package com.noisevisionsoftware.vitema.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngredientSuggestion {

    private String name;
    private Double quantity;
    private String unit;
    private String original;
}
