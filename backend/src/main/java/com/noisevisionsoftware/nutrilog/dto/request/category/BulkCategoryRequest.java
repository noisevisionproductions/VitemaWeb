package com.noisevisionsoftware.nutrilog.dto.request.category;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCategoryRequest {
    private List<ParsedProduct> products;
}
