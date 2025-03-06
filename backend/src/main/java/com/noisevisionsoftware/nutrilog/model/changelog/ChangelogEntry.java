package com.noisevisionsoftware.nutrilog.model.changelog;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangelogEntry {
    private String id;
    private String title;
    private String description;
    private Timestamp createdAt;
    private String author;
    private ChangelogEntryType type;
}