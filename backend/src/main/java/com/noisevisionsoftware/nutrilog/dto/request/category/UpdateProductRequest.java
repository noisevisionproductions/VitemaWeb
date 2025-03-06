package com.noisevisionsoftware.nutrilog.dto.request.category;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private ParsedProduct oldProduct;
    private ParsedProduct newProduct;
}