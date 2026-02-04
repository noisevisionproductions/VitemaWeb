package com.noisevisionsoftware.vitema.model.diet;

import com.google.cloud.Timestamp;
import lombok.*;

import java.util.List;

@Data
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Diet {
    private String id;
    private String userId;
    private String authorId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Day> days;
    private DietMetadata metadata;
}
