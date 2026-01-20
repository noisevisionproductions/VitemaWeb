package com.noisevisionsoftware.vitema.model.user;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    private String id;
    private String userId;
    private Timestamp lastChangelogRead;
}
