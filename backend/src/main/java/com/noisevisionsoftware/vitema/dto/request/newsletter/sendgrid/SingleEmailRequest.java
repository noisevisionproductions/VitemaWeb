package com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleEmailRequest {

    @NotBlank
    private String subject;

    @NotBlank
    private String content;

    @NotBlank
    @Email
    private String recipientEmail;

    private String recipientName;
    private Long externalRecipientId;
    private boolean updateLastContactDate = true;
    private boolean useTemplate = false;
    private String templateType = "basic";
    private Long savedTemplateId;
    private List<String> categories;
}
