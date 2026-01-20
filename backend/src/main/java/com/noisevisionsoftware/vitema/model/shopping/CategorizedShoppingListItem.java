package com.noisevisionsoftware.vitema.model.shopping;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategorizedShoppingListItem {
    private String name;
    private double quantity;
    private String unit;
    private String original;
    private List<ShoppingListRecipeReference> recipes;
}