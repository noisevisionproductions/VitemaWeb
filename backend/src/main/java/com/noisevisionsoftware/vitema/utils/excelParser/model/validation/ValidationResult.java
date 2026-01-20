package com.noisevisionsoftware.vitema.utils.excelParser.model.validation;

public record ValidationResult(boolean isValid, String message, ValidationSeverity severity) {
}
