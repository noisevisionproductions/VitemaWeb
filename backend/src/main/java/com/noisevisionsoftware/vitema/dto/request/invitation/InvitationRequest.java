package com.noisevisionsoftware.vitema.dto.request.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationRequest {
    
    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podaj prawidłowy adres email")
    private String email;
}
