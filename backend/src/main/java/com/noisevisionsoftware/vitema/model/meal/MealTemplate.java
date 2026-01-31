package com.noisevisionsoftware.vitema.model.meal;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealTemplate {

    private String id;
    private String name;
    private String instructions;
    private NutritionalValues nutritionalValues;
    private List<String> photos;
    private List<MealIngredient> ingredients;
    private MealType mealType;
    private String category;
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastUsed;
    private int usageCount;
}
