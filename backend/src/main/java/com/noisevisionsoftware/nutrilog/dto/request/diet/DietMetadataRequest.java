package com.noisevisionsoftware.nutrilog.dto.request.diet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietMetadataRequest {
    private int totalDays;
    private String fileName;
    private String fileUrl;
}