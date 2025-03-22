package com.noisevisionsoftware.nutrilog.model.diet;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplate {
    private int mealsPerDay;
    private Long startDate;
    private int duration;
    private Map<String, String> mealTimes;
    private List<MealType> mealTypes;
}
