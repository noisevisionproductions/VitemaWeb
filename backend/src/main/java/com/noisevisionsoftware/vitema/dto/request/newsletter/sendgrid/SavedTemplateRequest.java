package com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedTemplateRequest {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String subject;

    @NotBlank
    private String content;

    private String description;

    private boolean useTemplate = false;

    private String templateType = "basic";
}
