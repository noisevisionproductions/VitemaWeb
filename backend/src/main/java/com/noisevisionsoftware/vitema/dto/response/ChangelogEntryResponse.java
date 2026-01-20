package com.noisevisionsoftware.vitema.dto.response;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangelogEntryResponse {
    private String id;
    private String description;
    private Timestamp createdAt;
    private String author;
    private ChangelogEntryType type;
    private String typeString;
}