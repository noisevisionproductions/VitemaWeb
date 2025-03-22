package com.noisevisionsoftware.nutrilog.controller.contact;

import com.noisevisionsoftware.nutrilog.dto.request.ContactFormRequest;
import com.noisevisionsoftware.nutrilog.service.contact.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<?> submitContactForm(@Valid @RequestBody ContactFormRequest request) {
        try {
            contactService.processContactForm(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Twoja wiadomość została wysłana. Skontaktujemy się wkrótce."
            ));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas przetwarzania wiadomości."
            ));
        }
    }
}
