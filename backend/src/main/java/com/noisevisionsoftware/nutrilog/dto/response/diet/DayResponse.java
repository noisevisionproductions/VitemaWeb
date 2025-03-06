package com.noisevisionsoftware.nutrilog.dto.response.diet;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DayResponse {
    private Timestamp date;
    private List<DayMealResponse> meals;
}
