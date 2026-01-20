package com.noisevisionsoftware.vitema.dto.request.diet.manual;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealIngredientRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double quantity;

    @NotBlank
    private String unit;

    private String original;
    private String categoryId;
    private boolean hasCustomUnit;
}
