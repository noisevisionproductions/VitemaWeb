package com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetedEmailRequest {

    @NotBlank
    private String subject;

    @NotBlank
    private String content;

    @NotBlank
    private String recipientType;
    private Map<String, Object> subscriberFilters;
    private Map<String, Object> externalFilters;
    private List<String> externalRecipientIds;
    private boolean useTemplate = false;
    private String templateType = "basic";
    private String savedTemplateId;
    private List<String> categories;
    private boolean updateStatus = false;
    private String newStatus = "contacted";
}
