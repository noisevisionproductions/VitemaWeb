package com.noisevisionsoftware.vitema.model.invitation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    private String id;
    private String trainerId;
    private String clientEmail;
    private String clientId;        // ID klienta (null dla PENDING, wypełniane przy ACCEPTED)
    private String code;
    private InvitationStatus status;
    private Long createdAt;
    private Long expiresAt;
}
