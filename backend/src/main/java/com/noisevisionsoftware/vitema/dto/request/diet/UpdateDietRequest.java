package com.noisevisionsoftware.vitema.dto.request.diet;

import com.noisevisionsoftware.vitema.dto.diet.DietDayDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class UpdateDietRequest {
    private List<DietDayDto> days;
}