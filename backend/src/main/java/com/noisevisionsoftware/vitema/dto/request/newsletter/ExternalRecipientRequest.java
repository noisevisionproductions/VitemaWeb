package com.noisevisionsoftware.vitema.dto.request.newsletter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRecipientRequest {

    @NotBlank(message = "Email jest wymagany")
    @Email(message = "Podaj prawidłowy adres email")
    private String email;

    private String name;

    @NotBlank(message = "Kategoria jest wymagana")
    private String category;

    private List<String> tags;

    private String notes;

    private String status = "new";
}