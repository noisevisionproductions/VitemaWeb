package com.noisevisionsoftware.vitema.controller.newsletter;

import com.noisevisionsoftware.vitema.dto.request.newsletter.BulkExternalRecipientRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.ExternalRecipientRequest;
import com.noisevisionsoftware.vitema.model.newsletter.ExternalRecipient;
import com.noisevisionsoftware.vitema.service.newsletter.ExternalRecipientsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/email/external-recipients")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
@Slf4j
public class ExternalRecipientsController {

    private final ExternalRecipientsService externalRecipientsService;

    @GetMapping
    public ResponseEntity<List<ExternalRecipient>> getAllRecipients() throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(externalRecipientsService.getAllRecipients());
    }

    @PostMapping
    public ResponseEntity<ExternalRecipient> addRecipient(@Valid @RequestBody ExternalRecipientRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(externalRecipientsService.addRecipient(request));
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkAddRecipients(@Valid @RequestBody BulkExternalRecipientRequest request)
            throws ExecutionException, InterruptedException {
        Map<String, Object> result = externalRecipientsService.bulkAddRecipients(request.getRecipients());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExternalRecipient> updateRecipient(
            @PathVariable Long id,
            @Valid @RequestBody ExternalRecipientRequest request)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(externalRecipientsService.updateRecipient(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ExternalRecipient> updateRecipientStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request)
            throws ExecutionException, InterruptedException {
        String status = request.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(externalRecipientsService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipient(@PathVariable Long id)
            throws ExecutionException, InterruptedException {
        externalRecipientsService.deleteRecipient(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> getCategories() {
        return ResponseEntity.ok(Map.of(
                "categories", externalRecipientsService.getCategories()
        ));
    }
}
