package com.noisevisionsoftware.vitema.dto.request.diet.manual;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateDayRequest {

    @NotNull(message = "Numer dnia jest wymagany")
    @Positive(message = "Numer dnia musi być większy od 0")
    private Integer dayNumber;

    private String dayName;
    private String notes;

    @Valid
    private List<DietTemplateMealRequest> meals;
}