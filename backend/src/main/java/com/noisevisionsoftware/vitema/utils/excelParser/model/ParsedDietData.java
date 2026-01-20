package com.noisevisionsoftware.vitema.utils.excelParser.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDietData {
    private List<ParsedDay> days;
    private Map<String, List<String>> categorizedProducts;
    private List<String> shoppingList;
    private Map<String, String> mealTimes;
    private Integer mealsPerDay;
    private Timestamp startDate;
    private Integer duration;
    private List<String> mealTypes;
}
