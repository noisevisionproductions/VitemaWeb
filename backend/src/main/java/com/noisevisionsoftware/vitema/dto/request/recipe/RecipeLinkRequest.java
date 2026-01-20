package com.noisevisionsoftware.vitema.dto.request.recipe;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeLinkRequest {

    @NotBlank
    private String parentRecipeId;
}
