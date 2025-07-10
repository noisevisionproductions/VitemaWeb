package com.noisevisionsoftware.nutrilog.model.diet.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateDayData {
    private int dayNumber;
    private String dayName;
    private List<DietTemplateMealData> meals;
    private String notes;
}
