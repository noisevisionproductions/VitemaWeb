package com.noisevisionsoftware.vitema.model.changelog;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogEntry {
    private String id;
    private String description;
    private Timestamp createdAt;
    private String author;
    private ChangelogEntryType type;
}