package com.noisevisionsoftware.nutrilog.model.diet.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplateApplicationData {
    private String templateId;
    private String appliedToUserId;
    private Timestamp appliedAt;
    private String appliedBy;
    private Timestamp startDate;
    private Map<String, String> customizations;
    private String notes;
}
