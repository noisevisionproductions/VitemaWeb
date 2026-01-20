package com.noisevisionsoftware.vitema.utils.excelParser.model.unit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitDetectionResult {
    private String unit;
    private String type;
    private boolean match;
}
