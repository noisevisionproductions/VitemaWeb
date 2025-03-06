package com.noisevisionsoftware.nutrilog.dto.response.recipe;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeResponse {
    private String id;
    private String name;
    private String instructions;
    private Timestamp createdAt;
    private List<String> photos;
    private NutritionalValuesResponse nutritionalValues;
    private String parentRecipeId;
}