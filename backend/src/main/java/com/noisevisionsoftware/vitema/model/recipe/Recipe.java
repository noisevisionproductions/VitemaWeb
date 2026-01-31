package com.noisevisionsoftware.vitema.model.recipe;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipe {
    private String id;
    private String name;
    private String instructions;
    private Timestamp createdAt;
    private List<String> photos;
    private List<RecipeIngredient> ingredients;
    private NutritionalValues nutritionalValues;
    private String parentRecipeId;
    private String authorId;
    private boolean isPublic;
}

