package com.noisevisionsoftware.vitema.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private String category;
    private String unit;
    private Double kcal;
    private Double protein;
    private Double fat;
    private Double carbs;

    @Builder.Default
    private boolean isVerified = true;
}
