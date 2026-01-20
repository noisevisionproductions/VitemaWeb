package com.noisevisionsoftware.vitema.dto.response.recipe;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipesPageResponse {

    private List<RecipeResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
