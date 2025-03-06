package com.noisevisionsoftware.nutrilog.utils.excelParser.model;

import lombok.Data;

@Data
public class QuantityInfo {
    private boolean success;
    private Double quantity;
    private String potentialUnit;
    private String remainingText;

    public QuantityInfo(boolean success) {
        this.success = success;
    }

    public QuantityInfo(Double quantity, String potentialUnit, String remainingText) {
        this.quantity = quantity;
        this.potentialUnit = potentialUnit;
        this.remainingText = remainingText;
    }
}
