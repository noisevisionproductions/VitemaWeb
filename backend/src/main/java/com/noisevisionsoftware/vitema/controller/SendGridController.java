package com.noisevisionsoftware.vitema.controller;

import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.EmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SavedTemplateRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.SingleEmailRequest;
import com.noisevisionsoftware.vitema.dto.request.newsletter.sendgrid.TargetedEmailRequest;
import com.noisevisionsoftware.vitema.model.newsletter.EmailTemplate;
import com.noisevisionsoftware.vitema.service.email.EmailTemplateService;
import com.noisevisionsoftware.vitema.service.email.SendGridService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/email")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
@Slf4j
public class SendGridController {

    private final SendGridService sendGridService;
    private final EmailTemplateService emailTemplateService;

    @PostMapping("/single")
    public ResponseEntity<?> sendSingleEmail(@Valid @RequestBody SingleEmailRequest request) {
        try {
            sendGridService.sendSingleEmail(request);

            String responseMessage = "Wiadomość została wysłana do: " + request.getRecipientEmail();
            if (request.getExternalRecipientId() != null) {
                responseMessage += " (ID zewnętrznego odbiorcy: " + request.getExternalRecipientId() + ")";
            }

            return ResponseEntity.ok().body(Map.of(
                    "message", responseMessage,
                    "success", true
            ));
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania pojedynczego emaila do {}", request.getRecipientEmail(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas wysyłania wiadomości: " + e.getMessage(),
                    "success", false
            ));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> sendBulkEmail(@Valid @RequestBody EmailRequest request) {
        try {
            sendGridService.sendBulkEmail(request);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Wiadomość została wysłana do wszystkich aktywnych i zweryfikowanych subskrybentów"
            ));
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania masowego emaila", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas wysyłania wiadomości: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/bulk-targeted")
    public ResponseEntity<?> sendTargetedBulkEmail(@Valid @RequestBody TargetedEmailRequest request) {
        try {
            Map<String, Object> result = sendGridService.sendTargetedBulkEmail(request);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania targeted emaila", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas wysyłania wiadomości: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/send/{email}")
    public ResponseEntity<?> sendEmail(@PathVariable String email, @Valid @RequestBody EmailRequest request) {
        try {
            sendGridService.sendEmail(email, request);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Wiadomość została wysłana do: " + email
            ));
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania emaila do {}", email, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas wysyłania wiadomości: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getTemplates() {
        Map<String, Object> templates = new HashMap<>();

        List<Map<String, String>> templatesList = List.of(
                Map.of(
                        "id", "basic",
                        "name", "Podstawowy",
                        "description", "Prosty szablon z logo i stopką"
                ),
                Map.of(
                        "id", "promotional",
                        "name", "Promocyjny",
                        "description", "Szablon z elementami promocyjnymi i wyróżnieniami"
                ),
                Map.of(
                        "id", "survey",
                        "name", "Ankieta",
                        "description", "Szablon zawierający informację o ankiecie"
                ),
                Map.of(
                        "id", "announcement",
                        "name", "Ogłoszenie",
                        "description", "Szablon dla ważnych ogłoszeń i powiadomień"
                )
        );

        templates.put("templates", templatesList);
        return ResponseEntity.ok(templates);
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewEmail(@Valid @RequestBody EmailRequest request) {
        try {
            String previewContent = sendGridService.renderEmailPreview(request);
            return ResponseEntity.ok().body(Map.of(
                    "preview", previewContent
            ));
        } catch (Exception e) {
            log.error("Błąd podczas generowania podglądu emaila", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas generowania podglądu: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/saved-templates")
    public ResponseEntity<List<EmailTemplate>> getSavedTemplates() {
        try {
            List<EmailTemplate> templates = emailTemplateService.getAllTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania zapisanych szablonów", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/saved-templates")
    public ResponseEntity<EmailTemplate> saveTemplate(@Valid @RequestBody SavedTemplateRequest request) {
        try {
            EmailTemplate savedTemplate = emailTemplateService.saveTemplate(request);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            log.error("Błąd podczas zapisywania szablonu", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/saved-templates/{id}")
    public ResponseEntity<EmailTemplate> getTemplateById(@PathVariable Long id) {
        try {
            Optional<EmailTemplate> templateOpt = emailTemplateService.getTemplateById(id);
            return templateOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Błąd podczas pobierania szablonu", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/saved-templates/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        try {
            emailTemplateService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Błąd podczas usuwania szablonu", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}