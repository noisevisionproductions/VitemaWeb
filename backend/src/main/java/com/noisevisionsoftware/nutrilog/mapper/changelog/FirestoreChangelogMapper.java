package com.noisevisionsoftware.nutrilog.mapper.changelog;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.nutrilog.model.changelog.ChangelogEntry;
import com.noisevisionsoftware.nutrilog.model.changelog.ChangelogEntryType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FirestoreChangelogMapper {

    public ChangelogEntry toChangelogEntry(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        return ChangelogEntry.builder()
                .id(document.getId())
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .createdAt((Timestamp) data.get("createdAt"))
                .author((String) data.get("author"))
                .type(ChangelogEntryType.valueOf(((String) data.get("type")).toUpperCase()))
                .build();
    }

    public Map<String, Object> toFirestoreMap(ChangelogEntry entry) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", entry.getTitle());
        data.put("description", entry.getDescription());
        data.put("createdAt", entry.getCreatedAt());
        data.put("author", entry.getAuthor());
        data.put("type", entry.getType().name());
        return data;
    }
}