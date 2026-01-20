package com.noisevisionsoftware.vitema.dto.request.diet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietRequest {

    @NotBlank
    private String userId;

    @Valid
    @NotNull
    private List<DayRequest> days;

    @Valid
    private DietMetadataRequest metadata;
}