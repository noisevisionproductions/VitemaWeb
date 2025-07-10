package com.noisevisionsoftware.nutrilog.dto.request.diet.manual;

import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDay;
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
public class ManualDietRequest {

    private String userId;
    private List<ParsedDay> days;
    private int mealsPerDay;
    private String startDate;
    private int duration;
    private Map<String, String> mealTimes;
    private List<String> mealTypes;

}
