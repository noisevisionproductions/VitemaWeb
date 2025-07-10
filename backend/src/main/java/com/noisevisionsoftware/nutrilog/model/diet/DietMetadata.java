package com.noisevisionsoftware.nutrilog.model.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietMetadata {
    private int totalDays;
    private String fileName;
    private String fileUrl;
}