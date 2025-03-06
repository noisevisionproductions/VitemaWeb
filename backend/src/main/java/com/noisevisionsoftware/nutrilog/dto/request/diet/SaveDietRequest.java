package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.noisevisionsoftware.nutrilog.model.diet.DietFileInfo;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDietData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveDietRequest {
    private ParsedDietData parsedData;
    private String userId;
    private DietFileInfo fileInfo;
}

