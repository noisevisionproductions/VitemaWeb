package com.noisevisionsoftware.vitema.dto.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietDayDto {
    private String date;
    private List<DietMealDto> meals;
}