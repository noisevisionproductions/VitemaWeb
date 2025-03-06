package com.noisevisionsoftware.nutrilog.utils.excelParser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnitProcessingResult {
    private String unit;
    private String name;
    private boolean foundKnownUnit;
}
