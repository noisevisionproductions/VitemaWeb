package com.noisevisionsoftware.vitema.dto.response.product;

import com.noisevisionsoftware.vitema.model.product.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String authorId;
    private ProductType type;
    private String name;
    private String category;
    private String unit;
    private double kcal;
    private double protein;
    private double fat;
    private double carbs;
    private boolean isVerified;
}
