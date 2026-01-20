package com.noisevisionsoftware.vitema.dto.request.newsletter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriberRequest {
    private String notes;
    private Map<String, String> metadata;
}