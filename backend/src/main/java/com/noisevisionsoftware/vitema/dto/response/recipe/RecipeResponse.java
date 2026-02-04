package com.noisevisionsoftware.vitema.dto.response.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.recipe.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeResponse {
    private String id;
    private String originalId;
    private String name;
    private String instructions;
    private Timestamp createdAt;
    private List<String> photos;
    private NutritionalValuesResponse nutritionalValues;
    private List<RecipeIngredient> ingredients;
    private String parentRecipeId;
    
    @JsonProperty("isPublic")
    private boolean isPublic;
    
    @JsonProperty("isMine")
    private boolean isMine;
    
    private String authorId;
}