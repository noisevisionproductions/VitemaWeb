package com.noisevisionsoftware.nutrilog.model.diet.template;

import com.google.cloud.Timestamp;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietTemplate {
    private String id;
    private String name;
    private String description;
    private DietTemplateCategory category;
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;

    private int duration;
    private int mealsPerDay;
    private Map<String, String> mealTimes;
    private List<String> mealTypes;

    private List<DietTemplateDayData> days;
    private DietTemplateNutrition targetNutrition;

    private int usageCount;
    private Timestamp lastUsed;
    private String notes;
}
