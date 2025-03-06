package com.noisevisionsoftware.nutrilog.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DietPreviewRequest {
    private int mealsPerDay;
    private String startDate;
    private int duration;
    private Map<String, String> mealTimes;
    private List<String> mealTypes;
}