package com.noisevisionsoftware.vitema.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy format adresu email")
    private String email;

    private String password;

}
