package com.noisevisionsoftware.vitema.dto.product;

import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDTO {
    private String id;
    private String name;
    private String defaultUnit;
    private NutritionalValues nutritionalValues;
    private String categoryId;
    private String type;  // GLOBAL or CUSTOM
}
