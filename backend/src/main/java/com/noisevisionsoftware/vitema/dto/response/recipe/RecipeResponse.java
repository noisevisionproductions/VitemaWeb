package com.noisevisionsoftware.vitema.dto.response.recipe;

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
public class RecipeResponse {
    private String id;
    private String originalId;
    private String name;
    private String instructions;
    private Timestamp createdAt;
    private List<String> photos;
    private NutritionalValuesResponse nutritionalValues;
    private String parentRecipeId;
    private boolean isPublic;
    private boolean isMine;
    private String authorId;
}