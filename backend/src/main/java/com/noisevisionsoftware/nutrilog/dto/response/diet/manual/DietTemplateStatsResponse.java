package com.noisevisionsoftware.nutrilog.dto.response.diet.manual;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateStatsResponse {

    private long totalTemplates;
    private Map<String, Long> templatesByCategory;
    private DietTemplateResponse mostUsedTemplate;
    private DietTemplateResponse newestTemplate;
    private long totalUsageCount;
}
