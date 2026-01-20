package com.noisevisionsoftware.vitema.utils.excelParser.model;

import lombok.Data;

@Data
public class ParsingResult {
    private boolean success;
    private ParsedProduct product;
    private String error;

    public ParsingResult() {
    }

    public ParsingResult(ParsedProduct product) {
        this.product = product;
        this.success = true;
    }

    public ParsingResult(ParsedProduct product, boolean success) {
        this.product = product;
        this.success = success;
    }
}