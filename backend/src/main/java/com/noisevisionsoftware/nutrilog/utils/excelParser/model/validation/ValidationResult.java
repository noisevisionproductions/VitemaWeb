package com.noisevisionsoftware.nutrilog.utils.excelParser.model.validation;

public record ValidationResult(boolean isValid, String message, ValidationSeverity severity) {
}
