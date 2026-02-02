package com.noisevisionsoftware.vitema.dto.seed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDTO {
    private String category;
    private List<ProductItemDTO> items;
}
