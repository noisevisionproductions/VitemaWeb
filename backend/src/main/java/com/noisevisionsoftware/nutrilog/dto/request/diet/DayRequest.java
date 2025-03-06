package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.google.cloud.Timestamp;
import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DayRequest {
    @NotNull
    private Timestamp date;

    @Valid
    @NotNull
    private List<DayMealRequest> meals;
}