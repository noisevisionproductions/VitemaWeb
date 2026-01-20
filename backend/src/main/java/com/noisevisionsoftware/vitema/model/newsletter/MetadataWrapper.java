package com.noisevisionsoftware.vitema.model.newsletter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataWrapper {
    private Map<String, String> metadata;
}