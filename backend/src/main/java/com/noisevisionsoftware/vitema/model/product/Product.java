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
    private String searchName;
    private String defaultUnit;
    private NutritionalValues nutritionalValues;
    private ProductType type;
    private String authorId;
    private String categoryId;
    private Long createdAt;
}
