package com.noisevisionsoftware.vitema.dto.response.diet;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DietResponse {
    private String id;
    private String userId;
    private String userEmail;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<DayResponse> days;
    private DietMetadataResponse metadata;
}