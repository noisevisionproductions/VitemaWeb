package com.noisevisionsoftware.nutrilog.model.diet.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateIngredient {
    private String name;
    private Double quantity;
    private String unit;
    private String original;
    private String categoryId;
    private boolean hasCustomUnit;
}
