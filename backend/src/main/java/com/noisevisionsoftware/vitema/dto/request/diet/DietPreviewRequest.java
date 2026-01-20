package com.noisevisionsoftware.vitema.dto.request.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietPreviewRequest {
    private int mealsPerDay;
    private String startDate;
    private int duration;
    private Map<String, String> mealTimes;
    private List<String> mealTypes;
}