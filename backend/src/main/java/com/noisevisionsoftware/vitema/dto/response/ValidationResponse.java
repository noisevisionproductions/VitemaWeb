package com.noisevisionsoftware.vitema.dto.response;

import com.noisevisionsoftware.vitema.utils.excelParser.model.validation.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResponse {
    private boolean isValid;
    private List<ValidationResult> validationResults;
    private Map<String, Object> additionalData;
}
