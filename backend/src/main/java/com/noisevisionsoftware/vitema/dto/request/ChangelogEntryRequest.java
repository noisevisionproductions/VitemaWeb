package com.noisevisionsoftware.vitema.dto.request;

import com.noisevisionsoftware.vitema.model.changelog.ChangelogEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogEntryRequest {
    @NotBlank
    private String description;

    @NotNull
    private ChangelogEntryType type;
}