package com.noisevisionsoftware.nutrilog.dto.response;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDay;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedProduct;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DietPreviewResponse {
    private List<ParsedDay> days;
    private List<Map.Entry<String, ParsedProduct>> shoppingList;
}