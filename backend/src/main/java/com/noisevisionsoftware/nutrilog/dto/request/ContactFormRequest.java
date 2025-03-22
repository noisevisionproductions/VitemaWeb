package com.noisevisionsoftware.nutrilog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactFormRequest {

    @NotBlank(message = "Imię lub nazwa firmy jest wymagana")
    @Size(max = 100, message = "Imię lub nazwa firmy nie może przekraczać 100 znaków")
    private String name;

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podaj prawidłowy adres email")
    private String email;

    private String phone;

    @NotBlank(message = "Wiadomość jest wymagana")
    @Size(max = 2000, message = "Wiadomość nie może przekraczać 2000 znaków")
    private String message;
}