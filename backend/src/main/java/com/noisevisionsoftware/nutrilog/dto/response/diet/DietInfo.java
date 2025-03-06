package com.noisevisionsoftware.nutrilog.dto.response.diet;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietInfo {

    private boolean hasDiet;
    private Timestamp startDate;
    private Timestamp endDate;
}
