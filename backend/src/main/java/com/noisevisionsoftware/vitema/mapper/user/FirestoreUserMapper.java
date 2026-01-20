package com.noisevisionsoftware.vitema.mapper.user;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.user.Gender;
import com.noisevisionsoftware.vitema.model.user.User;
import com.noisevisionsoftware.vitema.model.user.UserRole;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FirestoreUserMapper {

    public User toUser(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        return User.builder()
                .id(document.getId())
                .email((String) data.get("email"))
                .nickname((String) data.get("nickname"))
                .gender(data.get("gender") != null ? Gender.valueOf((String) data.get("gender")) : null)
                .birthDate(convertToLong(data.get("birthDate")))
                .storedAge(data.get("storedAge") != null
                        ? ((Number) data.get("storedAge")).intValue()
                        : null)
                .profileCompleted(data.get("profileCompleted") != null
                        ? (Boolean) data.get("profileCompleted")
                        : false)
                .role(data.get("role") != null
                        ? UserRole.valueOf((String) data.get("role"))
                        : UserRole.USER)
                .note((String) data.get("note"))
                .createdAt(convertToLong(data.get("createdAt")))
                .build();
    }

    private Long convertToLong(Object value) {
        return switch (value) {
            case Long l -> l;
            case Timestamp timestamp -> timestamp.getSeconds() * 1000 + timestamp.getNanos() / 1_000_000;
            case Number number -> number.longValue();
            case null, default -> null;
        };
    }

    public Map<String, Object> toFirestoreMap(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", user.getEmail());
        data.put("nickname", user.getNickname());
        data.put("gender", user.getGender() != null ? user.getGender().name() : null);
        data.put("birthDate", user.getBirthDate());
        data.put("storedAge", user.getStoredAge());
        data.put("profileCompleted", user.isProfileCompleted());
        data.put("role", user.getRole().name());
        data.put("note", user.getNote());
        data.put("createdAt", user.getCreatedAt());
        return data;
    }
}