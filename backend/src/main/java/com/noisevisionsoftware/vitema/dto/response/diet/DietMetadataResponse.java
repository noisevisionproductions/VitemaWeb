package com.noisevisionsoftware.vitema.dto.response.diet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietMetadataResponse {
    private int totalDays;
    private String fileName;
    private String fileUrl;
}