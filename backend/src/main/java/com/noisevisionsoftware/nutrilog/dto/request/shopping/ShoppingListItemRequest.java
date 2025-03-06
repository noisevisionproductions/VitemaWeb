package com.noisevisionsoftware.nutrilog.dto.request.shopping;

import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItemRequest {
    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private double quantity;

    @NotBlank
    private String unit;

    @NotBlank
    private String original;

    private List<ShoppingListRecipeReferenceRequest> recipes;
}