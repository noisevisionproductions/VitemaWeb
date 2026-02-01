package com.noisevisionsoftware.vitema.dto.response.invitation;

import com.noisevisionsoftware.vitema.model.invitation.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvitationResponse {
    private String id;
    private String trainerId;
    private String clientEmail;
    private String code;
    private InvitationStatus status;
    private Long createdAt;
    private Long expiresAt;
}
