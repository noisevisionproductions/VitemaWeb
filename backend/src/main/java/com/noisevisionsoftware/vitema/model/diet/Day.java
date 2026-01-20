package com.noisevisionsoftware.vitema.model.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.google.cloud.Timestamp;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Day {
    private Timestamp date;
    private List<DayMeal> meals;
}