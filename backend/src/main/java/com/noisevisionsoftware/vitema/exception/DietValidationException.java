package com.noisevisionsoftware.vitema.exception;

import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DietValidationException extends RuntimeException {
    private final List<ValidationResult> validationResults;

    public DietValidationException(String message, List<ValidationResult> validationResults) {
        super(message);
        this.validationResults = validationResults;
    }

}