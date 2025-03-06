package com.noisevisionsoftware.nutrilog.dto.request.category;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoriesRequest {
    private Map<String, List<ParsedProduct>> categorizedProducts;
}