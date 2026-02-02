package com.noisevisionsoftware.vitema.dto.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemDTO {
    private String name;
    private String unit;
    
    @JsonProperty("weight")
    private Double weight;  // For items with default weight (e.g., eggs, bread rolls)
    
    @JsonProperty("kcal")
    private Double kcal;
    
    private Double protein;
    private Double fat;
    private Double carbs;
}
