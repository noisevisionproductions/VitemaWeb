package com.noisevisionsoftware.vitema.dto.request.invitation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcceptInvitationRequest {
    
    @NotBlank(message = "Kod zaproszenia jest wymagany")
    private String code;
}
