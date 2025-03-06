package com.noisevisionsoftware.nutrilog.mapper.changelog;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.dto.request.ChangelogEntryRequest;
import com.noisevisionsoftware.nutrilog.dto.response.ChangelogEntryResponse;
import com.noisevisionsoftware.nutrilog.model.changelog.ChangelogEntry;
import org.springframework.stereotype.Component;

@Component
public class ChangelogMapper {

    public ChangelogEntry toModel(ChangelogEntryRequest request, String author) {
        return ChangelogEntry.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .author(author)
                .createdAt(Timestamp.now())
                .build();
    }

    public ChangelogEntryResponse toResponse(ChangelogEntry entry) {
        return ChangelogEntryResponse.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .description(entry.getDescription())
                .createdAt(entry.getCreatedAt())
                .author(entry.getAuthor())
                .type(entry.getType())
                .build();
    }
}