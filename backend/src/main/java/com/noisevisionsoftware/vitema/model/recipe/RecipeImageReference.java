package com.noisevisionsoftware.vitema.model.recipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeImageReference {

    private String id;
    private String imageUrl;
    private String storagePath;
    private int referenceCount;
}
