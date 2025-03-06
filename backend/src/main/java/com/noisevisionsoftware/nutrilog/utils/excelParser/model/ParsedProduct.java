package com.noisevisionsoftware.nutrilog.utils.excelParser.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedProduct {
    private String id;
    private String name;
    private double quantity;
    private String unit;
    private String original;
    private boolean hasCustomUnit;
    private String categoryId;

    public ParsedProduct(String name, double quantity, String unit, String original, boolean hasCustomUnit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.original = original;
        this.hasCustomUnit = hasCustomUnit;}
}