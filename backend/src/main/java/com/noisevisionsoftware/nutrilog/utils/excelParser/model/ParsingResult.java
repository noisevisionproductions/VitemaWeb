package com.noisevisionsoftware.nutrilog.utils.excelParser.model;

import lombok.Data;

@Data
public class ParsingResult {
    private boolean success;
    private ParsedProduct product;
    private String error;

    public ParsingResult(String error) {
        this.error = error;
    }

    public ParsingResult(ParsedProduct product) {
        this.product = product;
    }
}
