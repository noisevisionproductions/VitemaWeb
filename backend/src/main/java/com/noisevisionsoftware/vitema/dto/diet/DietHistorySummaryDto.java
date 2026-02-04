package com.noisevisionsoftware.vitema.dto.diet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietHistorySummaryDto {
    private String id;
    private String name;
    private String clientName;
    private String date;
}