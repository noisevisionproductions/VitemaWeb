package com.noisevisionsoftware.vitema.model.product;

import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String searchName;  // lowercase/normalized for search
    private String defaultUnit;  // e.g., "g", "ml", "szt"
    private NutritionalValues nutritionalValues;  // per 100g/100ml
    private ProductType type;
    private String authorId;  // null for GLOBAL products
    private String categoryId;
    private Long createdAt;
}
