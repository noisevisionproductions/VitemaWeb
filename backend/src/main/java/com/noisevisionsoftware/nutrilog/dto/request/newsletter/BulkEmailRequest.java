package com.noisevisionsoftware.nutrilog.dto.request.newsletter;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequest {
    @NotBlank(message = "Temat jest wymagany")
    private String subject;

    @NotBlank(message = "Treść wiadomości jest wymagana")
    private String content;
}