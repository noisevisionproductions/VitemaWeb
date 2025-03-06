package com.noisevisionsoftware.nutrilog.utils.excelParser.model.unit;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductUnit {
    private String value;
    private String label;
    private String type;
    private String baseUnit;
    private Double conversionFactor;

    public ProductUnit(String value, String label, String type) {
        this.value = value;
        this.label = label;
        this.type = type;
    }

    public ProductUnit(String value, String label, String type, String baseUnit, Double conversionFactor) {
        this.value = value;
        this.label = label;
        this.type = type;
        this.baseUnit = baseUnit;
        this.conversionFactor = conversionFactor;
    }
}