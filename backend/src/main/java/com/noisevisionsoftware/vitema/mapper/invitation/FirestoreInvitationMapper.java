package com.noisevisionsoftware.vitema.mapper.invitation;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import com.noisevisionsoftware.vitema.model.invitation.InvitationStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FirestoreInvitationMapper {

    public Invitation toInvitation(DocumentSnapshot document) {
        if (document == null || !document.exists()) return null;

        Map<String, Object> data = document.getData();
        if (data == null) return null;

        return Invitation.builder()
                .id(document.getId())
                .trainerId((String) data.get("trainerId"))
                .clientEmail((String) data.get("clientEmail"))
                .code((String) data.get("code"))
                .status(data.get("status") != null 
                        ? InvitationStatus.valueOf((String) data.get("status")) 
                        : InvitationStatus.PENDING)
                .createdAt(convertToLong(data.get("createdAt")))
                .expiresAt(convertToLong(data.get("expiresAt")))
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

    public Map<String, Object> toFirestoreMap(Invitation invitation) {
        Map<String, Object> data = new HashMap<>();
        data.put("trainerId", invitation.getTrainerId());
        data.put("clientEmail", invitation.getClientEmail());
        data.put("code", invitation.getCode());
        data.put("status", invitation.getStatus().name());
        data.put("createdAt", invitation.getCreatedAt());
        data.put("expiresAt", invitation.getExpiresAt());
        return data;
    }
}
