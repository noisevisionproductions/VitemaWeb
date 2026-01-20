package com.noisevisionsoftware.vitema.mapper.changelog;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.vitema.dto.request.ChangelogEntryRequest;
import com.noisevisionsoftware.vitema.dto.response.ChangelogEntryResponse;
import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntry;
import org.springframework.stereotype.Component;

@Component
public class ChangelogMapper {

    public ChangelogEntry toModel(ChangelogEntryRequest request, String author) {
        return ChangelogEntry.builder()
                .description(request.getDescription())
                .type(request.getType())
                .author(author)
                .createdAt(Timestamp.now())
                .build();
    }

    public ChangelogEntryResponse toResponse(ChangelogEntry entry) {
        return ChangelogEntryResponse.builder()
                .id(entry.getId())
                .description(entry.getDescription())
                .createdAt(entry.getCreatedAt())
                .author(entry.getAuthor())
                .type(entry.getType())
                .build();
    }
}