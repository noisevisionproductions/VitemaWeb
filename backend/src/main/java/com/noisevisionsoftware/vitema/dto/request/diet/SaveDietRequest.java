package com.noisevisionsoftware.vitema.dto.request.diet;

import com.noisevisionsoftware.vitema.model.diet.DietFileInfo;
import com.noisevisionsoftware.vitema.utils.excelParser.model.ParsedDietData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveDietRequest {
    private ParsedDietData parsedData;
    private String userId;
    private String authorId;
    private DietFileInfo fileInfo;
}

