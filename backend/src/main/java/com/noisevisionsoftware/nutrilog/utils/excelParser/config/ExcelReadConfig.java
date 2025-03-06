package com.noisevisionsoftware.nutrilog.utils.excelParser.config;

import lombok.Getter;

public class ExcelReadConfig {
    // Gettery
    @Getter
    private int sheetNumber = 0;
    @Getter
    private int headerRowNumber = 0;
    private boolean skipEmptyRows = true;
    private boolean trimCells = true;

    // Builder pattern
    public static class Builder {
        private final ExcelReadConfig config;

        public Builder() {
            config = new ExcelReadConfig();
        }

        public Builder sheetNumber(int sheetNumber) {
            config.sheetNumber = sheetNumber;
            return this;
        }

        public Builder headerRowNumber(int headerRowNumber) {
            config.headerRowNumber = headerRowNumber;
            return this;
        }

        public Builder skipEmptyRows(boolean skipEmptyRows) {
            config.skipEmptyRows = skipEmptyRows;
            return this;
        }

        public Builder trimCells(boolean trimCells) {
            config.trimCells = trimCells;
            return this;
        }

        public ExcelReadConfig build() {
            return config;
        }
    }

    public boolean shouldSkipEmptyRows() {
        return skipEmptyRows;
    }

    public boolean shouldTrimCells() {
        return trimCells;
    }
}