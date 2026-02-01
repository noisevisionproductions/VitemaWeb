package com.noisevisionsoftware.vitema.mapper.invitation;

import com.noisevisionsoftware.vitema.dto.response.invitation.InvitationResponse;
import com.noisevisionsoftware.vitema.model.invitation.Invitation;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationResponse toResponse(Invitation invitation) {
        if (invitation == null) return null;

        return InvitationResponse.builder()
                .id(invitation.getId())
                .trainerId(invitation.getTrainerId())
                .clientEmail(invitation.getClientEmail())
                .code(invitation.getCode())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}
