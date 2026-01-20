package com.noisevisionsoftware.vitema.dto.request.newsletter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkExternalRecipientRequest {

    @NotEmpty(message = "Lista odbiorców nie może być pusta")
    @Valid
    private List<ExternalRecipientRequest> recipients;
}