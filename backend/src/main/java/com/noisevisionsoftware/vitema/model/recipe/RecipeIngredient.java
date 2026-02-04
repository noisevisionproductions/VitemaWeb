package com.noisevisionsoftware.vitema.model.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeIngredient {
    private String id;
    private String name;
    private Double quantity;
    private String unit;
    private String original;
    private String categoryId;
    private boolean hasCustomUnit;
    /** When set, macro calculation uses product; name is snapshot of product name. */
    private Long productId;
}
