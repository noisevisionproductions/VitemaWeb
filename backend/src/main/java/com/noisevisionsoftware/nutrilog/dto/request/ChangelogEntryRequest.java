package com.noisevisionsoftware.nutrilog.dto.request;

import com.noisevisionsoftware.nutrilog.model.changelog.ChangelogEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangelogEntryRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private ChangelogEntryType type;
}