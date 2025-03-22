package com.noisevisionsoftware.nutrilog.dto.response.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeDuplicateResponse {

    private RecipeResponse recipe1;
    private RecipeResponse recipe2;
    private double similarity;
}
