package com.noisevisionsoftware.vitema.dto.request.diet.manual;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateIngredientRequest {

    @NotBlank(message = "Nazwa składnika jest wymagana")
    private String name;

    @NotNull(message = "Ilość jest wymagana")
    @Positive(message = "Ilość musi być większa od 0")
    private Double quantity;

    @NotBlank(message = "Jednostka jest wymagana")
    private String unit;

    private String original;
    private String categoryId;
    private boolean hasCustomUnit;
}