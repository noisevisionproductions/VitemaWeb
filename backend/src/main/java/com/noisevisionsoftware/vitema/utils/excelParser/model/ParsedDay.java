package com.noisevisionsoftware.vitema.utils.excelParser.model;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParsedDay {
    private Timestamp date;
    private List<ParsedMeal> meals;
}