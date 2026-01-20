package com.noisevisionsoftware.vitema.controller.newsletter;

import com.noisevisionsoftware.vitema.exception.TooManyRequestsException;
import com.noisevisionsoftware.vitema.model.newsletter.MetadataWrapper;
import com.noisevisionsoftware.vitema.model.newsletter.NewsletterSubscriber;
import com.noisevisionsoftware.vitema.dto.request.newsletter.SubscriptionRequest;
import com.noisevisionsoftware.vitema.service.newsletter.PublicNewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final PublicNewsletterService publicNewsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        try {
            publicNewsletterService.subscribe(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Sprawdź swoją skrzynkę email, aby potwierdzić zapis do newslettera."
            ));
        } catch (TooManyRequestsException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas zapisywania do newslettera: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifySubscription(@RequestParam String token) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<NewsletterSubscriber> future = executor.submit(() -> publicNewsletterService.verifySubscriberAndGet(token));

            NewsletterSubscriber subscriber;
            try {
                subscriber = future.get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of(
                        "message", "Weryfikacja trwała zbyt długo. Spróbuj ponownie później."
                ));
            } finally {
                executor.shutdown();
            }

            if (subscriber != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Adres email został pomyślnie zweryfikowany.");
                response.put("subscriberId", subscriber.getId());
                response.put("subscriberRole", subscriber.getRole().getValue());
                response.put("email", subscriber.getEmail());
                response.put("verifiedAt", subscriber.getVerifiedAt());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Nieprawidłowy token weryfikacyjny."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas weryfikacji adresu email."
            ));
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam String email) {
        try {
            boolean unsubscribed = publicNewsletterService.unsubscribe(email);
            if (unsubscribed) {
                return ResponseEntity.ok(Map.of(
                        "message", "Zostałeś wypisany z newslettera."
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Podany adres email nie jest zapisany do newslettera."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas wypisywania z newslettera."
            ));
        }
    }

    @PostMapping("/subscribers/{id}/metadata")
    public ResponseEntity<?> updateSubscriberMetadata(@PathVariable Long id, @RequestBody MetadataWrapper wrapper) {
        try {
            publicNewsletterService.updateSubscriberMetadata(id, wrapper.getMetadata());
            return ResponseEntity.ok(Map.of(
                    "message", "Metadane zostały zapisane pomyślnie."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Wystąpił błąd podczas zapisywania metadanych: " + e.getMessage()
            ));
        }
    }
}
