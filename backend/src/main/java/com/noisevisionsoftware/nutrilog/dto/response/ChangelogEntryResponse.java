package com.noisevisionsoftware.nutrilog.dto.response;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.changelog.ChangelogEntryType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangelogEntryResponse {
    private String id;
    private String title;
    private String description;
    private Timestamp createdAt;
    private String author;
    private ChangelogEntryType type;
}