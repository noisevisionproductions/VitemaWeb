package com.noisevisionsoftware.vitema.dto.request.diet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalorieValidationRequest {

    private boolean validationEnabled;
    private Integer targetCalories;
    private Integer errorMarginPercent = 5;
}
