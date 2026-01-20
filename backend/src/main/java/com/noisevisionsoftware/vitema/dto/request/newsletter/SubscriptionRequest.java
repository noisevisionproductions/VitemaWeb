package com.noisevisionsoftware.vitema.dto.request.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podany adres email jest nieprawidłowy")
    private String email;

    @NotBlank(message = "Rola jest wymagana")
    private String role;
}
