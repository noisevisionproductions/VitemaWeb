package com.noisevisionsoftware.vitema.dto.search;

import com.noisevisionsoftware.vitema.model.recipe.NutritionalValues;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UnifiedSearchDto {

    private String id;
    private String name;
    private SearchResultType type;
    private NutritionalValues nutritionalValues;
    private String unit;
    private List<String> photos;
    private String authorId;

    public enum SearchResultType {
        RECIPE, PRODUCT
    }
}
