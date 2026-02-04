package com.noisevisionsoftware.vitema.dto.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietIngredientDto {
    private String name;
    private double quantity;
    private String unit;
    private String productId;
    private String categoryId;
}
