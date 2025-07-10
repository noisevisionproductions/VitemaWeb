package com.noisevisionsoftware.nutrilog.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateIngredientResponse {
    private String name;
    private Double quantity;
    private String unit;
    private String original;
    private String categoryId;
    private String categoryName;
    private boolean hasCustomUnit;

    private String displayText;
}